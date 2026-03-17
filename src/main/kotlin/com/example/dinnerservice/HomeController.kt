package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import kotlin.random.Random

@Controller
class HomeController {

    @GetMapping("/")
    fun home(model: Model): String {
        model.addAttribute("randomNumber", Random.nextInt(1, 10_000))
        return "index"
    }
}
