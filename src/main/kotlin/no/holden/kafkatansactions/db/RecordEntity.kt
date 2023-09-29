package no.holden.kafkatansactions.db

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "T_RECORD")
class RecordEntity(
    @Id
    val id: UUID,
    val record: String,
)
