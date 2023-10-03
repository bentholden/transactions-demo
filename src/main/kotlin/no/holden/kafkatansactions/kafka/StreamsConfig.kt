package no.holden.kafkatansactions.kafka

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serdes.UUIDSerde
import org.apache.kafka.streams.StreamsConfig.COMMIT_INTERVAL_MS_CONFIG
import org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG
import org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG
import org.apache.kafka.streams.StreamsConfig.EXACTLY_ONCE_V2
import org.apache.kafka.streams.StreamsConfig.PROCESSING_GUARANTEE_CONFIG
import org.apache.kafka.streams.StreamsConfig.REPLICATION_FACTOR_CONFIG
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration
import org.springframework.kafka.config.KafkaStreamsConfiguration

@Configuration
@EnableKafkaStreams
class StreamsConfig {
    @Bean(name = [KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME])
    fun kStreamsConfig(
        kafkaProperties: KafkaProperties,
    ): KafkaStreamsConfiguration {
        kafkaProperties.streams.applicationId = "test.streams.appid"
        kafkaProperties.streams.properties[DEFAULT_KEY_SERDE_CLASS_CONFIG] = UUIDSerde()::class.java.name
        kafkaProperties.streams.properties[DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.StringSerde()::class.java.name
        kafkaProperties.streams.properties[PROCESSING_GUARANTEE_CONFIG] = EXACTLY_ONCE_V2
        kafkaProperties.streams.properties[COMMIT_INTERVAL_MS_CONFIG] = "0"


        return KafkaStreamsConfiguration(kafkaProperties.buildStreamsProperties())
    }
}