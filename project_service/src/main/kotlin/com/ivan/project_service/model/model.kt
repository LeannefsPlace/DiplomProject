package com.ivan.project_service.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "projects")
data class Project(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 100)
    val name: String?,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "avatar_url", length = 255)
    var avatarUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: List<ProjectUser> = mutableListOf()
) {
    constructor() : this(
            0,
            null,
        Instant.now(),
        false,
        null,
        null
        )
}

@Entity
@Table(name = "project_users")
data class ProjectUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project? = null,

    @Column(name = "user_id", nullable = false)
    val userId: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "project_role", nullable = false, length = 20)
    var role: ProjectRole? = null,
)
enum class ProjectRole {
    OWNER, MEMBER, MANAGER
}