package com.tomward.tripmatch.dto

import com.tomward.tripmatch.model.Destination
import org.springframework.data.domain.Page

data class DestinationPageResponse(
    val content: List<DestinationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
) {
    companion object {
        fun from(destinations: Page<Destination>): DestinationPageResponse {
            return DestinationPageResponse(
                content = destinations.content.map(DestinationResponse::from),
                page = destinations.number,
                size = destinations.size,
                totalElements = destinations.totalElements,
                totalPages = destinations.totalPages,
                first = destinations.isFirst,
                last = destinations.isLast
            )
        }
    }
}