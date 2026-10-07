package com.tomward.tripmatch.repository

import com.tomward.tripmatch.TestcontainersConfiguration
import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.model.Destination
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DataJpaTest
@Import(TestcontainersConfiguration::class)
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class DestinationRepositoryTest {

    @Autowired
    private lateinit var repository: DestinationRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Test
    fun `saves and retrieves a destination with its interests`() {
        val destination = Destination(
            city = "Lisbon",
            country = "Portugal",
            averageCost = BigDecimal("650.00"),
            climate = Climate.WARM,
            flightTimeHours = BigDecimal("2.8"),
            interests = mutableSetOf("food", "culture", "nightlife")
        )

        val savedDestination = repository.saveAndFlush(destination)

        assertNotNull(savedDestination.id)

        entityManager.clear()

        val retrievedDestination =
            repository.findByCityIgnoreCaseAndCountryIgnoreCase(
                city = "lisbon",
                country = "portugal"
            )

        assertNotNull(retrievedDestination)
        assertEquals("Lisbon", retrievedDestination.city)
        assertEquals("Portugal", retrievedDestination.country)
        assertTrue(
            retrievedDestination.averageCost.compareTo(
                BigDecimal("650.00")
            ) == 0
        )
        assertEquals(Climate.WARM, retrievedDestination.climate)
        assertEquals(
            setOf("food", "culture", "nightlife"),
            retrievedDestination.interests
        )
    }
}