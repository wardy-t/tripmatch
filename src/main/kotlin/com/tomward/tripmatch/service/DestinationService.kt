package com.tomward.tripmatch.service

import com.tomward.tripmatch.dto.CreateDestinationRequest
import com.tomward.tripmatch.dto.DestinationResponse
import com.tomward.tripmatch.exception.DestinationNotFoundException
import com.tomward.tripmatch.model.Destination
import com.tomward.tripmatch.repository.DestinationRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.tomward.tripmatch.dto.DestinationPageResponse
import com.tomward.tripmatch.model.Climate
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

@Service
class DestinationService(
    private val destinationRepository: DestinationRepository
) {

    @Transactional
    fun create(request: CreateDestinationRequest): DestinationResponse {
        val destination = Destination(
            city = request.city.trim(),
            country = request.country.trim(),
            averageCost = request.averageCost,
            climate = request.climate,
            flightTimeHours = request.flightTimeHours,
            interests = request.interests
                .map { it.trim().lowercase() }
                .toMutableSet()
        )

        return DestinationResponse.from(
            destinationRepository.save(destination)
        )
    }

    @Transactional(readOnly = true)
    fun getById(id: Long): DestinationResponse {
        val destination = destinationRepository.findById(id)
            .orElseThrow { DestinationNotFoundException(id) }

        return DestinationResponse.from(destination)
    }

    @Transactional(readOnly = true)
    fun getAll(
        maxBudget: BigDecimal?,
        climate: Climate?,
        interest: String?,
        pageable: Pageable
    ): DestinationPageResponse {
        val normalisedInterest = interest
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }

        val destinations = destinationRepository.findFiltered(
            maxBudget = maxBudget,
            climate = climate,
            interest = normalisedInterest,
            pageable = pageable
        )

        return DestinationPageResponse.from(destinations)
    }
}