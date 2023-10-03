package no.holden.kafkatansactions.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.UUIDDeserializer
import org.apache.kafka.common.serialization.UUIDSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.transaction.KafkaTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID


@Configuration
class KafkaConfig {

    @Bean
    fun kafkaTransactionManager(producerFactory: ProducerFactory<UUID, String>) = KafkaTransactionManager(producerFactory)

    @Bean
    fun consumerFactory(
        defaultKafkaProperties: KafkaProperties,
    ): ConsumerFactory<UUID, String> {
        defaultKafkaProperties.consumer.clientId = UUID.randomUUID().toString()
        defaultKafkaProperties.consumer.groupId = "test.consumer.group"
        defaultKafkaProperties.consumer.isolationLevel = KafkaProperties.IsolationLevel.READ_COMMITTED

        return DefaultKafkaConsumerFactory(
            defaultKafkaProperties.buildConsumerProperties(),
            UUIDDeserializer(),
            StringDeserializer()
        )
    }

    @Bean
    fun kafkaListenerContainerFactoryConfigurer(ktm: KafkaTransactionManager<UUID, String>) =
        object : ConcurrentKafkaListenerContainerFactoryConfigurer() {
            override fun configure(
                listenerContainerFactory: ConcurrentKafkaListenerContainerFactory<Any, Any>,
                consumerFactory: ConsumerFactory<Any, Any>,
            ) {
                super.configure(listenerContainerFactory, consumerFactory)
                listenerContainerFactory.containerProperties.transactionManager = ktm
            }
        }


    @Bean
    @ConditionalOnMissingBean
    fun producerFactory(
        kafkaProperties: KafkaProperties,
    ): ProducerFactory<UUID, String> =
        DefaultKafkaProducerFactory(
            kafkaProperties
                .apply {
                       producer.properties[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = "true"
                }.buildProducerProperties(),
            UUIDSerializer(),
            StringSerializer()
        )

    @Bean
    fun defaultKafkaTemplate(producerFactory: ProducerFactory<UUID, String>): KafkaTemplate<UUID, String> =
        KafkaTemplate(producerFactory)


    @Bean
    fun kafkaTransactionTemplate(kafkaTransactionManager: KafkaTransactionManager<UUID, String>) =TransactionTemplate(kafkaTransactionManager)
}