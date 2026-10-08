package com.tomward.tripmatch.controller

import com.tomward.tripmatch.dto.CreateDestinationRequest
import com.tomward.tripmatch.dto.DestinationResponse
import com.tomward.tripmatch.service.DestinationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import com.tomward.tripmatch.dto.DestinationPageResponse
import com.tomward.tripmatch.model.Climate
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigDecimal
import com.tomward.tripmatch.dto.DestinationRecommendationResponse
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import com.tomward.tripmatch.dto.UpdateDestinationRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springdoc.core.annotations.ParameterObject

@Tag(
    name = "Destinations",
    description = "Create, manage, filter and rank travel destinations"
)

@RestController
@RequestMapping("/api/destinations")
class DestinationController(
    private val destinationService: DestinationService
) {

    @Operation(summary = "Create a destination")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Destination created"
            ),
            ApiResponse(
                responseCode = "400",
                description = "Request validation failed"
            )
        ]
    )
    @PostMapping
    fun createDestination(
        @Valid @RequestBody request: CreateDestinationRequest
    ): ResponseEntity<DestinationResponse> {
        val destination = destinationService.create(request)

        return ResponseEntity
            .created(URI.create("/api/destinations/${destination.id}"))
            .body(destination)
    }

    @Operation(summary = "List and filter destinations")
    @GetMapping
    fun getDestinations(
        @RequestParam(required = false) maxBudget: BigDecimal?,
        @RequestParam(required = false) climate: Climate?,
        @RequestParam(required = false) interest: String?,
        @ParameterObject
        @PageableDefault(size = 20, sort = ["city"])
        pageable: Pageable
    ): ResponseEntity<DestinationPageResponse> {
        return ResponseEntity.ok(
            destinationService.getAll(
                maxBudget = maxBudget,
                climate = climate,
                interest = interest,
                pageable = pageable
            )
        )
    }

    @Operation(summary = "Rank destination recommendations")
    @GetMapping("/recommendations")
    fun getRecommendations(
        @RequestParam
        @DecimalMin("0.01")
        budget: BigDecimal,

        @RequestParam
        climate: Climate,

        @RequestParam
        @DecimalMin("0.1")
        maxFlightTimeHours: BigDecimal,

        @RequestParam
        @Size(min = 1, max = 10)
        interests: Set<String>,

        @RequestParam(defaultValue = "10")
        @Min(1)
        @Max(50)
        limit: Int
    ): ResponseEntity<List<DestinationRecommendationResponse>> {
        return ResponseEntity.ok(
            destinationService.recommend(
                budget = budget,
                climate = climate,
                maxFlightTimeHours = maxFlightTimeHours,
                interests = interests,
                limit = limit
            )
        )
    }

    @Operation(summary = "Retrieve a destination")
    @GetMapping("/{id}")
    fun getDestination(
        @PathVariable id: Long
    ): ResponseEntity<DestinationResponse> {
        return ResponseEntity.ok(
            destinationService.getById(id)
        )
    }

    @Operation(summary = "Replace a destination")
    @PutMapping("/{id}")
    fun updateDestination(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateDestinationRequest
    ): ResponseEntity<DestinationResponse> {
        return ResponseEntity.ok(
            destinationService.update(id, request)
        )
    }

    @Operation(summary = "Delete a destination")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Destination deleted"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Destination not found"
            )
        ]
    )
    @DeleteMapping("/{id}")
    fun deleteDestination(
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        destinationService.delete(id)

        return ResponseEntity.noContent().build()
    }

}