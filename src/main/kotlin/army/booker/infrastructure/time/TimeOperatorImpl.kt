package army.booker.infrastructure.time

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.TimeUnit

@Component
class TimeOperatorImpl : TimeOperator {
  override fun addToCurrentTime(offset: Long, timeUnit: TimeUnit): Instant =
    Instant.now().plusMillis(timeUnit.toMillis(offset))

  override fun getCurrentTime(): Instant = Instant.now()
}
