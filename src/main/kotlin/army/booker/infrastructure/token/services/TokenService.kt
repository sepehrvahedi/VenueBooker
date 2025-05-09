package army.booker.infrastructure.token.services

interface TokenService {
  fun createJwt(expiryDurationInSeconds: Long, subject: String?, key: String, valueObject: Any): String
  fun <T> parseJwt(token: String, key: String, clazz: Class<T>): T
}
