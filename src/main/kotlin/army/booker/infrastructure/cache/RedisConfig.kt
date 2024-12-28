package army.booker.infrastructure.cache

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig {
  @Value("\${spring.data.redis.host}")
  private lateinit var redisHost: String

  @Value("\${spring.data.redis.port}")
  private var redisPort: Int = 6379

  @Bean
  fun redissonClient(): RedissonClient {
    val config = Config()
    println("Connecting to Redis at: redis://$redisHost:$redisPort") // Add debug log
    config.useSingleServer()
      .setAddress("redis://$redisHost:$redisPort")
      .setConnectionMinimumIdleSize(1)
      .setConnectionPoolSize(2)
      .setRetryAttempts(3)
      .setRetryInterval(1500)
      .setTimeout(3000)

    return Redisson.create(config)
  }
}
