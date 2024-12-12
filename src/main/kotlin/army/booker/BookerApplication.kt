package army.booker

import army.booker.infrastructure.AppConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.core.Ordered
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableReactiveMongoAuditing
@EnableConfigurationProperties(AppConfig::class)
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE + 1)
class BookerApplication

fun main(args: Array<String>) {
  runApplication<BookerApplication>(*args)
}
