package army.booker.domain.bundle

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.time.LocalDate

@Document(collection = "bundles")
data class Bundle(
  @Id
  var id: String? = null,

  @Indexed(unique = true)
  val name: String,

  val price: Long,

  val products: List<ProductType>,

  val active: Boolean = true,

  @Indexed
  val ownerId: String,

  val reservations: List<DailyReservation> = emptyList(),

  @Version
  val version: Long = 0,

  @CreatedDate
  val createdAt: Instant? = null,

  @LastModifiedDate
  val updatedAt: Instant? = null,
)

data class DailyReservation(
  val userId: String,
  val userName: String,
  val reservationDate: LocalDate,
  val createdAt: Instant = Instant.now()
)
