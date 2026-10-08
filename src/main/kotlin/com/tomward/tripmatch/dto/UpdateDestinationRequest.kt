package com.tomward.tripmatch.dto

import com.tomward.tripmatch.model.Climate
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class UpdateDestinationRequest(
    @field:NotBlank
    @field:Size(max = 100)
    val city: String,

    @field:NotBlank
    @field:Size(max = 100)
    val country: String,

    @field:DecimalMin(value = "0.00", inclusive = true)
    @field:Digits(integer = 8, fraction = 2)
    val averageCost: BigDecimal,

    val climate: Climate,

    @field:DecimalMin("0.1")
    @field:Digits(integer = 3, fraction = 1)
    val flightTimeHours: BigDecimal,

    @field:NotEmpty
    @field:Size(max = 10)
    val interests: Set<
        @NotBlank
        @Size(max = 50)
        String
    >
)