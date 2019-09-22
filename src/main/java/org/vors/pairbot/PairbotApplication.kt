package org.vors.pairbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
object PairbotApplication {

    @JvmStatic
    fun main(args: Array<String>) {
        ApiContextInitializer.init()

        SpringApplication.run(PairbotApplication::class.java, *args)
    }

}
