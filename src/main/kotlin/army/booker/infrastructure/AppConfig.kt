package army.booker.infrastructure

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("booker")
data class AppConfig(
  val externalServices: ExternalServices = ExternalServices(),
  val redis: Redis = Redis(),
  val security: Security = Security(),
  val kafka: Kafka = Kafka(),
) {
  class ExternalServices(
    val grpcConnection: GrpcConnection = GrpcConnection(),
    val httpConnection: HttpConnection = HttpConnection(),
  ) {
    data class GrpcConnection(
      val enabled: Boolean = true,
      val channels: Map<String, GrpcChannel> = emptyMap(),
      val clients: Map<String, GrpcClient> = emptyMap(),
      val provideDefaultChannels: Boolean = true,
    ) {
      data class GrpcChannel(
        val host: String,
        val port: Int,
        val retryAttempts: Int = 1000,
      )

      data class GrpcClient(
        val enabled: Boolean = true,
      )
    }

    data class HttpConnection(
      val enabled: Boolean = true,
    )
  }

  data class Security(
    val token: Token = Token(),
  ) {
    data class Token(
      val jwt: Jwt = Jwt(),
    ) {
      data class Jwt(
        val secretKey: String = "gfGlnCD+FDa8kvkVw9OHaRj+W/IXZCrZsu2FCJINRkPvgEI8nPTq70gY74d5QYAnRqdDsdMb/wY00/Uc/A540w==",
      )
    }
  }

  data class Redis(
    val enabled: Boolean = false,
  )

  data class Kafka(
    val producerEnabled: Boolean = false,
    val consumerEnabled: Boolean = false,
    val clusters: Map<String, Cluster> = emptyMap(),
    val producer: Producer = Producer(),
    val admin: Admin = Admin(),
    val consumer: Consumer = Consumer(),
  ) {
    data class Producer(
      val threadCount: Int = 1,
      val connection: Connection = Connection(),
    )

    data class Consumer(
      val threadCount: Int = 1,
      val batchSize: Int = 500,
      val windowSize: Int = 100,
      val windowTimeout: Duration = Duration.ofSeconds(1),
      val commitBatchSize: Int = 500,
      val commitInterval: Duration = Duration.ofSeconds(1),
    )

    data class Connection(
      val aliveDuration: Duration = Duration.ZERO,
    )

    data class Admin(
      val minimumClusterSize: Int = 1,
      val timeout: Duration = Duration.ZERO,
    )

    data class Cluster(
      val generalProperties: KafkaProperties,
      val topicProperties: Map<String, KafkaProperties> = emptyMap(),
      val consumerProperties: Map<String, Consumer> = emptyMap(),
    )
  }
}
