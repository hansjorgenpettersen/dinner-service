package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    /**
     * Forwards all non-API, non-static requests to index.html so React Router
     * can handle client-side routing. Excludes /api/**, /actuator/**, and /recipe-images/**.
     */
    @GetMapping("/{path:^(?!api|actuator|recipe-images).*}/**")
    fun spa(): String = "forward:/index.html"
}
