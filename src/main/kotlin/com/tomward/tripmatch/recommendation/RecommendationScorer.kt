package com.tomward.tripmatch.recommendation

import com.tomward.tripmatch.model.Destination
import java.math.BigDecimal
import java.math.RoundingMode
import org.springframework.stereotype.Component

@Component
class RecommendationScorer {

    fun calculate(
        destination: Destination,
        preferences: RecommendationPreferences
    ): RecommendationScore {
        return RecommendationScore(
            budgetScore = calculateProportionalScore(
                preferredMaximum = preferences.budget,
                actualValue = destination.averageCost,
                maximumScore = BUDGET_WEIGHT
            ),
            climateScore = if (
                destination.climate == preferences.climate
            ) {
                CLIMATE_WEIGHT
            } else {
                0
            },
            interestScore = calculateInterestScore(
                destination = destination,
                preferences = preferences
            ),
            flightTimeScore = calculateProportionalScore(
                preferredMaximum = preferences.maxFlightTimeHours,
                actualValue = destination.flightTimeHours,
                maximumScore = FLIGHT_TIME_WEIGHT
            )
        )
    }

    private fun calculateInterestScore(
        destination: Destination,
        preferences: RecommendationPreferences
    ): Int {
        val requestedInterests = preferences.normalisedInterests()

        val destinationInterests = destination.interests
            .map { it.trim().lowercase() }
            .toSet()

        val matchingInterests =
            requestedInterests.intersect(destinationInterests).size

        return BigDecimal(matchingInterests)
            .divide(
                BigDecimal(requestedInterests.size),
                4,
                RoundingMode.HALF_UP
            )
            .multiply(BigDecimal(INTEREST_WEIGHT))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
    }

    private fun calculateProportionalScore(
        preferredMaximum: BigDecimal,
        actualValue: BigDecimal,
        maximumScore: Int
    ): Int {
        if (actualValue <= preferredMaximum) {
            return maximumScore
        }

        return preferredMaximum
            .divide(actualValue, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(maximumScore))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()
            .coerceIn(0, maximumScore)
    }

    companion object {
        const val BUDGET_WEIGHT = 35
        const val CLIMATE_WEIGHT = 25
        const val INTEREST_WEIGHT = 30
        const val FLIGHT_TIME_WEIGHT = 10
    }
}