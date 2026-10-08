package com.tomward.tripmatch.recommendation

import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.model.Destination
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

class RecommendationScorerTest {

    private val scorer = RecommendationScorer()

    @Test
    fun `awards 100 points for a complete preference match`() {
        val destination = destination(
            cost = "650.00",
            climate = Climate.WARM,
            flightTime = "2.8",
            interests = setOf("food", "culture")
        )

        val preferences = RecommendationPreferences(
            budget = BigDecimal("800.00"),
            climate = Climate.WARM,
            maxFlightTimeHours = BigDecimal("4.0"),
            interests = setOf("Food", "Culture")
        )

        val score = scorer.calculate(destination, preferences)

        assertEquals(35, score.budgetScore)
        assertEquals(25, score.climateScore)
        assertEquals(30, score.interestScore)
        assertEquals(10, score.flightTimeScore)
        assertEquals(100, score.total)
    }

    @Test
    fun `applies proportional scores to partial matches`() {
        val destination = destination(
            cost = "1000.00",
            climate = Climate.COLD,
            flightTime = "4.0",
            interests = setOf("food")
        )

        val preferences = RecommendationPreferences(
            budget = BigDecimal("500.00"),
            climate = Climate.WARM,
            maxFlightTimeHours = BigDecimal("2.0"),
            interests = setOf("food", "culture")
        )

        val score = scorer.calculate(destination, preferences)

        assertEquals(18, score.budgetScore)
        assertEquals(0, score.climateScore)
        assertEquals(15, score.interestScore)
        assertEquals(5, score.flightTimeScore)
        assertEquals(38, score.total)
    }

    private fun destination(
        cost: String,
        climate: Climate,
        flightTime: String,
        interests: Set<String>
    ): Destination {
        return Destination(
            city = "Test City",
            country = "Test Country",
            averageCost = BigDecimal(cost),
            climate = climate,
            flightTimeHours = BigDecimal(flightTime),
            interests = interests.toMutableSet()
        )
    }
}