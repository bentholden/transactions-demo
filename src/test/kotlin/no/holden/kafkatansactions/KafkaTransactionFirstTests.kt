package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.time.Duration
import java.util.UUID
import kotlin.test.assertNotNull


@SpringBootTest
@AutoConfigureTestDatabase
@EmbeddedKafka(value = 3, partitions = 1, topics = ["internal.test.topic", "external.test.topic"], controlledShutdown = true)
class KafkaTransactionFirstTests {

//    @MockBean
    @Autowired
    private lateinit var kafkaRecordRepository: KafkaRecordRepository

    @Autowired
    private lateinit var kafkaProducerService: KafkaProducerService

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Test
    fun `Kafka message is sent when JPA save fails`() {
//        Mockito.`when`(kafkaRecordRepository.save(Mockito.any()))
//            .thenThrow(RuntimeException("Oh no! a runtime exception!!"))

        val id = UUID.randomUUID()
        val message = "hello"

        kafkaProducerService.sendMessage(id, message)
        Thread.sleep(1_000)

        val dbRecord = kafkaRecordRepository.findByIdOrNull(id)
        assertNotNull(dbRecord)

        // Setup kafka consumer
        val consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
        val kafkaConsumer = KafkaConsumer(consumerProps, UUIDDeserializer(), StringDeserializer())
        embeddedKafkaBroker.consumeFromEmbeddedTopics(kafkaConsumer, "external.test.topic")
        // --------------------
        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "external.test.topic", Duration.ofSeconds(2))

        assertNotNull(record)
        assertEquals(id, record.key())
        assertEquals(message, record.value())
    }
}
