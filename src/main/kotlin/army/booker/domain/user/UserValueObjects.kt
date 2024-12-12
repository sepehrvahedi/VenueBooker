package army.booker.domain.user

data class UserTokenPayload(
  val id: String,
  val username: String,
  val role: Role,
)

enum class Role(val roleName: String) {
  Supplier("SUPPLIER"),
  Customer("CUSTOMER"),
  ;
}
