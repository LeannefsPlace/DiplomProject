package com.ivan.user_service.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

sealed class UserEvent {
    abstract val eventId: String
}

sealed class SkillEvent {
    abstract val eventId: String
}

data class UserCommandEvent(
    @JsonProperty("eventId") override val eventId: String = UUID.randomUUID().toString(),
    @JsonProperty("commandType") val commandType: UserCommandType,
    @JsonProperty("userId") val userId: Int? = null,
    @JsonProperty("login") val login: String? = null,
    @JsonProperty("email") val email: String? = null,
    @JsonProperty("passwordHash") val passwordHash: String? = null,
    @JsonProperty("avatarUrl") val avatarUrl: String? = null,
    @JsonProperty("fullName") val fullName: String? = null,
    @JsonProperty("globalRole") val globalRole: String? = null,
    @JsonProperty("skillIds") val skillIds: List<Int>? = null
) : UserEvent()

data class UserResultEvent(
    override val eventId: String,
    val users: List<UserDto>? = null,
    val passwordHash: String? = null,
    val success: Boolean,
    val errorMessage: String? = null
) : UserEvent()

data class UserActionEvent(
    override val eventId: String,
    val actionType: String,
    val userId: Int?,
    val login: String?
) : UserEvent()

data class SkillActionEvent(
    override val eventId: String,
    val actionType: String,
    val skillId: Int
) : SkillEvent()

data class SkillCommandEvent(
    @JsonProperty("eventId") override val eventId: String = UUID.randomUUID().toString(),
    @JsonProperty("commandType") val commandType: SkillCommandType,
    @JsonProperty("skillId") val skillId: Int? = null,
    @JsonProperty("name") val name: String? = null,
    @JsonProperty("type") val type: String? = null
) : SkillEvent()

data class SkillResultEvent(
    override val eventId: String,
    val skills: List<SkillDto>? = null,
    val success: Boolean,
    val errorMessage: String? = null
) : SkillEvent()

data class UserDto(
    val id: Int,
    val login: String,
    val email: String,
    val fullName: String?,
    val globalRole: String,
    val skillIds: List<Int>,
    val createdAt: String,
    val avatarUrl: String?,
)

data class SkillDto(
    val id: Int,
    val name: String,
    val type: String,
    val userCount: Int? = null
)

enum class UserCommandType {
    CREATE, DELETE, LIST, EDIT, FIND_BY_SKILLS, GET
}

enum class SkillCommandType {
    CREATE, DELETE, LIST, FIND_BY_TYPE
}