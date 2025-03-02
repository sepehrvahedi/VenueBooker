package army.booker.domain.bundle.services

import army.booker.domain.bundle.Bundle
import army.booker.domain.bundle.DailyReservation
import army.booker.domain.bundle.ProductSorting
import army.booker.domain.bundle.ProductType
import army.booker.domain.bundle.repositories.BundleRepository
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDate

@Service
class BundleServiceImpl(
  private val bundleRepository: BundleRepository,
  private val mongoTemplate: ReactiveMongoTemplate,
) : BundleService {

  override fun createBundle(
    name: String,
    price: Long,
    products: List<ProductType>,
    ownerId: String,
  ): Mono<Bundle> {
    return bundleRepository.findByName(name)
      .flatMap<Bundle> {
        Mono.error(IllegalArgumentException("Bundle with name $name already exists"))
      }
      .switchIfEmpty {
        bundleRepository.save(
          Bundle(
            name = name,
            price = price,
            products = products,
            ownerId = ownerId
          )
        )
      }
  }

  override fun editBundles(
    ids: List<String>,
    price: Long,
    products: List<ProductType>,
    ownerId: String,
  ): Mono<Unit> {
    val query = Query(
      Criteria.where("id").`in`(ids)
        .and("ownerId").`is`(ownerId)
    )

    val update = Update()
      .set("price", price)
      .set("products", products)

    return mongoTemplate.updateMulti(query, update, Bundle::class.java)
      .flatMap { result ->
        if (result.modifiedCount > 0) {
          Mono.just(Unit)
        } else {
          Mono.error(IllegalArgumentException("No bundles found for update or access denied"))
        }
      }
  }

  override fun updateBundleStatus(
    id: String,
    active: Boolean,
    ownerId: String,
  ): Mono<Bundle> {
    return bundleRepository.findById(id)
      .flatMap { bundle ->
        if (bundle.ownerId != ownerId) {
          Mono.error(IllegalArgumentException("Access denied"))
        } else {
          bundleRepository.save(
            bundle.copy(active = active)
          )
        }
      }
      .switchIfEmpty(
        Mono.error(IllegalArgumentException("Bundle not found"))
      )
  }

  override fun findBundles(
    name: String?,
    minPrice: Long?,
    maxPrice: Long?,
    active: Boolean?,
    products: List<ProductType>?,
    sorting: ProductSorting?,
    ownerId: String?,
  ): Flux<Bundle> {
    val criteria = Criteria()

    ownerId?.let { criteria.and("ownerId").`is`(ownerId) }

    name?.let {
      criteria.and("name").regex(".*$it.*", "i")
    }

    if (minPrice != null && maxPrice != null) {
      criteria.and("price").gte(minPrice).lte(maxPrice)
    } else {
      minPrice?.let { criteria.and("price").gte(it) }
      maxPrice?.let { criteria.and("price").lte(it) }
    }

    active?.let {
      criteria.and("active").`is`(it)
    }

    products?.let {
      if (it.isNotEmpty()) {
        criteria.and("products").all(it)
      }
    }

    val query = Query(criteria)

    val sort = when (sorting) {
      ProductSorting.PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price")
      ProductSorting.PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price")
      ProductSorting.NAME_ASC -> Sort.by(Sort.Direction.ASC, "name")
      ProductSorting.NAME_DESC -> Sort.by(Sort.Direction.DESC, "name")
      ProductSorting.CREATED_ASC -> Sort.by(Sort.Direction.ASC, "createdAt")
      ProductSorting.CREATED_DESC -> Sort.by(Sort.Direction.DESC, "createdAt")
      null -> Sort.by(Sort.Direction.DESC, "createdAt")
    }

    query.with(sort)

    return mongoTemplate.find(query, Bundle::class.java)
  }

  override fun reserveBundle(
    bundleId: String,
    userId: String,
    userName: String,
    reservationDate: LocalDate
  ): Mono<Bundle> {
    return bundleRepository.findById(bundleId)
      .flatMap { bundle ->
        if (!bundle.active) {
          return@flatMap Mono.error(IllegalArgumentException("Bundle is not active"))
        }

        val isAlreadyReserved = bundle.reservations.any { it.reservationDate == reservationDate }
        if (isAlreadyReserved) {
          return@flatMap Mono.error(IllegalArgumentException("Bundle is already reserved for $reservationDate"))
        }

        val newReservation = DailyReservation(
          userId = userId,
          userName = userName,
          reservationDate = reservationDate
        )

        val updatedReservations = bundle.reservations + newReservation

        // Using findAndModify to atomically update with version check
        val query = Query(Criteria.where("_id").`is`(bundleId).and("version").`is`(bundle.version))
        val update = Update()
          .set("reservations", updatedReservations)
          .inc("version", 1)

        mongoTemplate.findAndModify(query, update, Bundle::class.java)
          .switchIfEmpty {
            Mono.error(
              ConcurrentModificationException("Bundle was modified by another transaction. Please try again.")
            )
          }
      }
      .switchIfEmpty(
        Mono.error(IllegalArgumentException("Bundle not found"))
      )
      .retry(3) // Retry up to 3 times if concurrent modification occurs
      .onErrorResume(ConcurrentModificationException::class.java) {
        Mono.error(IllegalArgumentException("Could not reserve bundle due to concurrent access. Please try again."))
      }
  }

  override fun findUserReservations(userId: String): Flux<Bundle> {
    val criteria = Criteria.where("reservations").elemMatch(
      Criteria.where("userId").`is`(userId)
    )

    val query = Query(criteria)
    query.with(Sort.by(Sort.Direction.DESC, "createdAt"))

    return mongoTemplate.find(query, Bundle::class.java)
      .map { bundle ->
        bundle.copy(
          reservations = bundle.reservations.filter { it.userId == userId }
        )
      }
  }
}
