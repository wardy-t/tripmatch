package com.tomward.tripmatch.recommendation

data class RecommendationScore(
    val budgetScore: Int,
    val climateScore: Int,
    val interestScore: Int,
    val flightTimeScore: Int
) {
    val total: Int
        get() = budgetScore +
            climateScore +
            interestScore +
            flightTimeScore
}