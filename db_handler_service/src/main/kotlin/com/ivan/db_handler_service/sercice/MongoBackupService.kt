package com.ivan.db_handler_service.sercice

import com.ivan.db_handler_service.kafka.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.io.File
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

@Service
class MongoBackupService(
    private val kafkaTemplate: KafkaTemplate<String, DbBackupEvent>,
    private val coroutineScope: CoroutineScope,
    @Value("\${backup.storage.path}") private val backupStoragePath: String,
    @Value("\${MONGO_HOST:mongodb}") private val mongoHost: String,
    @Value("\${MONGO_PORT:27017}") private val mongoPort: Int,
    @Value("\${MONGO_USER:root}") private val mongoUsername: String,
    @Value("\${MONGO_PASSWORD:password}") private val mongoPassword: String,
    @Value("\${MONGO_AUTH_DB:admin}") private val mongoAuthDb: String
) {
    companion object {
        const val BACKUP_COMMANDS_TOPIC = "backup-commands"
        const val BACKUP_RESULTS_TOPIC = "backup-results"
    }

    private val mongoUri: String
        get() = "mongodb://$mongoUsername:$mongoPassword@$mongoHost:$mongoPort"

    @KafkaListener(topics = [BACKUP_COMMANDS_TOPIC])
    fun handleCommand(event: BackupCommandEvent) {
        if (event.dbType != "mongodb") return
        
        coroutineScope.launch {
            val startTime = Instant.now()

            when (event.commandType) {
                BackupCommandType.CREATE -> handleCreateBackup(event, startTime)
                BackupCommandType.RESTORE -> handleRestore(event, startTime)
                BackupCommandType.DELETE -> handleDeleteBackup(event, startTime)
                BackupCommandType.LIST -> handleListBackups(event, startTime)
            }
        }
    }

    private suspend fun handleCreateBackup(event: BackupCommandEvent, startTime: Instant) {
        var status = BackupStatus.FAILED
        var backupPath: String? = null
        var errorMessage: String? = null

        try {
            println("Starting MongoDB backup process for database: task_manager_db")
            println("Using MongoDB URI: $mongoUri")
            sendProgress(event, startTime, BackupStatus.IN_PROGRESS)

            // Проверяем существование директории для бэкапов
            val backupDir = File(backupStoragePath)
            if (!backupDir.exists()) {
                println("Creating backup directory: $backupStoragePath")
                backupDir.mkdirs()
            }

            backupPath = "$backupStoragePath/mongo_task_manager_db_${startTime.epochSecond}.gz"
            println("Backup will be saved to: $backupPath")

            val process = ProcessBuilder(
                "mongodump",
                "--uri=$mongoUri",
                "--authenticationDatabase=$mongoAuthDb",
                "--db=task_manager_db",
                "--archive=$backupPath",
                "--gzip"
            )
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

            println("mongodump process started with PID: ${process.pid()}")

            val completed = process.waitFor(10, TimeUnit.MINUTES)
            val endTime = Instant.now()

            if (!completed) {
                println("Backup process timed out after 10 minutes")
                process.destroyForcibly()
                errorMessage = "Backup process timed out after 10 minutes"
                status = BackupStatus.FAILED
            } else {
                val exitValue = process.exitValue()
                println("mongodump process finished with exit code: $exitValue")
                
                if (exitValue == 0) {
                    val backupFile = File(backupPath)
                    if (backupFile.exists() && backupFile.length() > 0) {
                        println("Backup file created successfully: ${backupFile.length()} bytes")
                        status = BackupStatus.SUCCESS
                    } else {
                        errorMessage = "Backup file was not created or is empty"
                        status = BackupStatus.FAILED
                    }
                } else {
                    errorMessage = process.errorStream.bufferedReader().readText()
                    println("Backup process failed with error: $errorMessage")
                    status = BackupStatus.FAILED
                }
            }

            sendResult(
                event,
                status,
                startTime,
                endTime,
                backupPath = backupPath,
                errorMessage = errorMessage
            )
        } catch (e: Exception) {
            println("Exception during backup process: ${e.message}")
            e.printStackTrace()
            sendResult(
                event,
                BackupStatus.FAILED,
                startTime,
                Instant.now(),
                errorMessage = e.message
            )
        }
    }

    private suspend fun handleRestore(event: BackupCommandEvent, startTime: Instant) {
        var status = BackupStatus.FAILED
        var errorMessage: String? = null

        try {
            requireNotNull(event.backupFileName) { "Backup file name must be specified for restore" }

            sendProgress(event, startTime, BackupStatus.IN_PROGRESS)

            val backupPath = "$backupStoragePath/${event.backupFileName}"
            val backupFile = File(backupPath)
            
            if (!backupFile.exists()) {
                throw Exception("Backup file not found: $backupPath")
            }
            
            if (!backupFile.canRead()) {
                throw Exception("Cannot read backup file: $backupPath")
            }
            
            println("Starting restore from backup file: $backupPath")
            println("Using MongoDB URI: $mongoUri")

            val process = ProcessBuilder(
                "mongorestore",
                "--uri=$mongoUri",
                "--authenticationDatabase=$mongoAuthDb",
                "--db=task_manager_db",
                "--archive=$backupPath",
                "--gzip",
                "--drop"
            )
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

            val completed = process.waitFor(10, TimeUnit.MINUTES)
            val endTime = Instant.now()

            if (!completed) {
                process.destroyForcibly()
                errorMessage = "Restore process timed out after 10 minutes"
                status = BackupStatus.FAILED
            } else {
                val exitValue = process.exitValue()
                println("Restore process finished with exit code: $exitValue")
                
                if (exitValue == 0) {
                    println("Restore completed successfully")
                    status = BackupStatus.SUCCESS
                } else {
                    errorMessage = process.errorStream.bufferedReader().readText()
                    println("Restore failed with error: $errorMessage")
                    status = BackupStatus.FAILED
                }
            }

            sendResult(
                event,
                status,
                startTime,
                endTime,
                backupPath = backupPath,
                errorMessage = errorMessage
            )
        } catch (e: Exception) {
            println("Exception during restore process: ${e.message}")
            e.printStackTrace()
            sendResult(
                event,
                BackupStatus.FAILED,
                startTime,
                Instant.now(),
                errorMessage = e.message
            )
        }
    }

    private suspend fun handleDeleteBackup(event: BackupCommandEvent, startTime: Instant) {
        var status = BackupStatus.FAILED
        var errorMessage: String? = null

        try {
            requireNotNull(event.backupFileName) { "Backup file name must be specified for delete" }

            val file = File("$backupStoragePath/${event.backupFileName}")
            if (file.exists()) {
                if (file.delete()) {
                    status = BackupStatus.SUCCESS
                } else {
                    errorMessage = "Failed to delete file"
                }
            } else {
                errorMessage = "File not found"
            }

            sendResult(
                event,
                status,
                startTime,
                Instant.now(),
                errorMessage = errorMessage
            )
        } catch (e: Exception) {
            sendResult(
                event,
                BackupStatus.FAILED,
                startTime,
                Instant.now(),
                errorMessage = e.message
            )
        }
    }

    private suspend fun handleListBackups(event: BackupCommandEvent, startTime: Instant) {
        try {
            val backupDir = File(backupStoragePath)
            val backups = backupDir.listFiles { file ->
                file.name.startsWith("mongo_task_manager_db") && file.extension == "gz"
            }?.map { it.name } ?: emptyList()

            sendResult(
                event,
                BackupStatus.SUCCESS,
                startTime,
                Instant.now(),
                backupFiles = backups
            )
        } catch (e: Exception) {
            sendResult(
                event,
                BackupStatus.FAILED,
                startTime,
                Instant.now(),
                errorMessage = e.message
            )
        }
    }

    private fun sendProgress(event: BackupCommandEvent, startTime: Instant, status: BackupStatus) {
        kafkaTemplate.send(
            BACKUP_RESULTS_TOPIC,
            event.eventId,
            BackupResultEvent(
                eventId = event.eventId,
                status = status,
                commandType = event.commandType,
                startTime = startTime,
                endTime = Instant.now(),
                durationMs = Duration.between(startTime, Instant.now()).toMillis()
            )
        )
    }

    private fun sendResult(
        event: BackupCommandEvent,
        status: BackupStatus,
        startTime: Instant,
        endTime: Instant,
        backupPath: String? = null,
        backupFiles: List<String>? = null,
        errorMessage: String? = null
    ) {
        kafkaTemplate.send(
            BACKUP_RESULTS_TOPIC,
            event.eventId,
            BackupResultEvent(
                eventId = event.eventId,
                status = status,
                commandType = event.commandType,
                startTime = startTime,
                endTime = endTime,
                durationMs = Duration.between(startTime, endTime).toMillis(),
                backupFiles = backupFiles,
                backupPath = backupPath,
                errorMessage = errorMessage
            )
        )
    }
}