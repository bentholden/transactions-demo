package no.holden.kafkatansactions.db

import jakarta.persistence.EntityManagerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.TransactionDefinition.ISOLATION_SERIALIZABLE
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.support.TransactionTemplate

@Configuration
class JpaTransactionTemplateConfig(
    entityManagerFactory: EntityManagerFactory,
) {

    private val jpaTransactionManager = JpaTransactionManager(entityManagerFactory)


    @Bean
    @Primary
    fun defaultJpaTransactionTemplate() =
        TransactionTemplate(jpaTransactionManager)

    @Bean
    fun serializableJpaTransactionTemplate() =
        TransactionTemplate(jpaTransactionManager)
            .apply {
//                setIsolationLevelName(Isolation.SERIALIZABLE.name)
                isolationLevel = ISOLATION_SERIALIZABLE
            }
}