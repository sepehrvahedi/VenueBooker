package army.booker.application.grpc.http.v1.user.validations.validators

import army.booker.grpc.provides.http.v1.user.UserServiceProto
import jakarta.validation.ConstraintValidatorContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class CreateUserValidatorTest {
  private lateinit var validator: CreateUserValidator

  @Mock
  private lateinit var context: ConstraintValidatorContext

  @Mock
  private lateinit var builder: ConstraintValidatorContext.ConstraintViolationBuilder

  @BeforeEach
  fun setup() {
    MockitoAnnotations.openMocks(this)
    validator = CreateUserValidator()
    whenever(context.buildConstraintViolationWithTemplate(any())).thenReturn(builder)
    whenever(builder.addConstraintViolation()).thenReturn(context)
  }

  @Test
  fun `valid username and password should pass validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser123")
      .setPassword("ValidPass1!")
      .build()

    assertTrue(validator.isValid(request, context))
  }

  @Test
  fun `empty username should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("")
      .setPassword("ValidPass1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `username shorter than 3 characters should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("ab")
      .setPassword("ValidPass1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `username longer than 50 characters should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("a".repeat(51))
      .setPassword("ValidPass1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `username with invalid characters should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("user@name")
      .setPassword("ValidPass1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `empty password should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password shorter than 8 characters should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("Pass1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password longer than 100 characters should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("A" + "a".repeat(98) + "1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password without uppercase letter should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("password1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password without lowercase letter should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("PASSWORD1!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password without number should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("Password!")
      .build()

    assertFalse(validator.isValid(request, context))
  }

  @Test
  fun `password without special character should fail validation`() {
    val request = UserServiceProto.CreateUserRequest.newBuilder()
      .setUsername("validUser")
      .setPassword("Password1")
      .build()

    assertFalse(validator.isValid(request, context))
  }
}
