package army.booker.domain.bundle.services

import army.booker.domain.bundle.Bundle
import army.booker.domain.bundle.ProductSorting
import army.booker.domain.bundle.ProductType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface BundleService {
  fun createBundle(
    name: String,
    price: Long,
    products: List<ProductType>,
    ownerId: String
  ): Mono<Bundle>

  fun editBundles(
    ids: List<String>,
    price: Long,
    products: List<ProductType>,
    ownerId: String
  ): Mono<Unit>

  fun updateBundleStatus(
    id: String,
    active: Boolean,
    ownerId: String
  ): Mono<Bundle>

  fun findBundles(
    name: String?,
    minPrice: Long?,
    maxPrice: Long?,
    active: Boolean?,
    products: List<ProductType>?,
    sorting: ProductSorting?,
    ownerId: String? = null
  ): Flux<Bundle>

  fun reserveBundle(
    bundleId: String,
    userId: String,
    userName: String,
    reservationDate: LocalDate
  ): Mono<Bundle>

  fun findUserReservations(userId: String): Flux<Bundle>
}
