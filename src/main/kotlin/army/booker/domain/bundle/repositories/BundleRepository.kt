package army.booker.domain.bundle.repositories

import army.booker.domain.bundle.Bundle
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BundleRepository : ReactiveMongoRepository<Bundle, String> {
  fun findByName(name: String): Mono<Bundle>
}
