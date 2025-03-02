package army.booker.application.http.v1.bundle.controllers

import army.booker.application.http.v1.bundle.BundleDTO
import army.booker.application.http.v1.bundle.FindBundlesRequest
import army.booker.application.http.v1.bundle.FindBundlesResponse
import army.booker.application.http.v1.bundle.ReserveBundleRequest
import army.booker.domain.bundle.Bundle
import army.booker.domain.bundle.services.BundleService
import army.booker.domain.user.Role
import army.booker.domain.user.UserTokenPayload
import army.booker.infrastructure.token.TokenAuthenticationInterceptor
import army.booker.infrastructure.token.TokenAuthenticationInterceptor.Companion.AUTHORIZATION_HEADER
import army.booker.infrastructure.token.UnauthorizedException
import jakarta.validation.Valid
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/customer/bundles")
@CrossOrigin(
  origins = ["http://localhost:63342"],
  allowedHeaders = ["*"],
  methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
  allowCredentials = "true"
)
class CustomerBundleController(
  private val logger: Logger,
  private val bundleService: BundleService,
  private val tokenAuthenticationInterceptor: TokenAuthenticationInterceptor,
) {
  private fun verifyCustomerRole(userPayload: UserTokenPayload): Mono<UserTokenPayload> {
    return if (userPayload.role == Role.Customer) {
      Mono.just(userPayload)
    } else {
      Mono.error(UnauthorizedException("Access denied: Customer role required"))
    }
  }

  @GetMapping
  fun findBundles(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid request: FindBundlesRequest,
  ): Mono<ResponseEntity<FindBundlesResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifyCustomerRole)
      .flatMapMany { _ ->
        bundleService.findBundles(
          name = request.name,
          minPrice = request.minPrice,
          maxPrice = request.maxPrice,
          active = request.active,
          products = request.products,
          sorting = request.sorting
        )
      }
      .collectList()
      .map { bundles ->
        ResponseEntity.ok(
          FindBundlesResponse(
            bundles = bundles.map { it.toDTO() }
          )
        )
      }
      .onErrorResume { error ->
        handleError(error, FindBundlesResponse())
      }

  @PostMapping("/reserve")
  fun reserveBundle(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid @RequestBody request: ReserveBundleRequest
  ): Mono<ResponseEntity<Bundle>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifyCustomerRole)
      .flatMap { userPayload ->
        val userName = userPayload.username

        bundleService.reserveBundle(
          bundleId = request.bundleId,
          userId = userPayload.id,
          userName = userName,
          reservationDate = LocalDate.parse(request.reservationDate),
        )
      }
      .map { bundle ->
        ResponseEntity.ok(bundle)
      }
      .onErrorResume { error ->
        handleError(error, null)
      }

  @GetMapping("/reservations")
  fun getUserReservations(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?
  ): Mono<ResponseEntity<FindBundlesResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifyCustomerRole)
      .flatMapMany { userPayload ->
        bundleService.findUserReservations(userPayload.id)
      }
      .collectList()
      .map { bundles ->
        ResponseEntity.ok(
          FindBundlesResponse(
            bundles = bundles.map { it.toDTO() }
          )
        )
      }
      .onErrorResume { error ->
        handleError(error, FindBundlesResponse())
      }

  private fun <T> handleError(error: Throwable, emptyResponse: T): Mono<ResponseEntity<T>> {
    logger.error("Operation failed", error)
    return when (error) {
      is UnauthorizedException -> Mono.just(
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(emptyResponse)
      )
      is IllegalArgumentException -> Mono.just(
        ResponseEntity.badRequest().body(emptyResponse)
      )
      else -> Mono.just(
        ResponseEntity.internalServerError().body(emptyResponse)
      )
    }
  }

  private fun Bundle.toDTO() = BundleDTO(
    id = this.id!!,
    name = this.name,
    price = this.price,
    products = this.products,
    active = this.active,
    reservations = this.reservations,
  )
}
