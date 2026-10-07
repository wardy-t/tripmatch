package com.tomward.tripmatch.repository

import com.tomward.tripmatch.model.Destination
import org.springframework.data.jpa.repository.JpaRepository

interface DestinationRepository : JpaRepository<Destination, Long> {

    fun findByCityIgnoreCaseAndCountryIgnoreCase(
        city: String,
        country: String
    ): Destination?
}