package army.booker.domain.user.services

import army.booker.domain.user.User
import reactor.core.publisher.Mono

interface UserService {
  fun createUser(userName: String, password: String): Mono<User>
}
