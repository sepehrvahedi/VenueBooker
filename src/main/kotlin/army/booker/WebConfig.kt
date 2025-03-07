package army.booker

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebConfig : WebFluxConfigurer {
  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/api/v1/**")
      .allowedOrigins("*")  // Accept all origins
      .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
      .allowedHeaders("Content-Type", "Accept", "Authorization")
      .exposedHeaders("Access-Control-Allow-Origin")
      .maxAge(3600)
  }
}
