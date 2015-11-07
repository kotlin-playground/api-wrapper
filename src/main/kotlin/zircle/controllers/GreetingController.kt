package zircle.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import zircle.models.Greeting
import java.util.concurrent.atomic.AtomicLong

@RestController
class GreetingController {
    val counter = AtomicLong()

    @RequestMapping("/greeting")
    fun greeting(@RequestParam(value = "name", defaultValue = "World") name: String): Greeting {
        return Greeting(counter.incrementAndGet(), "Hello, $name!")
    }
}