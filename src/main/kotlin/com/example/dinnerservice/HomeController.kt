package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import kotlin.random.Random

@Controller
class HomeController(private val guestRepository: GuestRepository) {

    @PostMapping("/greet")
    @ResponseBody
    fun greet(@RequestParam name: String): String {
        guestRepository.save(Guest(name = name))
        return "Hello, $name!"
    }

    @GetMapping("/guests")
    fun guests(model: Model): String {
        model.addAttribute("guests", guestRepository.findAll())
        return "guests"
    }

    @GetMapping("/random")
    @ResponseBody
    fun random(): Int = Random.nextInt(1, 10_000)
}
