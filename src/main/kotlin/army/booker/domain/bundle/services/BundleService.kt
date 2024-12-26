package army.booker.domain.bundle.services

import army.booker.domain.bundle.Bundle
import army.booker.domain.bundle.ProductSorting
import army.booker.domain.bundle.ProductType
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
    ownerId: String
  ): Flux<Bundle>
}
