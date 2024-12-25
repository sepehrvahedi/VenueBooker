package army.booker.domain.user.services

import army.booker.domain.user.Role
import army.booker.domain.user.User
import reactor.core.publisher.Mono

interface UserService {
  fun authenticateUser(username: String, password: String, role: Role): Mono<User>
  fun createUser(
    username: String,
    password: String,
    role: Role,
    name: String,
    surname: String,
    nationalNumber: String,
    phone: String,
  ): Mono<User>
}
