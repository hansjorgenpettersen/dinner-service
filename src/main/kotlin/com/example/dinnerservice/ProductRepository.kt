package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByNameContainingIgnoreCase(name: String): List<Product>
    fun findByNameIgnoreCase(name: String): Optional<Product>
}
