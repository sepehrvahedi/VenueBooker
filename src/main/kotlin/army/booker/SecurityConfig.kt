package army.booker

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Suppress("removal", "DEPRECATION")
@Configuration
@EnableWebFluxSecurity
class SecurityConfig2 {

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
    return http
      .csrf { it.disable() }
      .cors()
      .and()
      .authorizeExchange { exchanges ->
        exchanges
          .pathMatchers("/api/v1/**").permitAll()
          .anyExchange().authenticated()
      }
      .httpBasic().disable()
      .formLogin().disable()
      .build()
  }
}
