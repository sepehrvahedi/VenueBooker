package army.booker.application

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.support.WebExchangeBindException
import reactor.core.publisher.Mono

@ControllerAdvice
class ValidationExceptionHandler {
  @ExceptionHandler(WebExchangeBindException::class)
  fun handleValidationErrors(ex: WebExchangeBindException): Mono<ResponseEntity<Map<String, List<String>>>> {
    val errors = ex.bindingResult.fieldErrors.groupBy(
      { it.field },
      { it.defaultMessage ?: "Invalid value" }
    )
    return Mono.just(ResponseEntity.badRequest().body(errors))
  }
}
