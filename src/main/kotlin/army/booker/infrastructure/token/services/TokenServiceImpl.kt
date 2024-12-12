package army.booker.infrastructure.token.services

import army.booker.infrastructure.AppConfig
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.jackson.io.JacksonSerializer
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.springframework.stereotype.Service
import army.booker.infrastructure.token.TokenException
import army.booker.infrastructure.time.TimeOperator
import army.booker.infrastructure.time.toDate
import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

@Service
class TokenServiceImpl(
  private val timeOperator: TimeOperator,
  private val logger: Logger,
  appConfig: AppConfig,
  private val objectMapper: ObjectMapper,
) : TokenService {
  private val secretKey: SecretKey =
    Keys.hmacShaKeyFor(Base64.getDecoder().decode(appConfig.security.token.jwt.secretKey))
  private val jwtParser = Jwts.parser().verifyWith(secretKey).build()

  override fun createJwt(
    expiryDurationInSeconds: Long,
    subject: String?,
    key: String,
    valueObject: Any,
  ): String {
    val builder = Jwts.builder()
      .json(JacksonSerializer(objectMapper))
      .claim(key, valueObject)
      .issuedAt(timeOperator.getCurrentTime().toDate())
      .expiration(
        timeOperator.addToCurrentTime(
          offset = expiryDurationInSeconds,
          timeUnit = TimeUnit.SECONDS,
        ).toDate(),
      )
      .signWith(secretKey)
    subject?.let {
      builder.subject(it)
    }
    return builder.compact()
  }

  override fun <T> parseJwt(token: String, key: String, clazz: Class<T>): T = run {
    try {
      val claims = jwtParser
        .parseSignedClaims(token).payload
      return@run objectMapper.convertValue(claims[key], clazz)
    } catch (e: ExpiredJwtException) {
      logger.error("Expired token: $token", e)
      throw TokenException.InvalidTokenException.createExpiredTokenException(token)
    } catch (e: UnsupportedJwtException) {
      logger.error("Invalid token: $token", e)
      throw TokenException.InvalidTokenException.createCorruptedTokenException(token)
    } catch (e: JwtException) {
      logger.error("Invalid token: $token", e)
      throw TokenException.InvalidTokenException.createCorruptedTokenException(token)
    } catch (e: IllegalArgumentException) {
      logger.error("Invalid token: $token", e)
      throw TokenException.InvalidTokenException.createCorruptedTokenException(token)
    }
  }
}
