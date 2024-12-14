package army.booker.application.http.v1.user.validations.constraints

import army.booker.application.http.v1.user.validations.validators.LoginUserValidator
import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [LoginUserValidator::class])
@MustBeDocumented
annotation class LoginUserConstraint(
  val message: String = "{loginUser.validations.message}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = [],
)
