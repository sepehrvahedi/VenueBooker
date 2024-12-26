package army.booker.domain.bundle

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

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

  @CreatedDate
  val createdAt: Instant? = null,

  @LastModifiedDate
  val updatedAt: Instant? = null,
)
