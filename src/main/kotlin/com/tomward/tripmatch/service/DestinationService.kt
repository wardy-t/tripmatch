package com.tomward.tripmatch.service

import com.tomward.tripmatch.dto.CreateDestinationRequest
import com.tomward.tripmatch.dto.DestinationPageResponse
import com.tomward.tripmatch.dto.DestinationRecommendationResponse
import com.tomward.tripmatch.dto.DestinationResponse
import com.tomward.tripmatch.exception.DestinationNotFoundException
import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.model.Destination
import com.tomward.tripmatch.recommendation.RecommendationPreferences
import com.tomward.tripmatch.recommendation.RecommendationScorer
import com.tomward.tripmatch.repository.DestinationRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import com.tomward.tripmatch.dto.UpdateDestinationRequest
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable

@Service
class DestinationService(
    private val destinationRepository: DestinationRepository,
    private val recommendationScorer: RecommendationScorer
) {

    @CacheEvict(
        cacheNames = ["destinationRecommendations"],
        allEntries = true
    )
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

    @CacheEvict(
        cacheNames = ["destinationRecommendations"],
        allEntries = true
    )
    @Transactional
    fun update(
        id: Long,
        request: UpdateDestinationRequest
    ): DestinationResponse {
        val destination = destinationRepository.findById(id)
            .orElseThrow { DestinationNotFoundException(id) }

        destination.city = request.city.trim()
        destination.country = request.country.trim()
        destination.averageCost = request.averageCost
        destination.climate = request.climate
        destination.flightTimeHours = request.flightTimeHours

        destination.interests.clear()
        destination.interests.addAll(
            request.interests.map { it.trim().lowercase() }
        )

        return DestinationResponse.from(destination)
    }

    @CacheEvict(
    cacheNames = ["destinationRecommendations"],
    allEntries = true
    )
    @Transactional
    fun delete(id: Long) {
        val destination = destinationRepository.findById(id)
            .orElseThrow { DestinationNotFoundException(id) }

        destinationRepository.delete(destination)
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

    @Cacheable(
        cacheNames = ["destinationRecommendations"],
        keyGenerator = "recommendationCacheKeyGenerator",
        sync = true
    )
    @Transactional(readOnly = true)
    fun recommend(
        budget: BigDecimal,
        climate: Climate,
        maxFlightTimeHours: BigDecimal,
        interests: Set<String>,
        limit: Int
    ): List<DestinationRecommendationResponse> {
        val preferences = RecommendationPreferences(
            budget = budget,
            climate = climate,
            maxFlightTimeHours = maxFlightTimeHours,
            interests = interests
        )

        return destinationRepository.findAll()
            .map { destination ->
                val score = recommendationScorer.calculate(
                    destination = destination,
                    preferences = preferences
                )

                DestinationRecommendationResponse.from(
                    destination = destination,
                    score = score
                )
            }
            .sortedWith(
                compareByDescending<DestinationRecommendationResponse> {
                    it.score.total
                }
                    .thenBy { it.destination.averageCost }
                    .thenBy { it.destination.city }
            )
            .take(limit.coerceIn(1, MAX_RECOMMENDATION_LIMIT))
    }

    companion object {
        const val MAX_RECOMMENDATION_LIMIT = 50
    }
}