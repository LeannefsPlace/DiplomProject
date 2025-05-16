package com.ivan.db_handler_service.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.util.*

sealed class DbBackupEvent {
    abstract val eventId: String
    abstract val timestamp: Instant
}

data class BackupCommandEvent(
    @JsonProperty("eventId") override val eventId: String,
    @JsonProperty("timestamp") override val timestamp: Instant,
    @JsonProperty("commandType") val commandType: BackupCommandType,
    @JsonProperty("backupFileName") val backupFileName: String? = null,
    @JsonProperty("dbType") val dbType: String? = "postgresql"
) : DbBackupEvent()

data class BackupResultEvent(
    override val eventId: String,
    override val timestamp: Instant = Instant.now(),
    val status: BackupStatus,
    val commandType: BackupCommandType,
    val startTime: Instant,
    val endTime: Instant,
    val durationMs: Long,
    val backupFiles: List<String>? = null,
    val backupPath: String? = null,
    val errorMessage: String? = null
) : DbBackupEvent()

enum class BackupCommandType {
    CREATE, RESTORE, DELETE, LIST
}

enum class BackupStatus {
    SUCCESS, FAILED, IN_PROGRESS
}