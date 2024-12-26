package army.booker.infrastructure.token

import army.booker.application.http.v1.user.controllers.UserController.Companion.USER_CLAIM_KEY
import army.booker.domain.user.UserTokenPayload
import army.booker.infrastructure.token.services.TokenService
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class TokenAuthenticationInterceptor(
  private val tokenService: TokenService,
) {

  private fun extractTokenFromHeader(authHeader: String?): String? {
    return authHeader?.let {
      if (it.startsWith("Bearer ", ignoreCase = true)) {
        it.substring(7)
      } else {
        it
      }
    }
  }

  fun extractAndValidateToken(authHeader: String?): Mono<UserTokenPayload> {
    val token = extractTokenFromHeader(authHeader)
    if (token.isNullOrEmpty()) {
      return Mono.error(UnauthorizedException("No token provided"))
    }

    return try {
      val payload = tokenService.parseJwt(
        token = token,
        key = USER_CLAIM_KEY,
        clazz = UserTokenPayload::class.java
      )

      if (payload.id.isEmpty()) {
        Mono.error(UnauthorizedException("Invalid token: missing user ID"))
      } else {
        Mono.just(payload)
      }
    } catch (e: Exception) {
      Mono.error(UnauthorizedException("Invalid token: ${e.message}"))
    }
  }

  companion object {
    const val AUTHORIZATION_HEADER = "Authorization"
  }
}


class UnauthorizedException(message: String) : RuntimeException(message)
