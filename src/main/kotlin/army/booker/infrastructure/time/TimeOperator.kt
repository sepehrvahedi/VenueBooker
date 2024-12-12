package army.booker.infrastructure.time

import java.time.Instant
import java.util.concurrent.TimeUnit

interface TimeOperator {
  fun addToCurrentTime(offset: Long, timeUnit: TimeUnit): Instant
  fun getCurrentTime(): Instant
}
