# Phase 1: Backend REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor dinner-service from session-based Thymeleaf MVC into a pure stateless REST API using JWT authentication, fixing security and reliability issues along the way.

**Architecture:** All controllers become `@RestController` under `/api/**`. Auth uses a `JwtFilter` that reads `Authorization: Bearer <token>` and populates Spring Security's `SecurityContextHolder`. Session is stateless; CSRF is explicitly disabled. Flyway manages schema migrations. Thymeleaf and all HTML templates are removed.

**Tech Stack:** Kotlin, Spring Boot 3.2, Spring Security 6, JJWT 0.12.6, Flyway 9, PostgreSQL, JUnit 5 + Testcontainers 1.19 + Spring Boot Test

**Spec:** `docs/superpowers/specs/2026-03-24-dinner-service-improvements-design.md`

---

## File Map

**New files:**
- `src/main/kotlin/com/example/dinnerservice/JwtUtil.kt`
- `src/main/kotlin/com/example/dinnerservice/JwtFilter.kt`
- `src/main/kotlin/com/example/dinnerservice/SecurityConfig.kt`
- `src/main/kotlin/com/example/dinnerservice/CurrentUserService.kt`
- `src/main/kotlin/com/example/dinnerservice/GlobalExceptionHandler.kt`
- `src/main/kotlin/com/example/dinnerservice/CorsConfig.kt`
- `src/main/kotlin/com/example/dinnerservice/Dto.kt`
- `src/main/resources/db/migration/V1__initial_schema.sql`
- `src/test/kotlin/com/example/dinnerservice/IntegrationTestBase.kt`
- `src/test/kotlin/com/example/dinnerservice/JwtUtilTest.kt`
- `src/test/kotlin/com/example/dinnerservice/AuthControllerTest.kt`
- `src/test/kotlin/com/example/dinnerservice/ProductControllerTest.kt`
- `src/test/kotlin/com/example/dinnerservice/RecipeControllerTest.kt`
- `src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt`
- `src/test/kotlin/com/example/dinnerservice/UserControllerTest.kt`
- `src/test/resources/application-test.properties`

**Modified files:**
- `pom.xml` — add security/jjwt/flyway/testcontainers deps; remove thymeleaf
- `src/main/resources/application.properties` — fix ddl-auto, show-sql, add jwt-secret/cors/flyway props
- `deploy.sh` — switch from `mvn spring-boot:run` to fat JAR
- `src/main/kotlin/com/example/dinnerservice/AuthController.kt` — full rewrite
- `src/main/kotlin/com/example/dinnerservice/ProductController.kt` — full rewrite
- `src/main/kotlin/com/example/dinnerservice/RecipeController.kt` — full rewrite
- `src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt` — full rewrite
- `src/main/kotlin/com/example/dinnerservice/UserController.kt` — full rewrite

**Deleted files:**
- `src/main/kotlin/com/example/dinnerservice/HomeController.kt`
- `src/main/kotlin/com/example/dinnerservice/Guest.kt`
- `src/main/kotlin/com/example/dinnerservice/GuestRepository.kt`
- All files under `src/main/resources/templates/`

---

### Task 1: Update dependencies

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Replace Thymeleaf with Spring Security + JJWT + Flyway + test dependencies**

Replace the entire `<dependencies>` block in `pom.xml` and add a `<dependencyManagement>` section. The full new `pom.xml` content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.3</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>dinner-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>17</java.version>
        <kotlin.version>1.9.22</kotlin.version>
        <jjwt.version>0.12.6</jjwt.version>
        <testcontainers.version>1.19.7</testcontainers.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>${jjwt.version}</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Flyway -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Mail -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>

        <!-- Kotlin -->
        <dependency>
            <groupId>com.fasterxml.jackson.module</groupId>
            <artifactId>jackson-module-kotlin</artifactId>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-reflect</artifactId>
        </dependency>
        <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-stdlib</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
        <testSourceDirectory>${project.basedir}/src/test/kotlin</testSourceDirectory>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.jetbrains.kotlin</groupId>
                <artifactId>kotlin-maven-plugin</artifactId>
                <configuration>
                    <args>
                        <arg>-Xjsr305=strict</arg>
                    </args>
                    <compilerPlugins>
                        <plugin>spring</plugin>
                        <plugin>jpa</plugin>
                    </compilerPlugins>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-allopen</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                    <dependency>
                        <groupId>org.jetbrains.kotlin</groupId>
                        <artifactId>kotlin-maven-noarg</artifactId>
                        <version>${kotlin.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Verify the project still compiles (tests will fail — that's fine for now)**

```bash
cd dinner-service
mvn compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "chore: update deps — add security/jwt/flyway/testcontainers, remove thymeleaf"
```

---

### Task 2: Fix application configuration and deploy script

**Files:**
- Modify: `src/main/resources/application.properties`
- Modify: `deploy.sh`

- [ ] **Step 1: Update application.properties**

Replace the full content of `src/main/resources/application.properties`:

```properties
spring.application.name=dinner-service
server.port=8090

spring.datasource.url=jdbc:postgresql://localhost:5432/dinnerservice
spring.datasource.username=dinneruser
spring.datasource.password=dinnerpass
spring.datasource.driver-class-name=org.postgresql.Driver

# Schema is managed by Flyway — never auto-alter tables
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway — set baseline-on-migrate=true for first run on existing database
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1

# Mail — set real values in application-local.properties (git-ignored)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File uploads
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=50MB
app.upload-dir=./uploads

# JWT — must be set in application-local.properties, minimum 32 characters
# app.jwt-secret=REPLACE_ME_IN_LOCAL_PROPERTIES

# CORS — comma-separated allowed origins for dev (e.g. http://localhost:5173)
app.cors.allowed-origins=

spring.config.import=optional:file:./application-local.properties
```

- [ ] **Step 2: Add jwt-secret to application-local.properties (not committed)**

Open `application-local.properties` (create if it doesn't exist — it is git-ignored) and add:
```properties
app.jwt-secret=<generate a random string of at least 32 characters>
app.cors.allowed-origins=http://localhost:5173
```
Generate a secret with: `openssl rand -base64 32`

- [ ] **Step 3: Update deploy.sh to use fat JAR**

Replace `deploy.sh` content:

```bash
#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

cd /projects/dinner-service

# Pull and check if anything changed
git fetch origin master
CHANGES=$(git log HEAD..origin/master --oneline)

if [ -n "$CHANGES" ]; then
    echo "[$(date)] Changes detected, pulling and building..."
    git pull origin master

    # Build fat JAR (skip tests for speed; CI should run tests separately)
    mvn package -DskipTests -q

    # Kill existing instance
    pkill -f "dinner-service.*\.jar" 2>/dev/null
    sleep 2

    # Start service in background
    JAR=$(ls target/dinner-service-*.jar | head -1)
    nohup java -jar "$JAR" > /projects/dinner-service/service.log 2>&1 &
    echo "[$(date)] Service restarted (PID $!)"
else
    echo "[$(date)] No changes."
fi
```

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/application.properties deploy.sh
git commit -m "fix: switch to ddl-auto=validate, Flyway config, and fat JAR deploy"
```

---

### Task 3: Create Flyway V1 migration

**Files:**
- Create: `src/main/resources/db/migration/V1__initial_schema.sql`

- [ ] **Step 1: Generate the schema from the existing database**

Run this on your production/dev PostgreSQL database to get the current schema:
```bash
pg_dump --schema-only --no-owner --no-acl -U dinneruser -d dinnerservice > /tmp/schema.sql
```

Review the output. The migration file should contain the `CREATE TABLE` statements for all current tables. Place the content into `src/main/resources/db/migration/V1__initial_schema.sql`.

The schema should match these entities (adjust from the `pg_dump` output — do not copy this verbatim if the actual schema differs):

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    default_list_id BIGINT
);

CREATE TABLE guests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(50) NOT NULL DEFAULT '#cccccc'
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    price DOUBLE PRECISION,
    category_id BIGINT REFERENCES categories(id)
);

CREATE TABLE shopping_lists (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id BIGINT REFERENCES users(id)
);

CREATE TABLE shopping_list_shares (
    list_id BIGINT REFERENCES shopping_lists(id),
    user_id BIGINT REFERENCES users(id),
    PRIMARY KEY (list_id, user_id)
);

CREATE TABLE shopping_list_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    count DOUBLE PRECISION,
    unit_price DOUBLE PRECISION,
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    category_id BIGINT REFERENCES categories(id),
    added_by_id BIGINT REFERENCES users(id),
    list_id BIGINT REFERENCES shopping_lists(id)
);

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT REFERENCES recipes(id),
    product_id BIGINT REFERENCES products(id),
    quantity DOUBLE PRECISION,
    unit VARCHAR(50)
);

CREATE TABLE recipe_images (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT REFERENCES recipes(id),
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL
);
```

- [ ] **Step 2: Verify Flyway runs cleanly against the existing database**

Start the app and check the logs for Flyway output:
```bash
mvn spring-boot:run -pl . 2>&1 | grep -i flyway
```
Expected: `Successfully applied 1 migration to schema "public"` (or `Flyway baseline established` on the first run with `baseline-on-migrate=true`)

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V1__initial_schema.sql
git commit -m "feat: add Flyway V1 initial schema migration"
```

---

### Task 4: JWT utilities

**Files:**
- Create: `src/main/kotlin/com/example/dinnerservice/JwtUtil.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/JwtUtilTest.kt`

- [ ] **Step 1: Write the failing test**

Create `src/test/kotlin/com/example/dinnerservice/JwtUtilTest.kt`:

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class JwtUtilTest {

    private val validSecret = "test-secret-that-is-at-least-32-characters-long"
    private val jwtUtil = JwtUtil(validSecret)

    @Test
    fun `generateToken produces a valid token containing the email`() {
        val token = jwtUtil.generateToken("user@test.com")
        assertThat(jwtUtil.isValid(token)).isTrue()
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com")
    }

    @Test
    fun `isValid returns false for a tampered token`() {
        val token = jwtUtil.generateToken("user@test.com") + "tampered"
        assertThat(jwtUtil.isValid(token)).isFalse()
    }

    @Test
    fun `isValid returns false for a garbage string`() {
        assertThat(jwtUtil.isValid("not.a.jwt")).isFalse()
    }

    @Test
    fun `constructor fails fast when secret is shorter than 32 characters`() {
        assertThatThrownBy { JwtUtil("tooshort") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("32")
    }
}
```

- [ ] **Step 2: Run to confirm it fails**

```bash
mvn test -pl . -Dtest=JwtUtilTest -q 2>&1 | tail -5
```
Expected: compilation error — `JwtUtil` does not exist yet.

- [ ] **Step 3: Implement JwtUtil**

Create `src/main/kotlin/com/example/dinnerservice/JwtUtil.kt`:

```kotlin
package com.example.dinnerservice

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(@Value("\${app.jwt-secret}") private val secret: String) {

    init {
        require(secret.toByteArray().size >= 32) {
            "app.jwt-secret must produce at least 32 bytes (256 bits) for HS256"
        }
    }

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val expiryMs = 7 * 24 * 60 * 60 * 1000L // 7 days

    fun generateToken(email: String): String =
        Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiryMs))
            .signWith(key)
            .compact()

    fun extractEmail(token: String): String =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun isValid(token: String): Boolean = try {
        extractEmail(token)
        true
    } catch (e: Exception) {
        false
    }
}
```

- [ ] **Step 4: Create test properties so JwtUtil can be constructed in tests**

Create `src/test/resources/application-test.properties`:

```properties
app.jwt-secret=test-secret-that-is-at-least-32-characters-long
spring.mail.host=localhost
spring.mail.port=3025
app.upload-dir=./test-uploads
app.cors.allowed-origins=http://localhost:5173
```

- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=JwtUtilTest -q 2>&1 | tail -5
```
Expected: `Tests run: 4, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/JwtUtil.kt \
        src/test/kotlin/com/example/dinnerservice/JwtUtilTest.kt \
        src/test/resources/application-test.properties
git commit -m "feat: add JwtUtil with 32-char secret validation"
```

---

### Task 5: Spring Security infrastructure

**Files:**
- Create: `src/main/kotlin/com/example/dinnerservice/JwtFilter.kt`
- Create: `src/main/kotlin/com/example/dinnerservice/SecurityConfig.kt`
- Create: `src/main/kotlin/com/example/dinnerservice/CurrentUserService.kt`
- Create: `src/main/kotlin/com/example/dinnerservice/GlobalExceptionHandler.kt`
- Create: `src/main/kotlin/com/example/dinnerservice/CorsConfig.kt`

No isolated unit tests for these — they are validated through the controller integration tests in Tasks 7–11.

- [ ] **Step 1: Create JwtFilter**

```kotlin
package com.example.dinnerservice

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(private val jwtUtil: JwtUtil) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ")
            if (jwtUtil.isValid(token)) {
                val email = jwtUtil.extractEmail(token)
                val auth = UsernamePasswordAuthenticationToken(email, null, emptyList())
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        chain.doFilter(request, response)
    }
}
```

- [ ] **Step 2: Create SecurityConfig**

```kotlin
package com.example.dinnerservice

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
                    .requestMatchers(
                        "/api/auth/**",
                        "/api/recipe-images/**",
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/favicon.ico"
                    ).permitAll()
                    .anyRequest().authenticated()
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
```

- [ ] **Step 3: Create CurrentUserService**

```kotlin
package com.example.dinnerservice

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService(private val userRepository: UserRepository) {

    /**
     * Returns the authenticated user. Throws AccessDeniedException if not authenticated,
     * or IllegalStateException if the JWT email doesn't match any user.
     */
    fun currentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.principal as? String
            ?: throw AccessDeniedException("Not authenticated")
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalStateException("Authenticated user not found in database: $email") }
    }
}
```

- [ ] **Step 4: Create GlobalExceptionHandler**

```kotlin
package com.example.dinnerservice

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

data class ErrorResponse(val error: String, val message: String)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException::class)
    fun handle(ex: ResponseStatusException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(ex.statusCode)
            .body(ErrorResponse(ex.statusCode.toString(), ex.reason ?: ex.message))

    @ExceptionHandler(EntityNotFoundException::class)
    fun handle(ex: EntityNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("NOT_FOUND", ex.message ?: "Resource not found"))

    @ExceptionHandler(AccessDeniedException::class)
    fun handle(ex: AccessDeniedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse("FORBIDDEN", ex.message ?: "Access denied"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handle(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val fields = ex.bindingResult.allErrors.joinToString("; ") { error ->
            if (error is FieldError) "${error.field}: ${error.defaultMessage}"
            else error.defaultMessage ?: "Invalid value"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_ERROR", fields))
    }

    @ExceptionHandler(Exception::class)
    fun handle(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
}
```

- [ ] **Step 5: Create CorsConfig**

Note: expose a `CorsConfigurationSource` bean (not a `CorsFilter`) so Spring Security can wire it directly into its filter chain via `.cors { it.configurationSource(...) }`.

```kotlin
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
```

- [ ] **Step 6: Verify the project compiles**

```bash
mvn compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/JwtFilter.kt \
        src/main/kotlin/com/example/dinnerservice/SecurityConfig.kt \
        src/main/kotlin/com/example/dinnerservice/CurrentUserService.kt \
        src/main/kotlin/com/example/dinnerservice/GlobalExceptionHandler.kt \
        src/main/kotlin/com/example/dinnerservice/CorsConfig.kt
git commit -m "feat: add JWT filter, Spring Security config, CurrentUserService, CORS, exception handler"
```

---

### Task 6: DTO data classes

**Files:**
- Create: `src/main/kotlin/com/example/dinnerservice/Dto.kt`

- [ ] **Step 1: Create all DTOs in a single file**

```kotlin
package com.example.dinnerservice

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    @field:NotBlank val confirmPassword: String
)

data class AuthResponse(val token: String, val email: String)

data class ForgotPasswordRequest(@field:NotBlank @field:Email val email: String)

data class ResetPasswordRequest(
    @field:NotBlank val token: String,
    @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    @field:NotBlank val confirmPassword: String
)

// ── Recipe ────────────────────────────────────────────────────────────────────

data class RecipeSummaryDto(val id: Long, val name: String, val description: String, val previewImage: String?)

data class IngredientDto(val id: Long, val productId: Long?, val productName: String?, val quantity: Double?, val unit: String)

data class RecipeImageDto(val id: Long, val filename: String, val originalName: String)

data class RecipeDetailDto(
    val id: Long,
    val name: String,
    val description: String,
    val ingredients: List<IngredientDto>,
    val images: List<RecipeImageDto>,
    val shoppingLists: List<ShoppingListSummaryDto>,
    val selectedListId: Long?,
    val ingredientCounts: Map<Long, Int>
)

data class CreateRecipeRequest(@field:NotBlank val name: String, val description: String = "")

data class AddIngredientRequest(
    val productId: Long,
    val quantity: Double?,
    @field:NotBlank val unit: String
)

data class SelectListRequest(val listId: Long?)

// ── Shopping List ─────────────────────────────────────────────────────────────

data class ShoppingListSummaryDto(val id: Long, val name: String, val ownerEmail: String?)

data class ShoppingListItemDto(
    val id: Long,
    val name: String,
    val count: Double?,
    val unitPrice: Double?,
    val totalPrice: Double?,
    val checked: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: String?,
    val addedByEmail: String?
)

data class ShoppingListDetailDto(
    val id: Long,
    val name: String,
    val items: List<ShoppingListItemDto>,
    val totalPrice: Double,
    val isOwner: Boolean
)

data class ShoppingListsResponse(
    val owned: List<ShoppingListSummaryDto>,
    val shared: List<ShoppingListSummaryDto>
)

data class CreateShoppingListRequest(@field:NotBlank val name: String)

data class AddItemRequest(
    @field:NotBlank val name: String,
    val count: Double?,
    val unitPrice: Double?
)

data class ShareRequest(@field:NotBlank @field:Email val email: String)

// ── Product ───────────────────────────────────────────────────────────────────

data class ProductDto(
    val id: Long,
    val name: String,
    val price: Double?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: String?
)

data class CategoryDto(val id: Long, val name: String, val color: String)

data class CreateProductRequest(
    @field:NotBlank val name: String,
    val price: Double?,
    val categoryId: Long?
)

data class CreateCategoryRequest(
    @field:NotBlank val name: String,
    val color: String = "#cccccc"
)

// ── User ──────────────────────────────────────────────────────────────────────

data class UserProfileDto(
    val email: String,
    val defaultListId: Long?,
    val allLists: List<ShoppingListSummaryDto>,
    val sharedLists: List<ShoppingListSummaryDto>
)

data class SetDefaultListRequest(val listId: Long?)
```

- [ ] **Step 2: Verify the project compiles**

```bash
mvn compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/Dto.kt
git commit -m "feat: add all request/response DTO data classes"
```

---

### Task 7: Integration test base class

**Files:**
- Create: `src/test/kotlin/com/example/dinnerservice/IntegrationTestBase.kt`

- [ ] **Step 1: Create IntegrationTestBase**

```kotlin
package com.example.dinnerservice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("dinnerservice_test")
            withUsername("test")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            // Flyway creates the schema from scratch in tests — no baseline needed
            registry.add("spring.flyway.baseline-on-migrate") { "false" }
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jwtUtil: JwtUtil

    private val encoder = BCryptPasswordEncoder()

    /**
     * Creates a user in the database and returns a valid JWT for that user.
     * Default password is "password123".
     */
    fun createUserAndToken(email: String = "user@test.com", password: String = "password123"): String {
        userRepository.save(User(email = email, passwordHash = encoder.encode(password)))
        return jwtUtil.generateToken(email)
    }

    /** Returns HttpHeaders with the Bearer token set. */
    fun authHeaders(token: String): HttpHeaders =
        HttpHeaders().apply { setBearerAuth(token) }

    /** Wraps a body with auth headers into an HttpEntity for use with restTemplate. */
    fun <T> authEntity(token: String, body: T? = null): HttpEntity<T> =
        HttpEntity(body, authHeaders(token))
}
```

- [ ] **Step 2: Verify it compiles**

```bash
mvn test-compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/test/kotlin/com/example/dinnerservice/IntegrationTestBase.kt
git commit -m "test: add IntegrationTestBase with Testcontainers PostgreSQL"
```

---

### Task 8: AuthController — rewrite and test (TDD)

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/AuthController.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/AuthControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

Create `src/test/kotlin/com/example/dinnerservice/AuthControllerTest.kt`:

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class AuthControllerTest : IntegrationTestBase() {

    @Autowired
    lateinit var resetTokenRepository: PasswordResetTokenRepository

    @BeforeEach
    fun cleanup() {
        resetTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST login returns JWT for valid credentials`() {
        createUserAndToken("alice@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("alice@test.com", "password123"),
            AuthResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.token).isNotBlank()
        assertThat(response.body?.email).isEqualTo("alice@test.com")
    }

    @Test
    fun `POST login returns 401 for wrong password`() {
        createUserAndToken("alice@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("alice@test.com", "wrongpassword"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `POST login returns 401 for unknown email`() {
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("nobody@test.com", "password123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `POST register creates user and returns JWT`() {
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("new@test.com", "password123", "password123"),
            AuthResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.token).isNotBlank()
        assertThat(userRepository.findByEmail("new@test.com").isPresent).isTrue()
    }

    @Test
    fun `POST register returns 409 for duplicate email`() {
        createUserAndToken("existing@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("existing@test.com", "password123", "password123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `POST register returns 400 when passwords do not match`() {
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("new@test.com", "password123", "different123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `authenticated endpoint returns 401 without token`() {
        val response = restTemplate.getForEntity("/api/recipes", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
```

- [ ] **Step 2: Run to confirm they fail**

```bash
mvn test -pl . -Dtest=AuthControllerTest -q 2>&1 | tail -10
```
Expected: test failures (AuthController still returns Thymeleaf redirects, not JSON)

- [ ] **Step 3: Rewrite AuthController as @RestController**

Replace the entire content of `src/main/kotlin/com/example/dinnerservice/AuthController.kt`:

```kotlin
package com.example.dinnerservice

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val mailSender: JavaMailSender,
    private val jwtUtil: JwtUtil
) {
    private val encoder = BCryptPasswordEncoder()

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(req.email.trim().lowercase())
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials") }
        if (user.passwordHash == null || !encoder.matches(req.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        return ResponseEntity.ok(AuthResponse(jwtUtil.generateToken(user.email), user.email))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        if (req.password != req.confirmPassword) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match")
        }
        val email = req.email.trim().lowercase()
        if (userRepository.findByEmail(email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
        }
        val user = userRepository.save(User(email = email, passwordHash = encoder.encode(req.password)))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse(jwtUtil.generateToken(user.email), user.email))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> = ResponseEntity.noContent().build()

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody req: ForgotPasswordRequest,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByEmail(req.email.trim().lowercase()).orElse(null)
        if (user != null) {
            resetTokenRepository.deleteByUser(user)
            val token = UUID.randomUUID().toString()
            resetTokenRepository.save(PasswordResetToken(token = token, user = user))

            val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"
            val resetUrl = "$baseUrl/reset-password?token=$token"

            val message = SimpleMailMessage()
            message.setTo(user.email)
            message.subject = "Dinner Service – Password Reset"
            message.text = "Click the link below to reset your password (expires in 1 hour):\n\n$resetUrl"
            mailSender.send(message)
        }
        // Always return success to avoid revealing whether an email exists
        return ResponseEntity.ok(mapOf("message" to "If that email exists, a reset link has been sent."))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest): ResponseEntity<Map<String, String>> {
        if (req.password != req.confirmPassword) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match")
        }
        val resetToken = resetTokenRepository.findByToken(req.token).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_INVALID")
        if (resetToken.expiresAt.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED")
        }
        val user = resetToken.user
        user.passwordHash = encoder.encode(req.password)
        userRepository.save(user)
        resetTokenRepository.delete(resetToken)
        return ResponseEntity.ok(mapOf("message" to "Password reset successfully."))
    }
}
```

Note: the old `/dashboard`, `/login` (GET), `/register` (GET) page routes and the `logout` form POST are removed. The React SPA handles all page routing.

Note: `forgot-password` and `reset-password` endpoints require sending email (the `JavaMailSender` bean). Integration-testing them fully requires a Greenmail or mock SMTP server. These flows are not covered in the automated tests in this plan — manual smoke-testing after deployment is sufficient for the test-group scope. If email sending is stubbed out with a `NoOpMailSender` in test config, add tests for the token validation logic.

Also note: `PasswordResetToken` stores `expiresAt` as `LocalDateTime`. For timezone safety, ensure the entity default uses `LocalDateTime.now(ZoneOffset.UTC).plusHours(1)` in its constructor default value.

- [ ] **Step 4: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=AuthControllerTest -q 2>&1 | tail -10
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/AuthController.kt \
        src/test/kotlin/com/example/dinnerservice/AuthControllerTest.kt
git commit -m "feat: rewrite AuthController as REST API with JWT"
```

---

### Task 9: ProductController — rewrite and test (TDD)

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/ProductController.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/ProductControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ProductControllerTest : IntegrationTestBase() {

    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var categoryRepository: CategoryRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET products returns empty list initially`() {
        val response = restTemplate.exchange(
            "/api/products", HttpMethod.GET, authEntity(token),
            object : ParameterizedTypeReference<List<ProductDto>>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }

    @Test
    fun `POST products creates a product`() {
        val response = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Milk", 1.5, null)),
            ProductDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Milk")
        assertThat(response.body?.price).isEqualTo(1.5)
    }

    @Test
    fun `POST categories creates a category`() {
        val response = restTemplate.postForEntity(
            "/api/categories", authEntity(token, CreateCategoryRequest("Dairy", "#ffffff")),
            CategoryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Dairy")
    }

    @Test
    fun `GET products search returns matching products`() {
        restTemplate.postForEntity("/api/products", authEntity(token, CreateProductRequest("Whole Milk", null, null)), ProductDto::class.java)
        restTemplate.postForEntity("/api/products", authEntity(token, CreateProductRequest("Butter", null, null)), ProductDto::class.java)

        val response = restTemplate.exchange(
            "/api/products/search?q=milk", HttpMethod.GET, authEntity(token),
            object : ParameterizedTypeReference<List<ProductDto>>() {}
        )
        assertThat(response.body).hasSize(1)
        assertThat(response.body?.first()?.name).isEqualTo("Whole Milk")
    }

    @Test
    fun `PUT products updates name and category`() {
        val category = categoryRepository.save(Category(name = "Dairy", color = "#fff"))
        val created = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Mlk", null, null)),
            ProductDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/products/${created.id}", HttpMethod.PUT,
            authEntity(token, CreateProductRequest("Milk", null, category.id)),
            ProductDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.name).isEqualTo("Milk")
        assertThat(response.body?.categoryName).isEqualTo("Dairy")
    }

    @Test
    fun `DELETE products removes the product`() {
        val created = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Temp", null, null)),
            ProductDto::class.java
        ).body!!

        val deleteResponse = restTemplate.exchange(
            "/api/products/${created.id}", HttpMethod.DELETE, authEntity(token), Void::class.java
        )
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(productRepository.findById(created.id).isEmpty).isTrue()
    }
}
```

- [ ] **Step 2: Run to confirm they fail**

```bash
mvn test -pl . -Dtest=ProductControllerTest -q 2>&1 | tail -10
```

- [ ] **Step 3: Rewrite ProductController**

Replace the full content of `src/main/kotlin/com/example/dinnerservice/ProductController.kt`:

```kotlin
package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ProductController(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository
) {

    private fun Product.toDto() = ProductDto(
        id = id, name = name, price = price,
        categoryId = category?.id, categoryName = category?.name, categoryColor = category?.color
    )

    private fun Category.toDto() = CategoryDto(id = id, name = name, color = color)

    @GetMapping("/products")
    fun listProducts(): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findAll()
                .sortedWith(compareBy({ it.category?.name ?: "zzz" }, { it.name.lowercase() }))
                .map { it.toDto() }
        )

    @GetMapping("/products/uncategorized")
    fun uncategorized(): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findAll()
                .filter { it.category == null }
                .sortedBy { it.name.lowercase() }
                .map { it.toDto() }
        )

    @GetMapping("/products/search")
    fun search(@RequestParam q: String): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findByNameContainingIgnoreCase(q)
                .sortedBy { it.name.lowercase() }
                .map { it.toDto() }
        )

    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody req: CreateProductRequest): ResponseEntity<ProductDto> {
        val trimmed = req.name.trim()
        if (productRepository.findByNameIgnoreCase(trimmed).isPresent) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
        val category = req.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val product = productRepository.save(Product(name = trimmed, price = req.price, category = category))
        return ResponseEntity.status(HttpStatus.CREATED).body(product.toDto())
    }

    @PutMapping("/products/{id}")
    fun editProduct(
        @PathVariable id: Long,
        @Valid @RequestBody req: CreateProductRequest
    ): ResponseEntity<ProductDto> {
        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val category = req.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val trimmed = req.name.trim()
        val updated = productRepository.save(Product(id = product.id, name = trimmed, price = req.price, category = category))
        // Sync category on existing shopping list items with this product name
        shoppingListItemRepository.findByNameIgnoreCase(trimmed).forEach { item ->
            shoppingListItemRepository.save(
                ShoppingListItem(id = item.id, name = item.name, count = item.count,
                    unitPrice = item.unitPrice, checked = item.checked, category = category,
                    addedBy = item.addedBy, shoppingList = item.shoppingList)
            )
        }
        return ResponseEntity.ok(updated.toDto())
    }

    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build()
        productRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/categories")
    fun listCategories(): ResponseEntity<List<CategoryDto>> =
        ResponseEntity.ok(
            categoryRepository.findAll().sortedBy { it.name.lowercase() }.map { it.toDto() }
        )

    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody req: CreateCategoryRequest): ResponseEntity<CategoryDto> {
        val category = categoryRepository.save(Category(name = req.name.trim(), color = req.color))
        return ResponseEntity.status(HttpStatus.CREATED).body(category.toDto())
    }

    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build()
        categoryRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=ProductControllerTest -q 2>&1 | tail -10
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/ProductController.kt \
        src/test/kotlin/com/example/dinnerservice/ProductControllerTest.kt
git commit -m "feat: rewrite ProductController as REST API"
```

---

### Task 10: RecipeController — rewrite and test (TDD)

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/RecipeController.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/RecipeControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class RecipeControllerTest : IntegrationTestBase() {

    @Autowired lateinit var recipeRepository: RecipeRepository
    @Autowired lateinit var recipeIngredientRepository: RecipeIngredientRepository
    @Autowired lateinit var recipeImageRepository: RecipeImageRepository
    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var shoppingListRepository: ShoppingListRepository
    @Autowired lateinit var shoppingListItemRepository: ShoppingListItemRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListItemRepository.deleteAll()
        recipeIngredientRepository.deleteAll()
        recipeImageRepository.deleteAll()
        recipeRepository.deleteAll()
        productRepository.deleteAll()
        shoppingListRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET recipes returns empty list initially`() {
        val response = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET, authEntity(token),
            object : ParameterizedTypeReference<List<RecipeSummaryDto>>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }

    @Test
    fun `POST recipes creates recipe and returns it`() {
        val response = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta", "Quick dinner")),
            RecipeSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Pasta")
        assertThat(response.body?.description).isEqualTo("Quick dinner")
    }

    @Test
    fun `GET recipe by id returns detail with empty ingredients`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta")),
            RecipeSummaryDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.GET, authEntity(token),
            RecipeDetailDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.ingredients).isEmpty()
    }

    @Test
    fun `PUT recipe updates name and description`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Old Name")),
            RecipeSummaryDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.PUT,
            authEntity(token, CreateRecipeRequest("New Name", "Updated desc")),
            RecipeSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.name).isEqualTo("New Name")
    }

    @Test
    fun `DELETE recipe removes it`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Temp")),
            RecipeSummaryDto::class.java
        ).body!!

        val deleteResponse = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.DELETE, authEntity(token), Void::class.java
        )
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(recipeRepository.findById(created.id).isEmpty).isTrue()
    }

    @Test
    fun `POST ingredients adds ingredient to recipe`() {
        val recipe = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta")),
            RecipeSummaryDto::class.java
        ).body!!
        val product = productRepository.save(Product(name = "Spaghetti"))

        val response = restTemplate.postForEntity(
            "/api/recipes/${recipe.id}/ingredients",
            authEntity(token, AddIngredientRequest(product.id, 200.0, "g")),
            IngredientDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.productName).isEqualTo("Spaghetti")
    }

    @Test
    fun `POST add-to-list adds ingredient to selected shopping list`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "My List", owner = user))
        val product = productRepository.save(Product(name = "Tomato"))
        val recipe = recipeRepository.save(Recipe(name = "Sauce"))
        val ingredient = recipeIngredientRepository.save(
            RecipeIngredient(recipe = recipe, product = product, quantity = 3.0, unit = "pcs")
        )

        val response = restTemplate.postForEntity(
            "/api/recipes/${recipe.id}/ingredients/${ingredient.id}/add-to-list?listId=${list.id}",
            authEntity(token),
            Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val items = shoppingListItemRepository.findByShoppingList(list)
        assertThat(items).hasSize(1)
        assertThat(items.first().name).isEqualTo("Tomato")
    }
}
```

- [ ] **Step 2: Run to confirm they fail**

```bash
mvn test -pl . -Dtest=RecipeControllerTest -q 2>&1 | tail -10
```

- [ ] **Step 3: Rewrite RecipeController**

Replace the full content of `src/main/kotlin/com/example/dinnerservice/RecipeController.kt`:

```kotlin
package com.example.dinnerservice

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/api")
class RecipeController(
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val currentUserService: CurrentUserService,
    @Value("\${app.upload-dir}") private val uploadDir: String
) {
    companion object {
        val UNITS = listOf("pcs", "g", "kg", "ml", "dl", "L", "tsp", "tbsp", "cup")
        val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
    }

    private fun Recipe.toSummary(): RecipeSummaryDto {
        val preview = recipeImageRepository.findByRecipe(this).firstOrNull()?.filename
        return RecipeSummaryDto(id, name, description, preview)
    }

    private fun RecipeIngredient.toDto() =
        IngredientDto(id, product?.id, product?.name, quantity, unit)

    private fun RecipeImage.toDto() = RecipeImageDto(id, filename, originalName)

    @GetMapping("/recipes")
    fun list(): ResponseEntity<List<RecipeSummaryDto>> =
        ResponseEntity.ok(
            recipeRepository.findAll()
                .sortedBy { it.name.lowercase() }
                .map { it.toSummary() }
        )

    @PostMapping("/recipes")
    fun create(@Valid @RequestBody req: CreateRecipeRequest): ResponseEntity<RecipeSummaryDto> {
        val recipe = recipeRepository.save(Recipe(name = req.name.trim(), description = req.description.trim()))
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe.toSummary())
    }

    @GetMapping("/recipes/{id}")
    fun view(@PathVariable id: Long, @RequestParam(required = false) selectedListId: Long?): ResponseEntity<RecipeDetailDto> {
        val user = currentUserService.currentUser()
        val recipe = recipeRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        val shoppingLists = (owned + shared)
            .sortedBy { it.name.lowercase() }
            .map { ShoppingListSummaryDto(it.id, it.name, it.owner?.email) }

        val ingredients = recipeIngredientRepository.findByRecipe(recipe)

        val ingredientCounts: Map<Long, Int> = if (selectedListId != null) {
            val list = shoppingListRepository.findById(selectedListId).orElse(null)
            if (list != null) {
                val listItems = shoppingListItemRepository.findByShoppingList(list)
                ingredients.associate { ing ->
                    val count = listItems
                        .find { it.name.equals(ing.product?.name, ignoreCase = true) }
                        ?.count?.toInt() ?: 0
                    ing.id to count
                }
            } else emptyMap()
        } else emptyMap()

        return ResponseEntity.ok(
            RecipeDetailDto(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                ingredients = ingredients.map { it.toDto() },
                images = recipeImageRepository.findByRecipe(recipe).map { it.toDto() },
                shoppingLists = shoppingLists,
                selectedListId = selectedListId,
                ingredientCounts = ingredientCounts
            )
        )
    }

    @PutMapping("/recipes/{id}")
    fun edit(@PathVariable id: Long, @Valid @RequestBody req: CreateRecipeRequest): ResponseEntity<RecipeSummaryDto> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val updated = recipeRepository.save(Recipe(id = recipe.id, name = req.name.trim(), description = req.description.trim()))
        return ResponseEntity.ok(updated.toSummary())
    }

    @DeleteMapping("/recipes/{id}")
    @Transactional
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        recipeIngredientRepository.deleteByRecipe(recipe)
        recipeImageRepository.findByRecipe(recipe).forEach { deleteImageFile(it.filename) }
        recipeImageRepository.deleteByRecipe(recipe)
        recipeRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    // ── Images ────────────────────────────────────────────────────────────────

    @GetMapping("/api/recipe-images/{filename}")
    fun serveImage(@PathVariable filename: String): ResponseEntity<Resource> {
        val file = Paths.get(uploadDir, "recipe-images", filename)
        if (!Files.exists(file)) return ResponseEntity.notFound().build()
        val contentType = Files.probeContentType(file) ?: "application/octet-stream"
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(FileSystemResource(file))
    }

    @PostMapping("/recipes/{id}/images")
    fun uploadImages(
        @PathVariable id: Long,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<List<RecipeImageDto>> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val dir = Paths.get(uploadDir, "recipe-images")
        Files.createDirectories(dir)
        val saved = files.filter { !it.isEmpty && it.contentType in ALLOWED_TYPES }.map { file ->
            val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
            val filename = "${UUID.randomUUID()}.$ext"
            Files.copy(file.inputStream, dir.resolve(filename))
            recipeImageRepository.save(RecipeImage(recipe = recipe, filename = filename, originalName = file.originalFilename ?: filename))
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.map { it.toDto() })
    }

    @DeleteMapping("/recipes/{id}/images/{imageId}")
    fun deleteImage(@PathVariable id: Long, @PathVariable imageId: Long): ResponseEntity<Void> {
        val image = recipeImageRepository.findById(imageId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        deleteImageFile(image.filename)
        recipeImageRepository.deleteById(imageId)
        return ResponseEntity.noContent().build()
    }

    private fun deleteImageFile(filename: String) {
        Files.deleteIfExists(Paths.get(uploadDir, "recipe-images", filename))
    }

    // ── Ingredients ───────────────────────────────────────────────────────────

    @PostMapping("/recipes/{id}/ingredients")
    fun addIngredient(
        @PathVariable id: Long,
        @Valid @RequestBody req: AddIngredientRequest
    ): ResponseEntity<IngredientDto> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val product = productRepository.findById(req.productId).orElse(null)
            ?: return ResponseEntity.badRequest().build()
        val ingredient = recipeIngredientRepository.save(
            RecipeIngredient(recipe = recipe, product = product, quantity = req.quantity, unit = req.unit)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient.toDto())
    }

    @DeleteMapping("/recipes/{id}/ingredients/{ingId}")
    fun deleteIngredient(@PathVariable id: Long, @PathVariable ingId: Long): ResponseEntity<Void> {
        if (!recipeIngredientRepository.existsById(ingId)) return ResponseEntity.notFound().build()
        recipeIngredientRepository.deleteById(ingId)
        return ResponseEntity.noContent().build()
    }

    // ── Ingredient ↔ Shopping List ────────────────────────────────────────────

    @PostMapping("/recipes/{id}/ingredients/{ingId}/add-to-list")
    fun addIngredientToList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        @RequestParam listId: Long
    ): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val productName = ingredient.product?.name ?: return ResponseEntity.badRequest().build()

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }

        if (existing != null) {
            shoppingListItemRepository.save(
                ShoppingListItem(id = existing.id, name = existing.name,
                    count = (existing.count ?: 0.0) + 1.0,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy, shoppingList = existing.shoppingList)
            )
        } else {
            val product = productRepository.findByNameIgnoreCase(productName).orElseGet {
                productRepository.save(Product(name = productName, price = ingredient.product?.price))
            }
            shoppingListItemRepository.save(
                ShoppingListItem(name = productName, count = 1.0,
                    unitPrice = ingredient.product?.price,
                    category = product.category, addedBy = user, shoppingList = list)
            )
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/recipes/{id}/ingredients/{ingId}/remove-from-list")
    fun removeIngredientFromList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        @RequestParam listId: Long
    ): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val productName = ingredient.product?.name ?: return ResponseEntity.badRequest().build()

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }
            ?: return ResponseEntity.noContent().build()

        val newCount = (existing.count ?: 1.0) - 1.0
        if (newCount <= 0) {
            shoppingListItemRepository.delete(existing)
        } else {
            shoppingListItemRepository.save(
                ShoppingListItem(id = existing.id, name = existing.name, count = newCount,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy, shoppingList = existing.shoppingList)
            )
        }
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=RecipeControllerTest -q 2>&1 | tail -10
```
Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/RecipeController.kt \
        src/test/kotlin/com/example/dinnerservice/RecipeControllerTest.kt
git commit -m "feat: rewrite RecipeController as REST API"
```

---

### Task 11: ShoppingListController — rewrite and test (TDD)

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ShoppingListControllerTest : IntegrationTestBase() {

    @Autowired lateinit var shoppingListRepository: ShoppingListRepository
    @Autowired lateinit var shoppingListItemRepository: ShoppingListItemRepository
    @Autowired lateinit var productRepository: ProductRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListItemRepository.deleteAll()
        shoppingListRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET shopping-lists returns owned and shared lists`() {
        val response = restTemplate.exchange(
            "/api/shopping-lists", HttpMethod.GET, authEntity(token),
            ShoppingListsResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.owned).isEmpty()
        assertThat(response.body?.shared).isEmpty()
    }

    @Test
    fun `POST shopping-lists creates a list`() {
        val response = restTemplate.postForEntity(
            "/api/shopping-lists",
            authEntity(token, CreateShoppingListRequest("Groceries")),
            ShoppingListSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Groceries")
    }

    @Test
    fun `GET shopping-lists by id returns items sorted by category`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}", HttpMethod.GET, authEntity(token),
            ShoppingListDetailDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.items).hasSize(1)
        assertThat(response.body?.items?.first()?.name).isEqualTo("Milk")
        assertThat(response.body?.isOwner).isTrue()
    }

    @Test
    fun `GET shopping-lists returns 403 for a list you do not own or share`() {
        val other = userRepository.save(User(email = "other@test.com"))
        val list = shoppingListRepository.save(ShoppingList(name = "Private", owner = other))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}", HttpMethod.GET, authEntity(token),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `POST items adds an item to the list`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/items",
            authEntity(token, AddItemRequest("Bread", 1.0, 2.5)),
            ShoppingListItemDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Bread")
    }

    @Test
    fun `POST toggle flips checked state`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/items/${item.id}/toggle",
            authEntity(token), ShoppingListItemDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.checked).isTrue()
    }

    @Test
    fun `DELETE item removes it`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}/items/${item.id}", HttpMethod.DELETE, authEntity(token), Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(shoppingListItemRepository.findById(item.id).isEmpty).isTrue()
    }

    @Test
    fun `POST clear-checked removes only checked items`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Milk", checked = true, shoppingList = list, addedBy = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Bread", checked = false, shoppingList = list, addedBy = user))

        restTemplate.postForEntity("/api/shopping-lists/${list.id}/items/clear-checked", authEntity(token), Void::class.java)

        val remaining = shoppingListItemRepository.findByShoppingList(list)
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().name).isEqualTo("Bread")
    }

    @Test
    fun `POST share shares list with another user`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        userRepository.save(User(email = "friend@test.com"))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/share",
            authEntity(token, ShareRequest("friend@test.com")),
            Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updated = shoppingListRepository.findById(list.id).get()
        assertThat(updated.sharedWith.any { it.email == "friend@test.com" }).isTrue()
    }
}
```

- [ ] **Step 2: Run to confirm they fail**

```bash
mvn test -pl . -Dtest=ShoppingListControllerTest -q 2>&1 | tail -10
```

- [ ] **Step 3: Verify `ShoppingListItem.checked` is `var`**

Open `src/main/kotlin/com/example/dinnerservice/ShoppingListItem.kt`. The `checked` field must be declared as `var checked: Boolean = false` (not `val`). If it is currently `val`, change it to `var` — the toggle endpoint mutates this field directly.

- [ ] **Step 4: Rewrite ShoppingListController**

Replace the full content of `src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt`:

```kotlin
package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/shopping-lists")
class ShoppingListController(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val currentUserService: CurrentUserService
) {
    private fun ShoppingList.toSummary() = ShoppingListSummaryDto(id, name, owner?.email)

    private fun ShoppingListItem.toDto() = ShoppingListItemDto(
        id = id, name = name, count = count, unitPrice = unitPrice, totalPrice = totalPrice,
        checked = checked, categoryId = category?.id, categoryName = category?.name,
        categoryColor = category?.color, addedByEmail = addedBy?.email
    )

    private fun accessibleList(listId: Long, user: User): ShoppingList {
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "List not found")
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }
        return list
    }

    @GetMapping
    fun list(): ResponseEntity<ShoppingListsResponse> {
        val user = currentUserService.currentUser()
        return ResponseEntity.ok(
            ShoppingListsResponse(
                owned = shoppingListRepository.findByOwner(user).map { it.toSummary() },
                shared = shoppingListRepository.findBySharedWithContaining(user).map { it.toSummary() }
            )
        )
    }

    @PostMapping
    fun create(@Valid @RequestBody req: CreateShoppingListRequest): ResponseEntity<ShoppingListSummaryDto> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.save(ShoppingList(name = req.name.trim(), owner = user))
        return ResponseEntity.status(HttpStatus.CREATED).body(list.toSummary())
    }

    @GetMapping("/{id}")
    fun view(@PathVariable id: Long): ResponseEntity<ShoppingListDetailDto> {
        val user = currentUserService.currentUser()
        val list = accessibleList(id, user)
        val items = shoppingListItemRepository.findByShoppingList(list)
            .sortedWith(compareBy({ it.category?.name ?: "zzz" }, { it.name.lowercase() }))
        val totalPrice = items.mapNotNull { it.totalPrice }.sum()
        return ResponseEntity.ok(
            ShoppingListDetailDto(
                id = list.id, name = list.name,
                items = items.map { it.toDto() },
                totalPrice = totalPrice,
                isOwner = list.owner?.id == user.id
            )
        )
    }

    @PostMapping("/{id}/items")
    fun addItem(
        @PathVariable id: Long,
        @Valid @RequestBody req: AddItemRequest
    ): ResponseEntity<ShoppingListItemDto> {
        val user = currentUserService.currentUser()
        val list = accessibleList(id, user)
        val trimmed = req.name.trim()
        val product = productRepository.findByNameIgnoreCase(trimmed).orElseGet {
            productRepository.save(Product(name = trimmed, price = req.unitPrice))
        }
        val item = shoppingListItemRepository.save(
            ShoppingListItem(name = trimmed, count = req.count, unitPrice = req.unitPrice,
                category = product.category, addedBy = user, shoppingList = list)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(item.toDto())
    }

    @PostMapping("/{id}/items/{itemId}/toggle")
    fun toggleItem(@PathVariable id: Long, @PathVariable itemId: Long): ResponseEntity<ShoppingListItemDto> {
        currentUserService.currentUser()
        val item = shoppingListItemRepository.findById(itemId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        item.checked = !item.checked
        val saved = shoppingListItemRepository.save(item)
        return ResponseEntity.ok(saved.toDto())
    }

    @DeleteMapping("/{id}/items/{itemId}")
    fun deleteItem(@PathVariable id: Long, @PathVariable itemId: Long): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val item = shoppingListItemRepository.findById(itemId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (item.addedBy?.id == user.id || item.shoppingList?.owner?.id == user.id) {
            shoppingListItemRepository.delete(item)
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/items/clear-checked")
    fun clearChecked(@PathVariable id: Long): ResponseEntity<Void> {
        currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val checked = shoppingListItemRepository.findByShoppingList(list).filter { it.checked }
        shoppingListItemRepository.deleteAll(checked)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/share")
    fun share(@PathVariable id: Long, @Valid @RequestBody req: ShareRequest): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        val target = userRepository.findByEmail(req.email.trim().lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        list.sharedWith.add(target)
        shoppingListRepository.save(list)
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 5: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=ShoppingListControllerTest -q 2>&1 | tail -10
```
Expected: `Tests run: 9, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/ShoppingListItem.kt \
        src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt \
        src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt
git commit -m "feat: rewrite ShoppingListController as REST API"
```

---

### Task 12: UserController — rewrite and test (TDD)

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/UserController.kt`
- Create: `src/test/kotlin/com/example/dinnerservice/UserControllerTest.kt`

- [ ] **Step 1: Write the failing tests**

```kotlin
package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserControllerTest : IntegrationTestBase() {

    @Autowired lateinit var shoppingListRepository: ShoppingListRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET user returns profile with email and empty lists`() {
        val response = restTemplate.exchange(
            "/api/user", HttpMethod.GET, authEntity(token), UserProfileDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.email).isEqualTo("user@test.com")
        assertThat(response.body?.allLists).isEmpty()
        assertThat(response.body?.defaultListId).isNull()
    }

    @Test
    fun `POST set-default-list sets default list id`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "My List", owner = user))

        val response = restTemplate.postForEntity(
            "/api/user/set-default-list",
            authEntity(token, SetDefaultListRequest(list.id)),
            UserProfileDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.defaultListId).isEqualTo(list.id)
    }

    @Test
    fun `POST leave-list removes user from shared list`() {
        val owner = userRepository.save(User(email = "owner@test.com"))
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "Shared", owner = owner))
        list.sharedWith.add(user)
        shoppingListRepository.save(list)

        val response = restTemplate.postForEntity(
            "/api/user/leave-list/${list.id}",
            authEntity(token), Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updated = shoppingListRepository.findById(list.id).get()
        assertThat(updated.sharedWith.any { it.email == "user@test.com" }).isFalse()
    }
}
```

- [ ] **Step 2: Run to confirm they fail**

```bash
mvn test -pl . -Dtest=UserControllerTest -q 2>&1 | tail -10
```

- [ ] **Step 3: Rewrite UserController**

Replace the full content of `src/main/kotlin/com/example/dinnerservice/UserController.kt`:

```kotlin
package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val currentUserService: CurrentUserService
) {
    private fun ShoppingList.toSummary() = ShoppingListSummaryDto(id, name, owner?.email)

    @GetMapping
    fun profile(): ResponseEntity<UserProfileDto> {
        val user = currentUserService.currentUser()
        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        return ResponseEntity.ok(
            UserProfileDto(
                email = user.email,
                defaultListId = user.defaultListId,
                allLists = (owned + shared).sortedBy { it.name.lowercase() }.map { it.toSummary() },
                sharedLists = shared.map { it.toSummary() }
            )
        )
    }

    @PostMapping("/set-default-list")
    fun setDefaultList(@Valid @RequestBody req: SetDefaultListRequest): ResponseEntity<UserProfileDto> {
        val user = currentUserService.currentUser()
        user.defaultListId = req.listId
        userRepository.save(user)
        return profile()
    }

    @PostMapping("/leave-list/{id}")
    fun leaveList(@PathVariable id: Long): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        list.sharedWith.remove(user)
        if (user.defaultListId == id) {
            user.defaultListId = null
            userRepository.save(user)
        }
        shoppingListRepository.save(list)
        return ResponseEntity.noContent().build()
    }
}
```

- [ ] **Step 4: Run tests to confirm they pass**

```bash
mvn test -pl . -Dtest=UserControllerTest -q 2>&1 | tail -10
```
Expected: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/com/example/dinnerservice/UserController.kt \
        src/test/kotlin/com/example/dinnerservice/UserControllerTest.kt
git commit -m "feat: rewrite UserController as REST API"
```

---

### Task 13: Remove legacy code and Thymeleaf

**Files:**
- Delete: `src/main/kotlin/com/example/dinnerservice/HomeController.kt`
- Delete: `src/main/kotlin/com/example/dinnerservice/Guest.kt`
- Delete: `src/main/kotlin/com/example/dinnerservice/GuestRepository.kt`
- Delete: all files under `src/main/resources/templates/`
- Create: `src/main/resources/db/migration/V2__drop_guests_table.sql`

- [ ] **Step 1: Delete legacy Kotlin files**

```bash
rm src/main/kotlin/com/example/dinnerservice/HomeController.kt
rm src/main/kotlin/com/example/dinnerservice/Guest.kt
rm src/main/kotlin/com/example/dinnerservice/GuestRepository.kt
```

- [ ] **Step 2: Delete all Thymeleaf HTML templates**

```bash
rm -rf src/main/resources/templates/
```

- [ ] **Step 3: Create V2 Flyway migration to drop guests table**

Create `src/main/resources/db/migration/V2__drop_guests_table.sql`:

```sql
DROP TABLE IF EXISTS guests;
```

- [ ] **Step 4: Run all tests to confirm nothing is broken**

```bash
mvn test -q 2>&1 | tail -15
```
Expected: all tests pass, no compilation errors

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: remove legacy HomeController, Guest entity, and Thymeleaf templates"
```

---

### Task 14: Full test run and final verification

- [ ] **Step 1: Run all tests**

```bash
mvn test 2>&1 | tail -20
```
Expected: all test suites pass

- [ ] **Step 2: Build the fat JAR**

```bash
mvn package -DskipTests -q
ls -lh target/dinner-service-*.jar
```
Expected: JAR exists

- [ ] **Step 3: Smoke-test the fat JAR locally**

```bash
java -jar target/dinner-service-*.jar &
sleep 10
curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"wrong"}'
# Expected: 401
pkill -f "dinner-service.*\.jar"
```

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat: Phase 1 complete — pure REST API with JWT auth"
```
