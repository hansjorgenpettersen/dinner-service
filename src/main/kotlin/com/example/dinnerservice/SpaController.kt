package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    // Forwards non-API, non-actuator, non-static requests to index.html for React Router
    @GetMapping("/{path:^(?!api|actuator|recipe-images).*}/**")
    fun spa(): String = "forward:/index.html"
}
