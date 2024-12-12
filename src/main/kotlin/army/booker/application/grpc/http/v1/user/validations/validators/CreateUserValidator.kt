package army.booker.application.grpc.http.v1.user.validations.validators

import army.booker.application.grpc.http.v1.user.validations.constraints.CreateUserConstraint
import army.booker.grpc.provides.http.v1.user.UserServiceProto
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CreateUserValidator :
  ConstraintValidator<CreateUserConstraint, UserServiceProto.CreateUserRequest> {
  override fun isValid(
    value: UserServiceProto.CreateUserRequest,
    context: ConstraintValidatorContext,
  ): Boolean = when {
    value.username.isBlank() -> {
      context.buildConstraintViolationWithTemplate("Username cannot be empty")
        .addConstraintViolation()
      false
    }
    value.username.length < 3 -> {
      context.buildConstraintViolationWithTemplate("Username must be at least 3 characters long")
        .addConstraintViolation()
      false
    }
    value.username.length > 50 -> {
      context.buildConstraintViolationWithTemplate("Username cannot be longer than 50 characters")
        .addConstraintViolation()
      false
    }
    !value.username.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> {
      context.buildConstraintViolationWithTemplate("Username can only contain letters, numbers, dots, dashes and underscores")
        .addConstraintViolation()
      false
    }
    value.password.isBlank() -> {
      context.buildConstraintViolationWithTemplate("Password cannot be empty")
        .addConstraintViolation()
      false
    }
    value.password.length < 8 -> {
      context.buildConstraintViolationWithTemplate("Password must be at least 8 characters long")
        .addConstraintViolation()
      false
    }
    value.password.length > 100 -> {
      context.buildConstraintViolationWithTemplate("Password cannot be longer than 100 characters")
        .addConstraintViolation()
      false
    }
    !value.password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]+$")) -> {
      context.buildConstraintViolationWithTemplate("Password must contain at least one uppercase letter, one lowercase letter, one number and one special character")
        .addConstraintViolation()
      false
    }
    else -> true
  }
}
