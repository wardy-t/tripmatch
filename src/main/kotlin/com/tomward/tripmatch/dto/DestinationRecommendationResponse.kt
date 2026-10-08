package com.tomward.tripmatch.dto

import com.tomward.tripmatch.model.Destination
import com.tomward.tripmatch.recommendation.RecommendationScore

data class DestinationRecommendationResponse(
    val destination: DestinationResponse,
    val score: RecommendationScore
) {
    companion object {
        fun from(
            destination: Destination,
            score: RecommendationScore
        ): DestinationRecommendationResponse {
            return DestinationRecommendationResponse(
                destination = DestinationResponse.from(destination),
                score = score
            )
        }
    }
}