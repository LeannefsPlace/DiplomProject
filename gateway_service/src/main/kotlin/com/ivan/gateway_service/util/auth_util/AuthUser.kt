package com.ivan.gateway_service.util.auth_util

import com.ivan.gateway_service.kafka.ProjectRole
import java.time.Instant

data class AuthUser(
    var id: Int,
    var login: String,
    var role: String,
    var projectRoles: List<AuthProject>,
    var lastActive: Instant = Instant.now()
)

data class AuthProject(
    val projectId: Int,
    val projectRole: ProjectRole,
)