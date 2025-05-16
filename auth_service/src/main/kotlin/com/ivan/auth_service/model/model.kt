package com.ivan.auth_service.model

import jakarta.persistence.*
import org.w3c.dom.Text
import java.time.Instant

@Entity
@Table(name = "sessions")
data class Session(
    @Id
    var id: String? = null,

    @Column(name = "user_id", nullable = false)
    var userId: Int? = null,

    @Column(name = "expiration_date", nullable = false)
    var expirationDate: Instant? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),
)