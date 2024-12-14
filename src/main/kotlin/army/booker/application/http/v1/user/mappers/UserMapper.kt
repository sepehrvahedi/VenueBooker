package army.booker.application.http.v1.user.mappers

import army.booker.domain.user.Role

object HttpV1UserMapper {
  object UserRoleMapper {
    fun fromString(role: String): Role = when (role) {
      "SUPPLIER" -> Role.Supplier
      "CUSTOMER" -> Role.Customer
      else -> error("Contact type is not valid")
    }
  }
}
