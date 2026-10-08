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

@RestController
@RequestMapping("/api/destinations")
class DestinationController(
    private val destinationService: DestinationService
) {

    @PostMapping
    fun createDestination(
        @Valid @RequestBody request: CreateDestinationRequest
    ): ResponseEntity<DestinationResponse> {
        val destination = destinationService.create(request)

        return ResponseEntity
            .created(URI.create("/api/destinations/${destination.id}"))
            .body(destination)
    }

    @GetMapping("/{id}")
    fun getDestination(
        @PathVariable id: Long
    ): ResponseEntity<DestinationResponse> {
        return ResponseEntity.ok(
            destinationService.getById(id)
        )
    }
}