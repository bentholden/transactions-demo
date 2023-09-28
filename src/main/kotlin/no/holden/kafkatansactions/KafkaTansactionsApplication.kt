package no.holden.kafkatansactions

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaTansactionsApplication

fun main(args: Array<String>) {
    runApplication<KafkaTansactionsApplication>(*args)
}
