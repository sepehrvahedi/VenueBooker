package army.booker.application.http.v1.user.controllers

import army.booker.application.http.v1.user.mappers.HttpV1UserMapper
import army.booker.application.http.v1.user.CreateUserRequest
import army.booker.application.http.v1.user.CreateUserResponse
import army.booker.application.http.v1.user.LoginRequest
import army.booker.application.http.v1.user.LoginResponse
import army.booker.domain.user.User
import army.booker.domain.user.UserTokenPayload
import army.booker.domain.user.services.UserService
import army.booker.infrastructure.token.services.TokenService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(
  origins = ["http://localhost:63342"],
  allowedHeaders = ["*"],
  methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
  allowCredentials = "true"
)
class UserController(
  private val logger: Logger,
  private val userService: UserService,
  private val tokenService: TokenService,
) {
  companion object {
    private const val THREE_HOURS_IN_SECONDS = 3 * 60 * 60L
    private const val USER_CLAIM_KEY = "user"
  }

  @PostMapping
  fun createUser(@Valid @RequestBody request: CreateUserRequest): Mono<ResponseEntity<CreateUserResponse>> =
    userService.createUser(
      username = request.username,
      password = request.password,
      role = HttpV1UserMapper.UserRoleMapper.fromString(request.role)
    ).map { user ->
      val jwtToken = createJwtToken(user)
      logger.info("Successfully created user with username: ${request.username}")
      ResponseEntity.ok(CreateUserResponse(jwtToken))
    }.onErrorResume { error ->
      logger.error("Failed to create user with username: ${request.username}", error)
      when (error) {
        is IllegalArgumentException -> Mono.just(
          ResponseEntity.badRequest().body(CreateUserResponse(error = "User already exists"))
        )

        is IllegalStateException -> Mono.just(
          ResponseEntity.badRequest().body(CreateUserResponse(error = "Invalid role provided"))
        )

        else -> Mono.just(
          ResponseEntity.internalServerError().body(CreateUserResponse(error = "Internal server error"))
        )
      }
    }

  @PostMapping("/login")
  fun login(@Valid @RequestBody request: LoginRequest): Mono<ResponseEntity<LoginResponse>> =
    userService.authenticateUser(
      username = request.username,
      password = request.password,
      role = HttpV1UserMapper.UserRoleMapper.fromString(request.role)
    ).map { user ->
      val jwtToken = createJwtToken(user)
      logger.info("Successfully authenticated user: ${request.username}")
      ResponseEntity.ok(LoginResponse(jwtToken))
    }.onErrorResume { error ->
      logger.error("Failed to authenticate user: ${request.username}", error)
      when (error) {
        is IllegalArgumentException -> Mono.just(
          ResponseEntity.badRequest().body(LoginResponse(error = "Invalid credentials"))
        )

        else -> Mono.just(
          ResponseEntity.internalServerError().body(LoginResponse(error = "Internal server error"))
        )
      }
    }

  private fun createJwtToken(user: User): String =
    tokenService.createJwt(
      expiryDurationInSeconds = THREE_HOURS_IN_SECONDS,
      subject = user.id.toString(),
      key = USER_CLAIM_KEY,
      valueObject = UserTokenPayload(
        id = user.id!!,
        username = user.username,
        role = user.role,
      )
    )
}
