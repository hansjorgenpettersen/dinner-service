package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val email: String = "",
    @Column(nullable = true)
    var passwordHash: String? = null
)
