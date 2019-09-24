package org.vors.pairbot

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.telegram.telegrambots.ApiContextInitializer

fun main(args: Array<String>) {
    ApiContextInitializer.init()

    SpringApplication.run(PairbotApplication::class.java, *args)
}

@SpringBootApplication
open class PairbotApplication {



}
