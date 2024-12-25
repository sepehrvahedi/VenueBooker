package army.booker.domain.user.services

import army.booker.domain.user.Role
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
  override fun createUser(
    username: String,
    password: String,
    role: Role,
    name: String,
    surname: String,
    nationalNumber: String,
    phone: String,
  ): Mono<User> =
    userRepository.findByUsernameAndRole(username, role)
      .flatMap<User> {
        logger.error("User with username $username already exists")
        Mono.error(IllegalArgumentException("Username already exists"))
      }
      .switchIfEmpty(
        Mono.defer {
          val hashedPassword = passwordEncoder.encode(password)
          val newUser = User(
            username = username,
            role = role,
            hashedPassword = hashedPassword,
            name = name,
            surname = surname,
            nationalNumber = nationalNumber,
            phone = phone,
          )

          userRepository.save(newUser)
            .doOnSuccess { logger.info("Created new user with username: $username") }
            .doOnError { error ->
              logger.error("Failed to create user with username: $username", error)
            }
        }
      )

  override fun authenticateUser(username: String, password: String, role: Role): Mono<User> =
    userRepository.findByUsernameAndRole(username, role)
      .filter { user -> passwordEncoder.matches(password, user.hashedPassword) }
      .switchIfEmpty(
        Mono.error(
          IllegalArgumentException("Invalid credentials")
        )
      )
}
