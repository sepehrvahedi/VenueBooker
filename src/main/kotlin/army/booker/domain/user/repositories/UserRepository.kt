package army.booker.domain.user.repositories

import army.booker.domain.user.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<User, String> {
  fun findByUserName(userName: String): Mono<User>
}
