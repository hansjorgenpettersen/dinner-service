package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "recipes")
class Recipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    @Column(columnDefinition = "TEXT")
    val description: String = ""
)
