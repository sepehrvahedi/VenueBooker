package army.booker.domain.user.services

import army.booker.domain.user.User
import army.booker.domain.user.repositories.UserRepository
import org.slf4j.Logger
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserServiceImpl(
  private val userRepository: UserRepository,
  private val logger: Logger,
  private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder(),
) : UserService {
  override fun createUser(userName: String, password: String): Mono<User> =
    userRepository.findByUserName(userName)
      .flatMap<User> {
        logger.error("User with username $userName already exists")
        Mono.error(IllegalArgumentException("Username already exists"))
      }
      .switchIfEmpty(
        Mono.defer {
          val hashedPassword = passwordEncoder.encode(password)
          val newUser = User(
            userName = userName,
            hashedPassword = hashedPassword
          )

          userRepository.save(newUser)
            .doOnSuccess { logger.info("Created new user with username: $userName") }
            .doOnError { error ->
              logger.error("Failed to create user with username: $userName", error)
            }
        }
      )
}
