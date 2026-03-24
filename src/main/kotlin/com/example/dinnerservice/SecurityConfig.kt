package com.example.dinnerservice

import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(private val jwtFilter: JwtFilter) {

    @Autowired
    private lateinit var corsConfigurationSource: CorsConfigurationSource

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF disabled: JWT in Authorization header is not vulnerable to CSRF
            // (no cookies are used for auth)
            .csrf { it.disable() }
            // Wire CORS into the Security filter chain so preflight OPTIONS requests
            // are handled before Spring Security blocks them
            .cors { it.configurationSource(corsConfigurationSource) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Don't apply security to FORWARD/ERROR dispatches (avoids StackOverflow
                    // when WelcomePageHandlerMapping forwards / → /index.html)
                    .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                    // Public API endpoints
                    .requestMatchers("/api/auth/**", "/api/recipe-images/**", "/actuator/info").permitAll()
                    // All other /api/** endpoints require authentication
                    .requestMatchers("/api/**").authenticated()
                    // Everything else (static files, SPA routes) is public —
                    // the React client handles auth redirects via ProtectedRoute
                    .anyRequest().permitAll()
            }
            // Return 401 (not 403) for unauthenticated requests
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
