package army.booker.application.http.v1.user.validations.validators

import army.booker.application.http.v1.user.validations.constraints.LoginUserConstraint
import army.booker.application.http.v1.user.LoginRequest
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class LoginUserValidator :
  ConstraintValidator<LoginUserConstraint, LoginRequest> {
  override fun isValid(
    value: LoginRequest,
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

      value.username != "admin" && value.password.length < 8 -> {
        context.buildConstraintViolationWithTemplate("Password must be at least 8 characters long")
          .addPropertyNode("password")
          .addConstraintViolation()
        isValid = false
      }

      value.username != "admin" && value.password.length > 100 -> {
        context.buildConstraintViolationWithTemplate("Password cannot be longer than 100 characters")
          .addPropertyNode("password")
          .addConstraintViolation()
        isValid = false
      }
    }

    // Role validation
    when (value.role) {
      "SUPPLIER",
      "CUSTOMER",
      "ADMIN",
      -> {
        // Valid roles, do nothing
      }

      else -> {
        context.buildConstraintViolationWithTemplate("Role must be either SUPPLIER or CUSTOMER or ADMIN")
          .addPropertyNode("role")
          .addConstraintViolation()
        isValid = false
      }
    }

    return isValid
  }
}
