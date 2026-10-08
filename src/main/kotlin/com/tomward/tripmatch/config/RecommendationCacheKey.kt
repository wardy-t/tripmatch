package com.tomward.tripmatch.config

data class RecommendationCacheKey(
    val budget: String,
    val climate: String,
    val maxFlightTimeHours: String,
    val interests: List<String>,
    val limit: Int
)