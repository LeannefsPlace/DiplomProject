package com.ivan.auth_service.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

sealed class UserEvent {
    abstract val eventId: String
}

sealed class SessionEvent {
    abstract val eventId: String
}

data class UserCommandEvent(
    override var eventId: String = UUID.randomUUID().toString(),
    val commandType: UserCommandType,
    val userId: Int? = null,
    val login: String? = null,
    val email: String? = null,
    val passwordHash: String? = null,
    val avatarUrl: String? = null,
    val fullName: String? = null,
    val globalRole: String? = null,
    val skillIds: List<Int> = emptyList(),
) : UserEvent()

data class UserResultEvent(
    @JsonProperty("eventId") override val eventId: String,
    @JsonProperty("users") val users: List<UserDto>? = null,
    @JsonProperty("passwordHash") val passwordHash: String? = null,
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("errorMessage") val errorMessage: String? = null
) : UserEvent()

data class UserActionEvent(
    @JsonProperty("eventId") override val eventId: String,
    @JsonProperty("actionType") val actionType: String,
    @JsonProperty("userId") val userId: Int?,
    @JsonProperty("login") val login: String?
) : UserEvent()

data class SessionCommandEvent(
    @JsonProperty("eventId") override val eventId: String,
    @JsonProperty("sessionCommandType") val sessionCommandType: SessionCommandType,
    @JsonProperty("sessionId") val sessionId: String?,
    @JsonProperty("userId") val userId: Int?
) : SessionEvent()

data class SessionResultEvent(
    override val eventId: String,
    val userId:Int? = null,
    val success: Boolean,
    val errorMessage: String? = null
) : SessionEvent()

data class SessionActionEvent(
    override val eventId: String,
    val userId: Int? = null,
    val sessionCommandType: SessionCommandType? = null
) : SessionEvent()

data class UserDto(
    @JsonProperty("id") val id: Int,
    @JsonProperty("login") val login: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("fullName") val fullName: String?,
    @JsonProperty("globalRole") val globalRole: String,
    @JsonProperty("skillIds") val skillIds: List<Int>,
    @JsonProperty("createdAt") val createdAt: String
)

enum class SessionCommandType {
    EXPIRE, EXPIRE_ALL, EXPIRE_ALL_EXCEPT_CURRENT, VERIFY
}

enum class UserCommandType {
    CREATE, DELETE, EDIT, GET
}