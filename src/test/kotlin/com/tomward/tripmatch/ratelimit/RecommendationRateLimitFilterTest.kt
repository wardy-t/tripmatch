package com.tomward.tripmatch.ratelimit

import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RecommendationRateLimitFilterTest {

    @Test
    fun `rejects recommendation requests after capacity is exhausted`() {
        val filter = RecommendationRateLimitFilter(
            capacity = 2,
            refillSeconds = 60
        )

        val firstResponse = executeRecommendationRequest(filter)
        val secondResponse = executeRecommendationRequest(filter)
        val rejectedResponse = executeRecommendationRequest(filter)

        assertEquals(200, firstResponse.status)
        assertEquals("1", firstResponse.getHeader("X-RateLimit-Remaining"))

        assertEquals(200, secondResponse.status)
        assertEquals("0", secondResponse.getHeader("X-RateLimit-Remaining"))

        assertEquals(429, rejectedResponse.status)
        assertEquals("2", rejectedResponse.getHeader("X-RateLimit-Limit"))
        assertEquals(
            "0",
            rejectedResponse.getHeader("X-RateLimit-Remaining")
        )
        assertNotNull(rejectedResponse.getHeader("Retry-After"))

        assertContains(
            rejectedResponse.contentAsString,
            "Recommendation request limit exceeded"
        )
    }

    @Test
    fun `does not limit other destination endpoints`() {
        val filter = RecommendationRateLimitFilter(
            capacity = 1,
            refillSeconds = 60
        )

        repeat(3) {
            val request = MockHttpServletRequest(
                "GET",
                "/api/destinations"
            )
            val response = MockHttpServletResponse()

            filter.doFilter(
                request,
                response,
                MockFilterChain()
            )

            assertEquals(200, response.status)
        }
    }

    private fun executeRecommendationRequest(
        filter: RecommendationRateLimitFilter
    ): MockHttpServletResponse {
        val request = MockHttpServletRequest(
            "GET",
            "/api/destinations/recommendations"
        )
        val response = MockHttpServletResponse()

        filter.doFilter(
            request,
            response,
            MockFilterChain()
        )

        return response
    }
}