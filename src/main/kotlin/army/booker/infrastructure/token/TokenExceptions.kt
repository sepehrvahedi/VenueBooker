package army.booker.infrastructure.token

import army.booker.infrastructure.BookerException
import army.booker.infrastructure.ErrorCodeString

sealed class TokenException(message: String) : BookerException(message) {
  class InvalidTokenException private constructor(message: String, val token: String, val userId: String) :
    TokenException(message) {
    private var errorCode: ErrorCodeString = ErrorCodeString.UnknownError

    override fun getErrorCodeString(): ErrorCodeString = errorCode

    companion object {
      fun createCorruptedTokenException(token: String, userId: String = ""): InvalidTokenException =
        InvalidTokenException("CORRUPTED_TOKEN", token, userId).apply {
          errorCode = ErrorCodeString.CorruptedToken
        }

      fun createExpiredTokenException(token: String, userId: String = ""): InvalidTokenException =
        InvalidTokenException("TOKEN_EXPIRED", token, userId).apply {
          errorCode = ErrorCodeString.TokenExpired
        }
    }
  }
}
