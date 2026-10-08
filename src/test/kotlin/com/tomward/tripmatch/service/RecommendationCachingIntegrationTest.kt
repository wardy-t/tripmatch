package com.tomward.tripmatch.service

import com.tomward.tripmatch.TestcontainersConfiguration
import com.tomward.tripmatch.dto.CreateDestinationRequest
import com.tomward.tripmatch.dto.UpdateDestinationRequest
import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.repository.DestinationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.math.BigDecimal
import kotlin.test.assertEquals

@SpringBootTest
@Import(TestcontainersConfiguration::class)
class RecommendationCachingIntegrationTest {

    @Autowired
    private lateinit var destinationService: DestinationService

    @MockitoSpyBean
    private lateinit var destinationRepository: DestinationRepository

    @Autowired
    private lateinit var cacheManager: CacheManager

    @BeforeEach
    fun setUp() {
        cacheManager
            .getCache("destinationRecommendations")
            ?.clear()

        destinationRepository.deleteAll()
        clearInvocations(destinationRepository)
    }

    @Test
    fun `equivalent recommendation requests use cached results`() {
        destinationService.create(
            createRequest(
                city = "Barcelona",
                country = "Spain"
            )
        )

        clearInvocations(destinationRepository)

        val firstResult = destinationService.recommend(
            budget = BigDecimal("1000.00"),
            climate = Climate.WARM,
            maxFlightTimeHours = BigDecimal("5.0"),
            interests = setOf("Food", "History"),
            limit = 5
        )

        val secondResult = destinationService.recommend(
            budget = BigDecimal("1000"),
            climate = Climate.WARM,
            maxFlightTimeHours = BigDecimal("5"),
            interests = setOf(" history ", "food"),
            limit = 5
        )

        assertEquals(firstResult, secondResult)

        verify(
            destinationRepository,
            times(1)
        ).findAll()
    }

    @Test
    fun `destination mutations invalidate cached recommendations`() {
        val original = destinationService.create(
            createRequest(
                city = "Barcelona",
                country = "Spain"
            )
        )

        clearInvocations(destinationRepository)

        recommend()
        recommend()

        verify(
            destinationRepository,
            times(1)
        ).findAll()

        val second = destinationService.create(
            createRequest(
                city = "Reykjavik",
                country = "Iceland",
                climate = Climate.COLD
            )
        )

        recommend()

        verify(
            destinationRepository,
            times(2)
        ).findAll()

        destinationService.update(
            second.id,
            UpdateDestinationRequest(
                city = "Reykjavik",
                country = "Iceland",
                averageCost = BigDecimal("1100.00"),
                climate = Climate.COLD,
                flightTimeHours = BigDecimal("3.2"),
                interests = setOf("nature", "hiking")
            )
        )

        recommend()

        verify(
            destinationRepository,
            times(3)
        ).findAll()

        destinationService.delete(original.id)

        recommend()

        verify(
            destinationRepository,
            times(4)
        ).findAll()
    }

    private fun recommend() =
        destinationService.recommend(
            budget = BigDecimal("1000"),
            climate = Climate.WARM,
            maxFlightTimeHours = BigDecimal("5"),
            interests = setOf("food", "history"),
            limit = 5
        )

    private fun createRequest(
        city: String,
        country: String,
        climate: Climate = Climate.WARM
    ) = CreateDestinationRequest(
        city = city,
        country = country,
        averageCost = BigDecimal("750.00"),
        climate = climate,
        flightTimeHours = BigDecimal("3.0"),
        interests = setOf("food", "history")
    )
}