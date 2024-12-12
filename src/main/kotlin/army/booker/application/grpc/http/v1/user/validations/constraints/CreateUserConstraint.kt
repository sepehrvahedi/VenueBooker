package army.booker.application.grpc.http.v1.user.validations.constraints

import jakarta.validation.Constraint
import army.booker.application.grpc.http.v1.user.validations.validators.CreateUserValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CreateUserValidator::class])
@MustBeDocumented
annotation class CreateUserConstraint(
  val message: String = "{createUser.validations.message}",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Any>> = [],
)
