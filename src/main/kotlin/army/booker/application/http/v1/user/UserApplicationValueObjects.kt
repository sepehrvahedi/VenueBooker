package army.booker.application.http.v1.user


import army.booker.application.http.v1.user.validations.constraints.CreateUserConstraint
import army.booker.application.http.v1.user.validations.constraints.LoginUserConstraint
import jakarta.validation.constraints.NotBlank

@CreateUserConstraint
data class CreateUserRequest(
  @field:NotBlank(message = "Username is required")
  val username: String,

  @field:NotBlank(message = "Password is required")
  val password: String,

  @field:NotBlank(message = "Role is required")
  val role: String,

  @field:NotBlank(message = "Name is required")
  val name: String,

  @field:NotBlank(message = "Surname is required")
  val surname: String,

  @field:NotBlank(message = "NationalNumber is required")
  val nationalNumber: String,

  @field:NotBlank(message = "Phone is required")
  val phone: String,
)

data class CreateUserResponse(
  val jwtToken: String? = null,
  val error: String? = null,
)

@LoginUserConstraint
data class LoginRequest(
  @field:NotBlank(message = "Username is required")
  val username: String,

  @field:NotBlank(message = "Password is required")
  val password: String,

  @field:NotBlank(message = "Role is required")
  val role: String,
)


data class LoginResponse(
  val jwtToken: String? = null,
  val error: String? = null,
)
