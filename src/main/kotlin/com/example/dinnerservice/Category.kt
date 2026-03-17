package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    val color: String = "#cccccc"
)
