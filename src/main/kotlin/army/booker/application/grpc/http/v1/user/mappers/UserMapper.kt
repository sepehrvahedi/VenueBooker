package army.booker.application.grpc.http.v1.user.mappers

import army.booker.domain.user.Role
import army.booker.grpc.provides.http.v1.user.UserServiceProto

object HttpV1UserMapper {
  object UserRoleMapper {
    fun fromProto(proto: UserServiceProto.UserRole): Role = when (proto) {
      UserServiceProto.UserRole.SUPPLIER -> Role.Supplier
      UserServiceProto.UserRole.CUSTOMER -> Role.Customer
      else -> error("Contact type is not valid")
    }
  }
}
