package army.booker.application.grpc.http.v1.user.controllers

import army.booker.application.grpc.http.v1.user.mappers.HttpV1UserMapper
import army.booker.domain.user.User
import army.booker.domain.user.UserTokenPayload
import army.booker.domain.user.services.UserService
import army.booker.grpc.provides.http.v1.user.ReactorUserServiceGrpc
import army.booker.grpc.provides.http.v1.user.UserServiceProto
import army.booker.infrastructure.token.services.TokenService
import io.grpc.Status
import org.lognet.springboot.grpc.GRpcService
import org.slf4j.Logger
import reactor.core.publisher.Mono

@GRpcService
class UserController(
  private val logger: Logger,
  private val userService: UserService,
  private val tokenService: TokenService,
) : ReactorUserServiceGrpc.UserServiceImplBase() {

  companion object {
    private const val THREE_HOURS_IN_SECONDS = 3 * 60 * 60L // 3 hours in seconds
    private const val USER_CLAIM_KEY = "user"
  }

  override fun createUser(
    request: UserServiceProto.CreateUserRequest,
  ): Mono<UserServiceProto.CreateUserResponse> =
    userService.createUser(
      username = request.username,
      password = request.password,
      role = HttpV1UserMapper.UserRoleMapper.fromProto(request.role)
    ).map { user ->
      val jwtToken = createJwtToken(user)
      logger.info("Successfully created user with username: ${request.username}")

      UserServiceProto.CreateUserResponse.newBuilder()
        .setJwtToken(jwtToken)
        .build()
    }.onErrorMap { error ->
      logger.error("Failed to create user with username: ${request.username}", error)
      when (error) {
        is IllegalArgumentException -> Status.ALREADY_EXISTS
          .withDescription("User already exists")
          .asRuntimeException()

        is IllegalStateException -> Status.INVALID_ARGUMENT
          .withDescription("Invalid role provided")
          .asRuntimeException()

        else -> Status.INTERNAL
          .withDescription("Internal server error")
          .asRuntimeException()
      }
    }

  override fun login(request: UserServiceProto.LoginRequest): Mono<UserServiceProto.LoginResponse> =
    userService.authenticateUser(
      username = request.username,
      password = request.password,
      role = HttpV1UserMapper.UserRoleMapper.fromProto(request.role)
    ).map { user ->
      val jwtToken = createJwtToken(user)
      logger.info("Successfully authenticated user: ${request.username}")

      UserServiceProto.LoginResponse.newBuilder()
        .setJwtToken(jwtToken)
        .build()
    }.onErrorMap { error ->
      logger.error("Failed to authenticate user: ${request.username}", error)
      when (error) {
        is IllegalArgumentException -> Status.INVALID_ARGUMENT
          .withDescription("Invalid credentials")
          .asRuntimeException()

        else -> Status.INTERNAL
          .withDescription("Internal server error")
          .asRuntimeException()
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
