package no.holden.kafkatansactions.kafka

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class KafkaProducerService(
    private val kafkaTemplate: KafkaTemplate<UUID, String>,
) {
    fun sendMessage(id: UUID, message: String) {
        kafkaTemplate.sendDefault(id, message)
    }
}