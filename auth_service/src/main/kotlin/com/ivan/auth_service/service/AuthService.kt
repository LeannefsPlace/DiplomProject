package com.ivan.auth_service.service

import com.ivan.auth_service.kafka.UserCommandEvent
import com.ivan.auth_service.kafka.UserCommandType
import com.ivan.auth_service.model.Session
import com.ivan.auth_service.model.UserModel
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID
import javax.security.auth.login.CredentialException

@Service
class AuthService(
    private val kafkaRequestReplyService: KafkaRequestReplyService,
    private val passwordEncoder: PasswordEncoder,
    private val sessionService: SessionService, ) {

    suspend fun register(user: UserModel) : Boolean {
        val userCommand = UserCommandEvent(
            eventId = UUID.randomUUID().toString(),
            commandType = UserCommandType.CREATE,
            login = user.login,
            email = user.email,
            passwordHash = passwordEncoder.encode(user.password),
            fullName = user.fullName,
            globalRole = user.globalRole?:"USER"
        )
        
        val userResponse = kafkaRequestReplyService.getUserEvent(userCommand, Duration.ofSeconds(3))

        if (!userResponse.success) throw Exception("Error registering new user: " + userResponse.errorMessage)

        return true
    }

    suspend fun loginWithSessionCreation(user: UserModel) : Session {
        val userCommand = UserCommandEvent(
            eventId = UUID.randomUUID().toString(),
            commandType = UserCommandType.GET,
            login = user.login
        )

        val userResponse = kafkaRequestReplyService.getUserEvent(userCommand, Duration.ofSeconds(3))

        if (!userResponse.success)
            throw Exception("Error logging in: " + userResponse.errorMessage)
        requireNotNull(userResponse.users)
        if (!passwordEncoder.matches(user.password, userResponse.passwordHash))
            throw CredentialException("Password for user " + user.login + " does not match hash")

        return sessionService.createSession(userResponse.users[0].id)
    }

    suspend fun logout(user: UserModel) : Boolean {

        val userCommand = UserCommandEvent(
            eventId = UUID.randomUUID().toString(),
            commandType = UserCommandType.GET,
            login = user.login
        )

        val userResponse = kafkaRequestReplyService.getUserEvent(userCommand, Duration.ofSeconds(3))

        if (!userResponse.success)
            throw Exception("Error logging in: " + userResponse.errorMessage)
        requireNotNull(userResponse.users)
        if (!passwordEncoder.matches(user.password, userResponse.passwordHash))
            throw CredentialException("Password for user " + user.login + " does not match hash")

        return sessionService.expireAll(userResponse.users[0].id)
    }

    suspend fun changePassword(userModel: UserModel, password: String): Boolean {
        val userResponse = kafkaRequestReplyService.getUserEvent(
            UserCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = UserCommandType.GET,
                login = userModel.login
        ), Duration.ofSeconds(3))
        if (!userResponse.success) throw Exception("Error changing password: " + userResponse.errorMessage)
        if(passwordEncoder.matches(password, userResponse.passwordHash)){
            val userUpdateResponse = kafkaRequestReplyService.getUserEvent(
                UserCommandEvent(
                    eventId = UUID.randomUUID().toString(),
                    commandType = UserCommandType.EDIT,
                    userId = userResponse.users?.get(0)?.id?:throw CredentialException("User ID does not match hash"),
                    email = null,
                    passwordHash = passwordEncoder.encode(password),
                    avatarUrl = null,
                    fullName = null,
                    globalRole = null,
                    skillIds = emptyList()
                ), Duration.ofSeconds(3))
            return userUpdateResponse.success
        }
        else return false
    }
}