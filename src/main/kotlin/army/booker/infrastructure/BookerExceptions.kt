package army.booker.infrastructure

import com.google.rpc.ErrorInfo
import io.grpc.Status

open class BookerException(message: String?) : Exception(message) {
  open fun getErrorCodeString(): ErrorCodeString = ErrorCodeString.UnknownError

  open fun asStatus(): Status = Status.FAILED_PRECONDITION

  open fun getErrorInfo(): ErrorInfo.Builder? = null
}

enum class ErrorCodeString(val codeString: String = "UNKNOWN_ERROR") {
  UnknownError,
  FieldPreConditionFailed("BOOKER_%s_FIELD_PRE_CONDITION_FAILED"),
  UserNotFound("BOOKER_USER_NOT_FOUND"),
}
