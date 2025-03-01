package army.booker.domain.user

data class UserTokenPayload(
  val id: String,
  val username: String,
  val name: String,
  val surname: String,
  val role: Role,
)

enum class Role(val roleName: String) {
  Supplier("SUPPLIER"),
  Customer("CUSTOMER"),
  Admin("ADMIN"),
  ;
}
