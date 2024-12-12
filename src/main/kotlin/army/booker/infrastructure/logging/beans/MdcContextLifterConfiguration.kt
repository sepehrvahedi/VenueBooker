package army.booker.infrastructure.logging.beans

import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators
import army.booker.infrastructure.logging.MdcContextLifter
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
class MdcContextLifterConfiguration {
  @PostConstruct
  fun contextOperatorHook() {
    Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
  }

  @PreDestroy
  fun cleanupHook() {
    Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY)
  }

  companion object {
    val MDC_CONTEXT_REACTOR_KEY: String = MdcContextLifterConfiguration::class.simpleName!!
  }
}
