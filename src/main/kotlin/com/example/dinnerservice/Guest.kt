package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "guests")
class Guest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = ""
)
