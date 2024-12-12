package army.booker.infrastructure.logging

import org.reactivestreams.Subscription
import reactor.core.CoreSubscriber
import reactor.util.context.Context

class MdcContextLifter<T>(private val coreSubscriber: CoreSubscriber<T>) : CoreSubscriber<T> {

  override fun onNext(t: T) {
    coreSubscriber.currentContext().copyToMdc()
    coreSubscriber.onNext(t)
  }

  override fun onSubscribe(subscription: Subscription) {
    coreSubscriber.currentContext().copyToMdc()
    coreSubscriber.onSubscribe(subscription)
  }

  override fun onComplete() {
    coreSubscriber.currentContext().copyToMdc()
    coreSubscriber.onComplete()
  }

  override fun onError(throwable: Throwable?) {
    coreSubscriber.currentContext().copyToMdc()
    coreSubscriber.onError(throwable)
  }

  override fun currentContext(): Context = coreSubscriber.currentContext()

  companion object {
    private fun Context.copyToMdc() {
      if (this.isEmpty) {
        return
      }
    }
  }
}
