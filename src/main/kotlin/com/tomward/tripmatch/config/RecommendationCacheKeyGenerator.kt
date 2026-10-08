package com.tomward.tripmatch.config

import com.tomward.tripmatch.model.Climate
import org.springframework.cache.interceptor.KeyGenerator
import java.lang.reflect.Method
import java.math.BigDecimal

class RecommendationCacheKeyGenerator : KeyGenerator {

    override fun generate(
        target: Any,
        method: Method,
        vararg params: Any?
    ): Any {
        val budget = params[0] as BigDecimal
        val climate = params[1] as Climate
        val maxFlightTimeHours = params[2] as BigDecimal

        val interests = (params[3] as Set<*>)
            .map { it.toString().trim().lowercase() }
            .filter { it.isNotEmpty() }
            .sorted()

        val limit = params[4] as Int

        return RecommendationCacheKey(
            budget = budget.stripTrailingZeros().toPlainString(),
            climate = climate.name,
            maxFlightTimeHours = maxFlightTimeHours
                .stripTrailingZeros()
                .toPlainString(),
            interests = interests,
            limit = limit
        )
    }
}