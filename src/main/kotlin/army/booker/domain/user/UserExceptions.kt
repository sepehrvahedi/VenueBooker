package army.booker.domain.user

import army.booker.infrastructure.BookerException
import army.booker.infrastructure.ErrorCodeString
import io.grpc.Status

sealed class UserException(message: String) : BookerException(message) {
  abstract override fun getErrorCodeString(): ErrorCodeString

  class UserNotFoundException private constructor(message: String) : UserException(message) {
    override fun getErrorCodeString(): ErrorCodeString = ErrorCodeString.UserNotFound
    override fun asStatus(): Status = Status.NOT_FOUND

    companion object {
      fun create(userName: String): UserNotFoundException =
        UserNotFoundException("Cannot find user with username: $userName")
    }
  }
}
