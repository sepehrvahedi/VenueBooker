package army.booker.domain.user.repositories

import army.booker.domain.user.Role
import army.booker.domain.user.User
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface UserRepository : ReactiveMongoRepository<User, String> {
  fun findByUsernameAndRole(username: String, role: Role): Mono<User>
  fun findByUsername(username: String): Mono<User>
}
