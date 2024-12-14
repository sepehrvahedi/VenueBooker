package army.booker.application.http.v1.user.validations.constraints

import army.booker.application.http.v1.user.validations.validators.CreateUserValidator
import jakarta.validation.Constraint
import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CreateUserValidator::class])
@MustBeDocumented
annotation class CreateUserConstraint(
  val message: String = "{createUser.validations.message}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = [],
)
