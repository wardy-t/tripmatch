package com.tomward.tripmatch.repository

import com.tomward.tripmatch.model.Destination
import org.springframework.data.jpa.repository.JpaRepository
import com.tomward.tripmatch.model.Climate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.math.BigDecimal

interface DestinationRepository : JpaRepository<Destination, Long> {

    fun findByCityIgnoreCaseAndCountryIgnoreCase(
        city: String,
        country: String
    ): Destination?

    @Query(
        value = """
            SELECT DISTINCT destination
            FROM Destination destination
            LEFT JOIN destination.interests interest
            WHERE (:maxBudget IS NULL OR destination.averageCost <= :maxBudget)
            AND (:climate IS NULL OR destination.climate = :climate)
            AND (
                :interest IS NULL
                OR LOWER(interest) = :interest
            )
        """,
        countQuery = """
            SELECT COUNT(DISTINCT destination)
            FROM Destination destination
            LEFT JOIN destination.interests interest
            WHERE (:maxBudget IS NULL OR destination.averageCost <= :maxBudget)
            AND (:climate IS NULL OR destination.climate = :climate)
            AND (
                :interest IS NULL
                OR LOWER(interest) = :interest
            )
        """
    )
    fun findFiltered(
        @Param("maxBudget") maxBudget: BigDecimal?,
        @Param("climate") climate: Climate?,
        @Param("interest") interest: String?,
        pageable: Pageable
    ): Page<Destination>
}