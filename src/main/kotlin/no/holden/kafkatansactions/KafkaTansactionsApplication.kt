package no.holden.kafkatansactions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class KafkaTansactionsApplication

fun main(args: Array<String>) {
    runApplication<KafkaTansactionsApplication>(*args)
}
