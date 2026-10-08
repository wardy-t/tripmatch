package com.tomward.tripmatch.exception

import java.time.OffsetDateTime
import java.time.ZoneOffset

data class ApiError(
    val timestamp: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: Map<String, String> = emptyMap()
)