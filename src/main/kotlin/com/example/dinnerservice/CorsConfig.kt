package com.example.dinnerservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(@Value("\${app.cors.allowed-origins:}") private val allowedOrigins: String) {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowCredentials = true
        if (allowedOrigins.isNotBlank()) {
            allowedOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                config.addAllowedOrigin(it)
            }
        }
        config.addAllowedHeader("*")
        config.addAllowedMethod("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/api/**", config)
        return source
    }
}
