package no.holden.kafkatansactions.kafka

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import java.util.UUID

@Configuration
class KafkaProducerConfig {

    @Bean
    @ConditionalOnMissingBean
    fun producerFactory(
        kafkaProperties: KafkaProperties,
    ): ProducerFactory<UUID, String> =
        DefaultKafkaProducerFactory(
            kafkaProperties.buildProducerProperties(),
            UUIDSerializer(),
            StringSerializer()
        )

    @Bean
    fun defaultKafkaEventTemplate(producerFactory: ProducerFactory<UUID, String>): KafkaTemplate<UUID, String> =
        KafkaTemplate(producerFactory)
            .apply {
                defaultTopic = "internal.test.topic"
            }

}