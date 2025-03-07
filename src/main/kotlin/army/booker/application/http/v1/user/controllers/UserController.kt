package army.booker.application.http.v1.user.controllers

import army.booker.application.http.v1.user.*
import army.booker.application.http.v1.user.mappers.HttpV1UserMapper
import army.booker.domain.user.Role
import army.booker.domain.user.User
import army.booker.domain.user.UserTokenPayload
import army.booker.domain.user.services.UserService
import army.booker.infrastructure.token.TokenAuthenticationInterceptor
import army.booker.infrastructure.token.TokenAuthenticationInterceptor.Companion.AUTHORIZATION_HEADER
import army.booker.infrastructure.token.services.TokenService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(
  origins = ["*"],
  allowedHeaders = ["*"],
  methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
  allowCredentials = "false"
)
class UserController(
  private val logger: Logger,
  private val userService: UserService,
  private val tokenService: TokenService,
  private val tokenAuthenticationInterceptor: TokenAuthenticationInterceptor,
) {
  companion object {
    private const val THREE_HOURS_IN_SECONDS = 3 * 60 * 60L
    const val USER_CLAIM_KEY = "user"
  }

  @PostMapping
  fun createUser(@Valid @RequestBody request: CreateUserRequest): Mono<ResponseEntity<CreateUserResponse>> =
    userService.createUser(
      username = request.username,
      password = request.password,
      name = request.name,
      surname = request.surname,
      phone = request.phone,
      nationalNumber = request.nationalNumber,
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

  @PostMapping("/admin/login")
  fun adminLogin(@Valid @RequestBody request: LoginRequest): Mono<ResponseEntity<LoginResponse>> =
    if (request.username == "admin" && request.password == "admin") {
      userService.authenticateUser(
        username = "admin",
        password = "admin",
        role = Role.Admin
      ).map { user ->
        val jwtToken = createJwtToken(user)
        logger.info("Successfully authenticated admin")
        ResponseEntity.ok(LoginResponse(jwtToken))
      }
    } else {
      Mono.just(ResponseEntity.badRequest().body(LoginResponse(error = "Invalid admin credentials")))
    }.onErrorResume { error ->
      logger.error("Failed to authenticate admin", error)
      Mono.just(
        ResponseEntity.internalServerError().body(LoginResponse(error = "Internal server error"))
      )
    }

  @PostMapping("/admin/shadow")
  fun adminShadowUser(
    @Valid @RequestBody request: AdminShadowUserRequest,
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?
  ): Mono<ResponseEntity<AdminShadowUserResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap { tokenPayload ->
        // Verify admin role
        if (tokenPayload.role != Role.Admin) {
          return@flatMap Mono.just(
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(
              AdminShadowUserResponse(valid = false, error = "Unauthorized access")
            )
          )
        }

        userService.findUserByUsername(request.username)
          .map { user ->
            // Create a shadow token for the user
            val shadowToken = createJwtToken(user)

            ResponseEntity.ok(
              AdminShadowUserResponse(
                valid = true,
                role = user.role.roleName,
                userId = user.id,
                token = shadowToken
              )
            )
          }
          .switchIfEmpty(
            Mono.just(
              ResponseEntity.ok(
                AdminShadowUserResponse(
                  valid = false,
                  error = "User not found"
                )
              )
            )
          )
      }
      .onErrorResume { error ->
        logger.error("Failed to shadow user: ${request.username}", error)
        Mono.just(
          ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            AdminShadowUserResponse(valid = false, error = "Internal server error")
          )
        )
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
        name = user.name,
        surname = user.surname
      )
    )
}
