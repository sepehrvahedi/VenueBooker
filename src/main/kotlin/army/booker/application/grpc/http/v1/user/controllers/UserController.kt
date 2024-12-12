package army.booker.application.grpc.http.v1.user.controllers

import army.booker.domain.user.services.UserService
import army.booker.grpc.provides.http.v1.user.ReactorUserServiceGrpc
import army.booker.grpc.provides.http.v1.user.UserServiceProto
import io.grpc.Status
import org.lognet.springboot.grpc.GRpcService
import org.slf4j.Logger
import reactor.core.publisher.Mono

@GRpcService
class UserController(
  private val logger: Logger,
  private val userService: UserService,
) : ReactorUserServiceGrpc.UserServiceImplBase() {

  override fun createUser(request: UserServiceProto.CreateUserRequest): Mono<UserServiceProto.Empty> =
    userService.createUser(request.username, request.password)
      .map {
        logger.info("Successfully created user with username: ${request.username}")
        UserServiceProto.Empty.getDefaultInstance()
      }
      .onErrorMap { error ->
        logger.error("Failed to create user with username: ${request.username}", error)
        when (error) {
          is IllegalArgumentException -> Status.ALREADY_EXISTS
            .withDescription("User already exists")
            .asRuntimeException()

          else -> Status.INTERNAL
            .withDescription("Internal server error")
            .asRuntimeException()
        }
      }
}
