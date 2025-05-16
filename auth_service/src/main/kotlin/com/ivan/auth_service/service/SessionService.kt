package com.ivan.auth_service.service

import com.ivan.auth_service.model.Session
import com.ivan.auth_service.repository.SessionRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    @Qualifier("jpaDispatcher") private val jpaDispatcher: CoroutineDispatcher,
    private val transactionTemplate: TransactionTemplate,
) {
    @Transactional
    suspend fun createSession(userId: Int): Session = withContext(jpaDispatcher) {
        try {
            sessionRepository.save(
                Session(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    expirationDate = Instant.now().plus(Duration.ofDays(5)),
                    createdAt = Instant.now()
                )
            )
        } catch (e: Exception) {
            println("Session creation failed for user $userId: " + e)
            throw IllegalStateException("Failed to create session", e)
        }
    }

    @Transactional
    suspend fun checkSession(sessionId: String) : Int =
        withContext(jpaDispatcher) {
                try {
                    val session = sessionRepository.findById(sessionId).orElseThrow()

                    requireNotNull(session.expirationDate)
                    
                    if (session.expirationDate!! > Instant.now()) session.userId
                    else {
                        sessionRepository.deleteById(sessionId)
                        -1
                    }
                } catch (e: Exception) {
                    println("Exception while checking session: ${e.message}")
                    throw IllegalArgumentException("Error while creating session", e)
                }?:throw IllegalStateException("Transaction failed")
        }

    suspend fun expireSession(sessionId: String) : Boolean =
        withContext(jpaDispatcher) {
                try {
                    sessionRepository.deleteById(sessionId)
                    true
                } catch (e: Exception) {
                    println("Exception while deleting session: ${e.message}")
                    throw IllegalArgumentException("Error while deleting session", e)
                }?: throw IllegalStateException("Transaction failed")
        }

    suspend fun expireAll(userId: Int) : Boolean =
        withContext(jpaDispatcher) {
            transactionTemplate.execute {
                try {
                    sessionRepository.deleteAllByUserId(userId)
                    true
                } catch (e: Exception) {
                    println("Exception while expiring session: ${e.message}")
                    throw IllegalArgumentException("Error while expiring session", e)
                }
            }?: throw IllegalStateException("Transaction failed")
        }

    suspend fun expireAllExceptCurrent(userId: Int, currentSessionId: String) : Boolean =
        withContext(jpaDispatcher) {
            withContext(jpaDispatcher) {
                try {
                    sessionRepository.deleteAllByUserIdExcept(
                        userId = userId,
                        excludedSessionId = currentSessionId
                    )
                    true
                } catch (e: Exception) {
                    println("Exception while exception deleting session: ${e.message}")
                    throw IllegalArgumentException("Error while exception deleting session", e)
                }
            }?: throw IllegalStateException("Transaction failed")
        }
}