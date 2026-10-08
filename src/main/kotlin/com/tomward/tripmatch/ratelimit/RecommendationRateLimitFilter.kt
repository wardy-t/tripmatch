package com.tomward.tripmatch.ratelimit

import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Component
class RecommendationRateLimitFilter(
    @Value(
        "\${tripmatch.rate-limit.recommendations.capacity:30}"
    )
    private val capacity: Long,

    @Value(
        "\${tripmatch.rate-limit.recommendations.refill-seconds:60}"
    )
    private val refillSeconds: Long
) : OncePerRequestFilter() {

    private val bucket: Bucket = Bucket.builder()
        .addLimit { limit ->
            limit
                .capacity(capacity)
                .refillGreedy(
                    capacity,
                    Duration.ofSeconds(refillSeconds)
                )
        }
        .build()

    override fun shouldNotFilter(
        request: HttpServletRequest
    ): Boolean =
        request.method != "GET" ||
            request.requestURI != RECOMMENDATION_PATH

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        response.setHeader(
            RATE_LIMIT_HEADER,
            capacity.toString()
        )

        response.setHeader(
            RATE_LIMIT_REMAINING_HEADER,
            probe.remainingTokens.toString()
        )

        if (probe.isConsumed) {
            filterChain.doFilter(request, response)
            return
        }

        val retryAfterSeconds = max(
            1,
            TimeUnit.NANOSECONDS.toSeconds(
                probe.nanosToWaitForRefill
            )
        )

        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = "application/json"
        response.setHeader(
            RETRY_AFTER_HEADER,
            retryAfterSeconds.toString()
        )

        response.writer.write(
            """
            {
              "status": 429,
              "error": "Too Many Requests",
              "message": "Recommendation request limit exceeded",
              "path": "$RECOMMENDATION_PATH"
            }
            """.trimIndent()
        )
    }

    companion object {
        private const val RECOMMENDATION_PATH =
            "/api/destinations/recommendations"

        private const val RATE_LIMIT_HEADER =
            "X-RateLimit-Limit"

        private const val RATE_LIMIT_REMAINING_HEADER =
            "X-RateLimit-Remaining"

        private const val RETRY_AFTER_HEADER =
            "Retry-After"
    }
}