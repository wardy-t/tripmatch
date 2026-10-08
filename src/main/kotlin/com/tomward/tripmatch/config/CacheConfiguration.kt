package com.tomward.tripmatch.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.cache.interceptor.KeyGenerator
import org.springframework.context.annotation.Bean

@Configuration
@EnableCaching
class CacheConfiguration {

    @Bean
    fun recommendationCacheKeyGenerator(): KeyGenerator =
        RecommendationCacheKeyGenerator()
}