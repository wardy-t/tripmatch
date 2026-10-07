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

        assertEquals(0, destinationRepository.count())
    }
}