package army.booker.infrastructure.config

import army.booker.domain.user.Role
import army.booker.domain.user.User
import army.booker.domain.user.repositories.UserRepository
import org.slf4j.Logger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono

@Configuration
class DatabaseInitializer(
    private val userRepository: UserRepository,
    private val logger: Logger,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    @Bean
    fun initDatabase(): CommandLineRunner = CommandLineRunner {
        createAdminUserIfNotExists()
            .subscribe(
                { logger.info("Admin user initialized successfully") },
                { error -> logger.error("Failed to initialize admin user", error) }
            )
    }

    private fun createAdminUserIfNotExists(): Mono<User> {
        return userRepository.findByUsernameAndRole("admin", Role.Admin)
            .switchIfEmpty(
                Mono.defer {
                    logger.info("Admin user not found, creating...")
                    val adminUser = User(
                        username = "admin",
                        hashedPassword = passwordEncoder.encode("admin"),
                        role = Role.Admin,
                        name = "System",
                        surname = "Administrator",
                        phone = "0000000000",
                        nationalNumber = "0000000000"
                    )
                    userRepository.save(adminUser)
                }
            )
    }
}
