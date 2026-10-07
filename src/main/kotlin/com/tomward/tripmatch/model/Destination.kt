package com.tomward.tripmatch.model

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "destinations")
class Destination(
    @field:Column(nullable = false, length = 100)
    var city: String,

    @field:Column(nullable = false, length = 100)
    var country: String,

    @field:Column(name = "average_cost", nullable = false, precision = 10, scale = 2)
    var averageCost: BigDecimal,

    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false, length = 20)
    var climate: Climate,

    @field:Column(name = "flight_time_hours", nullable = false, precision = 4, scale = 1)
    var flightTimeHours: BigDecimal,

    @field:ElementCollection(fetch = FetchType.LAZY)
    @field:CollectionTable(
        name = "destination_interests",
        joinColumns = [JoinColumn(name = "destination_id")]
    )
    @field:Column(name = "interest", nullable = false, length = 50)
    var interests: MutableSet<String> = mutableSetOf(),

    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @field:Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
)