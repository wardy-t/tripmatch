package com.tomward.tripmatch.controller

import com.tomward.tripmatch.TestcontainersConfiguration
import com.tomward.tripmatch.repository.DestinationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.hamcrest.Matchers.matchesPattern
import kotlin.test.assertEquals
import com.tomward.tripmatch.model.Climate
import com.tomward.tripmatch.model.Destination
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import java.math.BigDecimal
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import kotlin.test.assertFalse

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class DestinationControllerIntegrationTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var destinationRepository: DestinationRepository

    @BeforeEach
    fun clearDatabase() {
        destinationRepository.deleteAll()
    }

    @Test
    fun `creates a destination and returns 201`() {
        val requestBody = """
            {
              "city": "Lisbon",
              "country": "Portugal",
              "averageCost": 650.00,
              "climate": "WARM",
              "flightTimeHours": 2.8,
              "interests": ["Food", "Culture", "Nightlife"]
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/destinations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isCreated)
            .andExpect(
                header().string(
                    "Location",
                    matchesPattern("/api/destinations/[0-9]+")
                )
            )
            .andExpect(
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath("\$.city").value("Lisbon"))
            .andExpect(jsonPath("\$.country").value("Portugal"))
            .andExpect(jsonPath("\$.climate").value("WARM"))
            .andExpect(jsonPath("\$.interests[0]").exists())

        assertEquals(1, destinationRepository.count())

        val savedInterests = jdbcTemplate.queryForList(
            "SELECT interest FROM destination_interests",
            String::class.java
        ).toSet()

        assertEquals(
            setOf("food", "culture", "nightlife"),
            savedInterests
        )
    }

    @Test
    fun `rejects invalid destination data with 400`() {
        val requestBody = """
            {
              "city": "",
              "country": "Portugal",
              "averageCost": -1.00,
              "climate": "WARM",
              "flightTimeHours": 0,
              "interests": []
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/destinations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("\$.status").value(400))
            .andExpect(jsonPath("\$.error").value("Bad Request"))
            .andExpect(jsonPath("\$.message").value("Request validation failed"))
            .andExpect(jsonPath("\$.path").value("/api/destinations"))
            .andExpect(jsonPath("\$.fieldErrors.city").exists())
            .andExpect(jsonPath("\$.fieldErrors.averageCost").exists())
            .andExpect(jsonPath("\$.fieldErrors.flightTimeHours").exists())
            .andExpect(jsonPath("\$.fieldErrors.interests").exists())

        assertEquals(0, destinationRepository.count())
    }

    @Test
    fun `retrieves a destination by id`() {
        val destination = destinationRepository.saveAndFlush(
            Destination(
                city = "Lisbon",
                country = "Portugal",
                averageCost = BigDecimal("650.00"),
                climate = Climate.WARM,
                flightTimeHours = BigDecimal("2.8"),
                interests = mutableSetOf("food", "culture")
            )
        )

        val destinationId = requireNotNull(destination.id)

        mockMvc.perform(
            get("/api/destinations/{id}", destinationId)
        )
            .andExpect(status().isOk)
            .andExpect(
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath("\$.id").value(destinationId))
            .andExpect(jsonPath("\$.city").value("Lisbon"))
            .andExpect(jsonPath("\$.country").value("Portugal"))
            .andExpect(jsonPath("\$.climate").value("WARM"))
            .andExpect(jsonPath("\$.interests.length()").value(2))
    }

    @Test
    fun `returns structured 404 when destination does not exist`() {
        mockMvc.perform(
            get("/api/destinations/{id}", 999999)
        )
            .andExpect(status().isNotFound)
            .andExpect(
                content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
            )
            .andExpect(jsonPath("\$.status").value(404))
            .andExpect(jsonPath("\$.error").value("Not Found"))
            .andExpect(
                jsonPath("\$.message")
                    .value("Destination with id 999999 was not found")
            )
            .andExpect(
                jsonPath("\$.path")
                    .value("/api/destinations/999999")
            )
            .andExpect(jsonPath("\$.timestamp").exists())
    }

    @Test
    fun `filters destinations by budget climate and interest`() {
        destinationRepository.saveAllAndFlush(
            listOf(
                createDestination(
                    city = "Lisbon",
                    country = "Portugal",
                    cost = "650.00",
                    climate = Climate.WARM,
                    interests = arrayOf("food", "culture")
                ),
                createDestination(
                    city = "Barcelona",
                    country = "Spain",
                    cost = "720.00",
                    climate = Climate.WARM,
                    interests = arrayOf("food", "architecture")
                ),
                createDestination(
                    city = "Reykjavik",
                    country = "Iceland",
                    cost = "1100.00",
                    climate = Climate.COLD,
                    interests = arrayOf("nature", "culture")
                )
            )
        )

        mockMvc.perform(
            get("/api/destinations")
                .param("maxBudget", "700.00")
                .param("climate", "WARM")
                .param("interest", "FOOD")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.content.length()").value(1))
            .andExpect(jsonPath("\$.content[0].city").value("Lisbon"))
            .andExpect(jsonPath("\$.totalElements").value(1))
            .andExpect(jsonPath("\$.page").value(0))
            .andExpect(jsonPath("\$.first").value(true))
            .andExpect(jsonPath("\$.last").value(true))
    }

    @Test
    fun `returns destinations with pagination metadata`() {
        destinationRepository.saveAllAndFlush(
            listOf(
                createDestination(
                    city = "Lisbon",
                    country = "Portugal",
                    cost = "650.00",
                    climate = Climate.WARM,
                    interests = arrayOf("food")
                ),
                createDestination(
                    city = "Barcelona",
                    country = "Spain",
                    cost = "720.00",
                    climate = Climate.WARM,
                    interests = arrayOf("architecture")
                ),
                createDestination(
                    city = "Amsterdam",
                    country = "Netherlands",
                    cost = "800.00",
                    climate = Climate.MILD,
                    interests = arrayOf("culture")
                )
            )
        )

        mockMvc.perform(
            get("/api/destinations")
                .param("page", "0")
                .param("size", "2")
                .param("sort", "city,asc")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.content.length()").value(2))
            .andExpect(jsonPath("\$.content[0].city").value("Amsterdam"))
            .andExpect(jsonPath("\$.content[1].city").value("Barcelona"))
            .andExpect(jsonPath("\$.page").value(0))
            .andExpect(jsonPath("\$.size").value(2))
            .andExpect(jsonPath("\$.totalElements").value(3))
            .andExpect(jsonPath("\$.totalPages").value(2))
            .andExpect(jsonPath("\$.first").value(true))
            .andExpect(jsonPath("\$.last").value(false))
    }

    @Test
    fun `ranks destinations by recommendation score`() {
        destinationRepository.saveAllAndFlush(
            listOf(
                createDestination(
                    city = "Lisbon",
                    country = "Portugal",
                    cost = "650.00",
                    climate = Climate.WARM,
                    interests = arrayOf("food", "culture"),
                    flightTime = "2.8"
                ),
                createDestination(
                    city = "Barcelona",
                    country = "Spain",
                    cost = "720.00",
                    climate = Climate.WARM,
                    interests = arrayOf("food", "architecture"),
                    flightTime = "2.5"
                ),
                createDestination(
                    city = "Reykjavik",
                    country = "Iceland",
                    cost = "1100.00",
                    climate = Climate.COLD,
                    interests = arrayOf("culture", "nature"),
                    flightTime = "3.0"
                )
            )
        )

        mockMvc.perform(
            get("/api/destinations/recommendations")
                .param("budget", "800.00")
                .param("climate", "WARM")
                .param("maxFlightTimeHours", "4.0")
                .param("interests", "food,culture")
                .param("limit", "2")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.length()").value(2))
            .andExpect(
                jsonPath("\$[0].destination.city").value("Lisbon")
            )
            .andExpect(jsonPath("\$[0].score.budgetScore").value(35))
            .andExpect(jsonPath("\$[0].score.climateScore").value(25))
            .andExpect(jsonPath("\$[0].score.interestScore").value(30))
            .andExpect(jsonPath("\$[0].score.flightTimeScore").value(10))
            .andExpect(jsonPath("\$[0].score.total").value(100))
            .andExpect(
                jsonPath("\$[1].destination.city").value("Barcelona")
            )
            .andExpect(jsonPath("\$[1].score.total").value(85))
    }

    @Test
    fun `updates a destination and replaces its interests`() {
        val destination = destinationRepository.saveAndFlush(
            createDestination(
                city = "Lisbon",
                country = "Portugal",
                cost = "650.00",
                climate = Climate.WARM,
                interests = arrayOf("food", "culture"),
                flightTime = "2.8"
            )
        )

        val destinationId = requireNotNull(destination.id)

        val requestBody = """
            {
            "city": "Porto",
            "country": "Portugal",
            "averageCost": 580.00,
            "climate": "MILD",
            "flightTimeHours": 2.5,
            "interests": ["Wine", "Architecture"]
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/destinations/{id}", destinationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.id").value(destinationId))
            .andExpect(jsonPath("\$.city").value("Porto"))
            .andExpect(jsonPath("\$.averageCost").value(580.00))
            .andExpect(jsonPath("\$.climate").value("MILD"))
            .andExpect(jsonPath("\$.interests.length()").value(2))

        val updatedDestination = destinationRepository
            .findById(destinationId)
            .orElseThrow()

        assertEquals("Porto", updatedDestination.city)
        assertEquals(Climate.MILD, updatedDestination.climate)

        val savedInterests = jdbcTemplate.queryForList(
            """
                SELECT interest
                FROM destination_interests
                WHERE destination_id = ?
            """.trimIndent(),
            String::class.java,
            destinationId
        ).toSet()

        assertEquals(
            setOf("wine", "architecture"),
            savedInterests
        )
    }

    @Test
    fun `deletes a destination and its interests`() {
        val destination = destinationRepository.saveAndFlush(
            createDestination(
                city = "Lisbon",
                country = "Portugal",
                cost = "650.00",
                climate = Climate.WARM,
                interests = arrayOf("food", "culture")
            )
        )

        val destinationId = requireNotNull(destination.id)

        mockMvc.perform(
            delete("/api/destinations/{id}", destinationId)
        )
            .andExpect(status().isNoContent)

        assertFalse(destinationRepository.existsById(destinationId))

        val interestCount = jdbcTemplate.queryForObject(
            """
                SELECT COUNT(*)
                FROM destination_interests
                WHERE destination_id = ?
            """.trimIndent(),
            Long::class.java,
            destinationId
        )

        assertEquals(0L, interestCount)
    }

    @Test
    fun `returns 404 when updating a missing destination`() {
        val requestBody = """
            {
            "city": "Porto",
            "country": "Portugal",
            "averageCost": 580.00,
            "climate": "MILD",
            "flightTimeHours": 2.5,
            "interests": ["Wine"]
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/destinations/{id}", 999999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("\$.status").value(404))
            .andExpect(
                jsonPath("\$.message")
                    .value("Destination with id 999999 was not found")
            )
    }

    @Test
    fun `publishes OpenAPI documentation`() {
        mockMvc.perform(
            get("/api-docs")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("\$.info.title").value("TripMatch API"))
            .andExpect(
                jsonPath("\$.paths['/api/destinations']").exists()
            )
            .andExpect(
                jsonPath(
                    "\$.paths['/api/destinations/{id}']"
                ).exists()
            )
            .andExpect(
                jsonPath(
                    "\$.paths['/api/destinations/recommendations']"
                ).exists()
            )
            .andExpect(
                jsonPath(
                    "\$.paths['/api/destinations'].post.responses['201']"
                ).exists()
            )
            .andExpect(
                jsonPath(
                    "\$.paths['/api/destinations/{id}'].delete.responses['204']"
                ).exists()
            )
    }

    private fun createDestination(
        city: String,
        country: String,
        cost: String,
        climate: Climate,
        interests: Array<String>,
        flightTime: String = "2.5"
    ): Destination {
        return Destination(
            city = city,
            country = country,
            averageCost = BigDecimal(cost),
            climate = climate,
            flightTimeHours = BigDecimal(flightTime),
            interests = interests.toMutableSet()
        )
    }

}