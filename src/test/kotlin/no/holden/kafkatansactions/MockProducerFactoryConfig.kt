package no.holden.kafkatansactions

import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.mock.MockProducerFactory
import java.util.UUID

@TestConfiguration
class MockProducerFactoryConfig {
    @Bean
    fun producer() = MockProducer(true, UUIDSerializer(), StringSerializer())
        .apply {
            initTransactions()
        }

    @Bean
    fun producerFactory(producer: MockProducer<UUID, String>) =
        MockProducerFactory({ tx, id -> producer }, "defaultTxId")

}