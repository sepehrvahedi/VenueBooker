package army.booker.domain.user

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "users")
data class User(
  @Id var id: String? = null,
  @Indexed val userName: String,
  val hashedPassword: String,
  @CreatedDate val createdAt: Instant? = null,
  @LastModifiedDate val updatedAt: Instant? = null,
)
