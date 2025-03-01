package army.booker.application.http.v1.bundle.controllers

import army.booker.application.http.v1.bundle.BundleDTO
import army.booker.application.http.v1.bundle.CreateBundleRequest
import army.booker.application.http.v1.bundle.CreateBundleResponse
import army.booker.application.http.v1.bundle.EditBundleRequest
import army.booker.application.http.v1.bundle.EditBundleResponse
import army.booker.application.http.v1.bundle.FindBundlesRequest
import army.booker.application.http.v1.bundle.FindBundlesResponse
import army.booker.application.http.v1.bundle.UpdateBundleStatusRequest
import army.booker.application.http.v1.bundle.UpdateBundleStatusResponse
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
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/bundles")
@CrossOrigin(
  origins = ["http://localhost:63342"],
  allowedHeaders = ["*"],
  methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS],
  allowCredentials = "true"
)
class BundleController(
  private val logger: Logger,
  private val bundleService: BundleService,
  private val tokenAuthenticationInterceptor: TokenAuthenticationInterceptor,
) {
  private fun verifySupplierRole(userPayload: UserTokenPayload): Mono<UserTokenPayload> {
    return if (userPayload.role == Role.Supplier) {
      Mono.just(userPayload)
    } else {
      Mono.error(UnauthorizedException("Access denied: Supplier role required"))
    }
  }

  @PostMapping
  fun createBundle(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid @RequestBody request: CreateBundleRequest,
  ): Mono<ResponseEntity<CreateBundleResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifySupplierRole)
      .flatMap { user ->
        bundleService.createBundle(
          name = request.name,
          price = request.price,
          products = request.products,
          ownerId = user.id
        )
      }
      .map { bundle ->
        logger.info("Successfully created bundle with name: ${request.name}")
        ResponseEntity.ok(
          CreateBundleResponse(
            id = bundle.id,
            name = bundle.name,
            price = bundle.price,
            products = bundle.products
          )
        )
      }
      .onErrorResume { error ->
        logger.error("Failed to create bundle with name: ${request.name}", error)
        handleError(error, CreateBundleResponse())
      }

  @PutMapping
  fun editBundles(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid @RequestBody request: EditBundleRequest,
  ): Mono<ResponseEntity<EditBundleResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifySupplierRole)
      .flatMap { user ->
        if (request.price == null && request.products == null) {
          Mono.error(IllegalArgumentException("Either price or products must be provided"))
        } else {
          bundleService.editBundles(
            ids = request.ids,
            price = request.price ?: 0,
            products = request.products ?: emptyList(),
            ownerId = user.id
          )
        }
      }
      .map {
        ResponseEntity.ok(EditBundleResponse())
      }
      .onErrorResume { error ->
        handleError(error, EditBundleResponse())
      }

  @PutMapping("/status")
  fun updateBundleStatus(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid @RequestBody request: UpdateBundleStatusRequest,
  ): Mono<ResponseEntity<UpdateBundleStatusResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifySupplierRole)
      .flatMap { user ->
        bundleService.updateBundleStatus(
          id = request.id,
          active = request.active,
          ownerId = user.id
        )
      }
      .map { bundle ->
        ResponseEntity.ok(
          UpdateBundleStatusResponse(
            id = bundle.id,
            active = bundle.active
          )
        )
      }
      .onErrorResume { error ->
        handleError(error, UpdateBundleStatusResponse())
      }

  @GetMapping
  fun findBundles(
    @RequestHeader(AUTHORIZATION_HEADER) authHeader: String?,
    @Valid request: FindBundlesRequest,
  ): Mono<ResponseEntity<FindBundlesResponse>> =
    tokenAuthenticationInterceptor.extractAndValidateToken(authHeader)
      .flatMap(::verifySupplierRole)
      .flatMapMany { user ->
        bundleService.findBundles(
          name = request.name,
          minPrice = request.minPrice,
          maxPrice = request.maxPrice,
          active = request.active,
          products = request.products,
          sorting = request.sorting,
          ownerId = user.id
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
