package com.ivan.user_service.service

import com.ivan.user_service.kafka.*
import com.ivan.user_service.model.Skill
import com.ivan.user_service.model.User
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.kafka.common.requests.DeleteAclsResponse.log
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID

@Service
class KafkaMessageService(
    private val kafkaProducerService: KafkaProducerService,
    private val userService: UserService,
    private val skillService: SkillService,
    @Qualifier("jpaDispatcher") private val jpaDispatcher: CoroutineDispatcher,
    private val transactionTemplate: TransactionTemplate,
) {

    @KafkaListener(
        topics = ["\${app.kafka.topics.user-commands}"],
        containerFactory = "userCommandKafkaListenerContainerFactory"
    )
    fun handleUserCommands(command: UserCommandEvent, ack: Acknowledgment) {
        runBlocking {
            try {
                when (command.commandType) {
                    UserCommandType.CREATE -> createUser(command)
                    UserCommandType.EDIT -> updateUser(command)
                    UserCommandType.DELETE -> deleteUser(command)
                    UserCommandType.LIST -> listUsers(command)
                    UserCommandType.FIND_BY_SKILLS -> findUsersBySkills(command)
                    UserCommandType.GET -> getUser(command)
                }
                ack.acknowledge()
            } catch (e: Exception) {
                log.error("Error processing user command: ${e.message}", e)
                ack.acknowledge()
            }
        }
    }

    @KafkaListener(
        topics = ["\${app.kafka.topics.skill-commands}"],
        containerFactory = "skillCommandKafkaListenerContainerFactory"
    )
    fun handleSkillCommands(command: SkillCommandEvent, ack: Acknowledgment) {
        runBlocking {
            try {
                when (command.commandType) {
                    SkillCommandType.CREATE -> createSkill(command)
                    SkillCommandType.DELETE -> deleteSkill(command)
                    SkillCommandType.LIST -> listSkills(command)
                    SkillCommandType.FIND_BY_TYPE -> findSkillsByType(command)
                }
                ack.acknowledge()
            } catch (e: Exception) {
                log.error("Error processing skill command: ${e.message}", e)
                ack.acknowledge()
            }
        }
    }

    suspend fun createUser(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    requireNotNull(command.login) { "Login is required" }
                    requireNotNull(command.email) { "Email is required" }
                    requireNotNull(command.passwordHash) { "Password hash is required" }

                    val user = userService.createUser(
                        login = command.login,
                        email = command.email,
                        passwordHash = command.passwordHash,
                        fullName = command.fullName,
                        globalRole = command.globalRole ?: "USER",
                        skillIds = command.skillIds ?: emptyList()
                    )

                    kafkaProducerService.sendUserResult(
                        UserResultEvent(
                            eventId = command.eventId,
                            users = listOf(user.toDto()),
                            success = true
                        )
                    )

                } catch (e: IllegalArgumentException) {
                    log.error("Validation error creating user: ${e.message}")
                    sendUserErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to create user: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to create user: ${e.message}")
                }
            }
        }
    }

    suspend fun getUser(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val user:User

                    if (command.login != null){
                        val userLogin = command.login
                        user = userService.getUserByLogin(userLogin)!!
                    }else{
                        requireNotNull(command.userId)
                        user = userService.getUserById(command.userId)!!
                    }


                    kafkaProducerService.sendUserResult(
                        UserResultEvent(
                            eventId = command.eventId,
                            users = listOf(user.toDto()),
                            passwordHash = user.passwordHash,
                            success = true
                        )
                    )

                } catch (e: Exception) {
                    log.error("Failed to get user: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to update user: ${e.message}")
                }
            }
        }
    }

    suspend fun updateUser(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val userId = requireNotNull(command.userId) { "User ID is required" }

                    val prevUser = userService.getUserById(userId)!!

                    val updatedUser = userService.updateUser(
                        userId = userId,
                        email = command.email,
                        fullName = command.fullName,
                        avatarUrl = command.avatarUrl,
                        passwordHash = command.passwordHash,
                        globalRole = command.globalRole,
                        skillIds = command.skillIds
                    )

                    kafkaProducerService.sendUserResult(
                        UserResultEvent(
                            eventId = command.eventId,
                            users = listOf(updatedUser.toDto()),
                            success = true
                        )
                    )

                    if (command.globalRole != prevUser.globalRole || !command.passwordHash.isNullOrEmpty()) {
                        val actionType = when {
                            command.globalRole != prevUser.globalRole -> "ROLE_UPDATED"
                            command.passwordHash != prevUser.passwordHash -> "PASSWORD_UPDATED"
                            else -> "USER_UPDATED"
                        }

                        kafkaProducerService.sendUserAction(
                            UserActionEvent(
                                eventId = UUID.randomUUID().toString(),
                                actionType = actionType,
                                userId = updatedUser.id,
                                login = updatedUser.login
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    log.error("Validation error updating user: ${e.message}")
                    sendUserErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to update user: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to update user: ${e.message}")
                }
            }
        }
    }

    suspend fun findUsersBySkills(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val skillIds = requireNotNull(command.skillIds) { "Skill IDs are required" }

                    val users = userService.findUsersBySkills(skillIds)

                    kafkaProducerService.sendUserResult(
                        UserResultEvent(
                            eventId = command.eventId,
                            users = users.map { it.toDto() },
                            success = true
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    log.error("Validation error finding users by skills: ${e.message}")
                    sendUserErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to find users by skills: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to find users by skills: ${e.message}")
                }
            }
        }
    }

    suspend fun listUsers(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val users = userService.getAllUsers()

                    kafkaProducerService.sendUserResult(
                        UserResultEvent(
                            eventId = command.eventId,
                            users = users.map { it.toDto() },
                            success = true
                        )
                    )
                } catch (e: Exception) {
                    log.error("Failed to list users: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to list users: ${e.message}")
                }
            }
        }
    }

    suspend fun deleteUser(command: UserCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val userId = requireNotNull(command.userId) { "User ID is required" }

                    val deletedUser = userService.deleteUser(userId)

                    if (deletedUser) {
                        kafkaProducerService.sendUserResult(
                            UserResultEvent(
                                eventId = command.eventId,
                                success = true
                            )
                        )

                        kafkaProducerService.sendUserAction(
                            UserActionEvent(
                                eventId = UUID.randomUUID().toString(),
                                actionType = "USER_DELETED",
                                userId = command.userId,
                                login = null
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    log.error("Validation error deleting user: ${e.message}")
                    sendUserErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to delete user: ${e.message}", e)
                    sendUserErrorResult(command.eventId, "Failed to delete user: ${e.message}")
                }
            }
        }
    }

    suspend fun findSkillsByType(command: SkillCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val skills = skillService.getAllSkillsByType(requireNotNull(command.type))

                    kafkaProducerService.sendSkillResult(
                        SkillResultEvent(
                            eventId = command.eventId,
                            skills = skills.map { it.toDto() },
                            success = true
                        )
                    )

                } catch (e: Exception) {
                    log.error("Failed to list skills by type: ${e.message}", e)
                    sendSkillErrorResult(command.eventId, "Failed to list skills by type: ${e.message}")
                }
            }
        }
    }

    suspend fun listSkills(command: SkillCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val skills = skillService.getAllSkills()

                    kafkaProducerService.sendSkillResult(
                        SkillResultEvent(
                            eventId = command.eventId,
                            skills = skills.map { it.toDto() },
                            success = true,
                            errorMessage = null
                        )
                    )

                } catch (e: Exception) {
                    log.error("Failed to list skills: ${e.message}", e)
                    sendSkillErrorResult(command.eventId, "Failed to list skills: ${e.message}")
                }
            }
        }
    }

    suspend fun deleteSkill(command: SkillCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val skillId = requireNotNull(command.skillId) { "Skill ID is required" }

                    val deletedSkill = skillService.deleteSkill(skillId)

                    if (deletedSkill) {
                        kafkaProducerService.sendSkillResult(
                            SkillResultEvent(
                                eventId = command.eventId,
                                success = true
                            )
                        )

                        kafkaProducerService.sendSkillAction(
                            SkillActionEvent(
                                eventId = UUID.randomUUID().toString(),
                                actionType = "SKILL_DELETED",
                                skillId = command.skillId,
                            )
                        )
                    }
                } catch (e: Exception) {
                    log.error("Failed to delete skill: ${e.message}", e)
                    sendSkillErrorResult(command.eventId, "Failed to delete skill: ${e.message}")
                }
            }
        }
    }

    suspend fun updateSkill(command: SkillCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val skillId = requireNotNull(command.skillId) { "Skill ID is required" }

                    val updatedSkill = skillService.updateSkill(
                        skillId = skillId,
                        name = command.name,
                        type = command.type
                    )

                    kafkaProducerService.sendSkillResult(
                        SkillResultEvent(
                            eventId = command.eventId,
                            skills = listOf(updatedSkill.toDto()),
                            success = true
                        )
                    )

                } catch (e: IllegalArgumentException) {
                    log.error("Validation error updating skill: ${e.message}")
                    sendSkillErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to update skill: ${e.message}", e)
                    sendSkillErrorResult(command.eventId, "Failed to update skill: ${e.message}")
                }
            }
        }
    }

    suspend fun createSkill(command: SkillCommandEvent) {
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    val createdSkill = skillService.createSkill(
                        name = requireNotNull(command.name),
                        type = requireNotNull(command.type)
                    )

                    kafkaProducerService.sendSkillResult(
                        SkillResultEvent(
                            eventId = command.eventId,
                            skills = listOf(createdSkill.toDto()),
                            success = true
                        )
                    )

                } catch (e: IllegalArgumentException) {
                    log.error("Validation error creating skill: ${e.message}")
                    sendSkillErrorResult(command.eventId, "Validation error: ${e.message}")
                } catch (e: Exception) {
                    log.error("Failed to create skill: ${e.message}", e)
                    sendSkillErrorResult(command.eventId, "Failed to create skill: ${e.message}")
                }
            }
        }
    }

    private fun User.toDto() = UserDto(
        id = id,
        login = login,
        email = email,
        fullName = fullName,
        globalRole = globalRole,
        skillIds = userSkills.map { it.skill.id },
        createdAt = createdAt.toString(),
        avatarUrl = avatarUrl
    )

    private fun Skill.toDto() = SkillDto(
        id = id,
        name = name,
        type = type,
        userCount = this.userSkills.count()
    )

    private fun sendUserErrorResult(eventId: String, message: String) {
        kafkaProducerService.sendUserResult(
            UserResultEvent(
                eventId = eventId,
                success = false,
                errorMessage = message
            )
        )
    }

    private fun sendSkillErrorResult(eventId: String, message: String) {
        kafkaProducerService.sendSkillResult(
            SkillResultEvent(
                eventId = eventId,
                skills = null,
                success = false,
                errorMessage = message
            )
        )
    }
}