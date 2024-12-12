package army.booker.application.grpc.http.v1.user.validations.validators

import army.booker.application.grpc.http.v1.user.validations.constraints.LoginUserConstraint
import army.booker.grpc.provides.http.v1.user.UserServiceProto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class LoginUserValidator :
  ConstraintValidator<LoginUserConstraint, UserServiceProto.LoginRequest> {
  override fun isValid(
    value: UserServiceProto.LoginRequest,
    context: ConstraintValidatorContext,
  ): Boolean {
    var isValid = true

    // Username validation
    when {
      value.username.isBlank() -> {
        context.buildConstraintViolationWithTemplate("Username cannot be empty")
          .addPropertyNode("username")
          .addConstraintViolation()
        isValid = false
      }

      value.username.length < 3 -> {
        context.buildConstraintViolationWithTemplate("Username must be at least 3 characters long")
          .addPropertyNode("username")
          .addConstraintViolation()
        isValid = false
      }

      value.username.length > 50 -> {
        context.buildConstraintViolationWithTemplate("Username cannot be longer than 50 characters")
          .addPropertyNode("username")
          .addConstraintViolation()
        isValid = false
      }

      !value.username.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> {
        context.buildConstraintViolationWithTemplate("Username can only contain letters, numbers, dots, dashes and underscores")
          .addPropertyNode("username")
          .addConstraintViolation()
        isValid = false
      }
    }

    // Password validation
    when {
      value.password.isBlank() -> {
        context.buildConstraintViolationWithTemplate("Password cannot be empty")
          .addPropertyNode("password")
          .addConstraintViolation()
        isValid = false
      }

      value.password.length < 8 -> {
        context.buildConstraintViolationWithTemplate("Password must be at least 8 characters long")
          .addPropertyNode("password")
          .addConstraintViolation()
        isValid = false
      }

      value.password.length > 100 -> {
        context.buildConstraintViolationWithTemplate("Password cannot be longer than 100 characters")
          .addPropertyNode("password")
          .addConstraintViolation()
        isValid = false
      }
    }

    // Role validation
    when (value.role) {
      UserServiceProto.UserRole.NONE_USER_ROLE,
      UserServiceProto.UserRole.UNRECOGNIZED,
      -> {
        context.buildConstraintViolationWithTemplate("Role must be either SUPPLIER or CUSTOMER")
          .addPropertyNode("role")
          .addConstraintViolation()
        isValid = false
      }

      UserServiceProto.UserRole.SUPPLIER,
      UserServiceProto.UserRole.CUSTOMER,
      -> {
        // Valid roles, do nothing
      }
    }

    return isValid
  }
}
