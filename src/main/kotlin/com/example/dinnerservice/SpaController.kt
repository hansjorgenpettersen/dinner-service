package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    // Forwards SPA routes to index.html for React Router.
    // Excluded: api, actuator, recipe-images (backend), assets (Vite output),
    // and any first-segment containing a dot (static files like index.html, favicon.ico).
    @GetMapping("/{path:^(?!api|actuator|recipe-images|assets)[^.]*}/**")
    fun spa(): String = "forward:/index.html"
}
