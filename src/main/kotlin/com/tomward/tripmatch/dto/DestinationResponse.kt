package com.tomward.tripmatch.dto

import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.model.Destination
import java.math.BigDecimal
import java.time.OffsetDateTime

data class DestinationResponse(
    val id: Long,
    val city: String,
    val country: String,
    val averageCost: BigDecimal,
    val climate: Climate,
    val flightTimeHours: BigDecimal,
    val interests: Set<String>,
    val createdAt: OffsetDateTime
) {
    companion object {
        fun from(destination: Destination): DestinationResponse {
            return DestinationResponse(
                id = requireNotNull(destination.id),
                city = destination.city,
                country = destination.country,
                averageCost = destination.averageCost,
                climate = destination.climate,
                flightTimeHours = destination.flightTimeHours,
                interests = destination.interests.toSet(),
                createdAt = destination.createdAt
            )
        }
    }
}