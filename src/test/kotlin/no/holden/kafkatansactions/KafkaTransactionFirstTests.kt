package no.holden.kafkatansactions

import no.holden.kafkatansactions.db.KafkaRecordRepository
import no.holden.kafkatansactions.kafka.KafkaProducerService
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.util.UUID
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Duration
import kotlin.test.assertNotNull


@SpringBootTest
@AutoConfigureTestDatabase
@EmbeddedKafka(partitions = 1, topics = ["test.topic"], controlledShutdown = true)
class KafkaTransactionFirstTests {

    @MockBean
    private lateinit var kafkaRecordRepository: KafkaRecordRepository

    @Autowired
    private lateinit var myService: MyService

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Test
    fun `Kafka message is sent when JPA save fails`() {
        Mockito.`when`(kafkaRecordRepository.save(Mockito.any()))
            .thenThrow(RuntimeException("Oh no! a runtime exception!!"))

        val id = UUID.randomUUID()
        val message = "hello"

        assertThrows<RuntimeException> {
            myService.sendMessageWithKafkaFirst(id, message)
        }

        val dbRecord = kafkaRecordRepository.findByIdOrNull(id)
        assertNull(dbRecord)

        // Setup kafka consumer
        val consumerProps = KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"
        val kafkaConsumer = KafkaConsumer(consumerProps, UUIDDeserializer(), StringDeserializer())
        embeddedKafkaBroker.consumeFromEmbeddedTopics(kafkaConsumer, "test.topic");
        // --------------------
        val record = KafkaTestUtils.getSingleRecord(kafkaConsumer, "test.topic", Duration.ofSeconds(10))

        assertNotNull(record)
        assertEquals(id, record.key())
        assertEquals(message, record.value())
    }
}
