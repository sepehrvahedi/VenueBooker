package army.booker.domain.user.services

import army.booker.FixtureTestHelper
import army.booker.domain.user.Role
import army.booker.domain.user.User
import army.booker.domain.user.repositories.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserServiceTest {
  private lateinit var userService: UserServiceImpl

  @Mock
  private lateinit var userRepository: UserRepository

  @Mock
  private lateinit var logger: Logger

  @Mock
  private lateinit var passwordEncoder: BCryptPasswordEncoder

  private val fixture = FixtureTestHelper.getDefaultFixture()

  @BeforeEach
  fun init() {
    MockitoAnnotations.openMocks(this)
    userService = UserServiceImpl(userRepository, logger, passwordEncoder)
  }

  @Test
  fun `createUser should create new user when username doesn't exist`() {
    // Arrange
    val username = "testUser"
    val password = "testPass123!"
    val hashedPassword = "hashedPassword"
    val role = Role.Supplier
    val expectedUser = User(username = username, hashedPassword = hashedPassword, role = role)

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.empty())
    whenever(passwordEncoder.encode(password)).thenReturn(hashedPassword)
    whenever(userRepository.save(any())).thenReturn(Mono.just(expectedUser))

    // Act & Assert
    StepVerifier.create(userService.createUser(username, password, role))
      .expectNext(expectedUser)
      .verifyComplete()
  }

  @Test
  fun `createUser should throw exception when username already exists`() {
    // Arrange
    val username = "existingUser"
    val password = "testPass123!"
    val role = Role.Supplier
    val existingUser = fixture<User>()

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.just(existingUser))

    // Act & Assert
    StepVerifier.create(userService.createUser(username, password, role))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }

  @Test
  fun `createUser should handle repository save error`() {
    // Arrange
    val username = "testUser"
    val password = "testPass123!"
    val hashedPassword = "hashedPassword"
    val role = Role.Supplier

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.empty())
    whenever(passwordEncoder.encode(password)).thenReturn(hashedPassword)
    whenever(userRepository.save(any())).thenReturn(Mono.error(RuntimeException("Database error")))

    // Act & Assert
    StepVerifier.create(userService.createUser(username, password, role))
      .expectError(RuntimeException::class.java)
      .verify()
  }

  @Test
  fun `authenticateUser should return user when credentials are valid`() {
    // Arrange
    val username = "testUser"
    val password = "testPass123!"
    val hashedPassword = "hashedPassword"
    val role = Role.Supplier
    val existingUser = User(username = username, hashedPassword = hashedPassword, role = role)

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.just(existingUser))
    whenever(passwordEncoder.matches(password, hashedPassword)).thenReturn(true)

    // Act & Assert
    StepVerifier.create(userService.authenticateUser(username, password, role))
      .expectNext(existingUser)
      .verifyComplete()
  }

  @Test
  fun `authenticateUser should throw exception when user not found`() {
    // Arrange
    val username = "nonexistentUser"
    val password = "testPass123!"
    val role = Role.Supplier

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.empty())

    // Act & Assert
    StepVerifier.create(userService.authenticateUser(username, password, role))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }

  @Test
  fun `authenticateUser should throw exception when password is incorrect`() {
    // Arrange
    val username = "testUser"
    val password = "wrongPassword"
    val hashedPassword = "hashedPassword"
    val role = Role.Supplier
    val existingUser = User(username = username, hashedPassword = hashedPassword, role = role)

    whenever(userRepository.findByUsernameAndRole(username, role)).thenReturn(Mono.just(existingUser))
    whenever(passwordEncoder.matches(password, hashedPassword)).thenReturn(false)

    // Act & Assert
    StepVerifier.create(userService.authenticateUser(username, password, role))
      .expectError(IllegalArgumentException::class.java)
      .verify()
  }

  @Test
  fun `authenticateUser should handle repository error`() {
    // Arrange
    val username = "testUser"
    val password = "testPass123!"
    val role = Role.Supplier

    whenever(userRepository.findByUsernameAndRole(username, role))
      .thenReturn(Mono.error(RuntimeException("Database error")))

    // Act & Assert
    StepVerifier.create(userService.authenticateUser(username, password, role))
      .expectError(RuntimeException::class.java)
      .verify()
  }
}
