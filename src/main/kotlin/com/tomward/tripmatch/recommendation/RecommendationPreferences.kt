package com.tomward.tripmatch.recommendation

import com.tomward.tripmatch.model.Climate
import java.math.BigDecimal

data class RecommendationPreferences(
    val budget: BigDecimal,
    val climate: Climate,
    val maxFlightTimeHours: BigDecimal,
    val interests: Set<String>
) {
    init {
        require(budget > BigDecimal.ZERO) {
            "Budget must be greater than zero"
        }
        require(maxFlightTimeHours > BigDecimal.ZERO) {
            "Maximum flight time must be greater than zero"
        }
        require(interests.isNotEmpty()) {
            "At least one interest is required"
        }
    }

    fun normalisedInterests(): Set<String> {
        return interests
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()
    }
}