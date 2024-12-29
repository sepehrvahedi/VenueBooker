package army.booker.application.http.v1.bundle

import army.booker.domain.bundle.ProductSorting
import army.booker.domain.bundle.ProductType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateBundleRequest(
  @field:NotBlank
  val name: String,
  @field:Min(0)
  val price: Long,
  val products: List<ProductType>,
)

data class FindBundlesRequest(
  val name: String?,
  @field:Min(0)
  val minPrice: Long?,
  @field:Min(0)
  val maxPrice: Long?,
  val active: Boolean?,
  val products: List<ProductType>?,
  val sorting: ProductSorting?,
)


data class CreateBundleResponse(
  val id: String? = null,
  val name: String? = null,
  val price: Long? = null,
  val products: List<ProductType>? = null,
  val error: String? = null,
)

data class EditBundleRequest(
  @field:NotNull(message = "Bundle ID is required")
  val ids: List<String>,

  val price: Long?,
  val products: List<ProductType>?,
)

data class EditBundleResponse(
  val error: String? = null,
)

data class UpdateBundleStatusRequest(
  @field:NotNull(message = "Bundle ID is required")
  val id: String,

  @field:NotNull(message = "Status is required")
  val active: Boolean,
)

data class UpdateBundleStatusResponse(
  val id: String? = null,
  val active: Boolean? = null,
  val error: String? = null,
)

data class BundleDTO(
  val id: String,
  val name: String,
  val price: Long,
  val products: List<ProductType>,
  val active: Boolean,
)

data class FindBundlesResponse(
  val bundles: List<BundleDTO>? = null,
  val error: String? = null,
)
