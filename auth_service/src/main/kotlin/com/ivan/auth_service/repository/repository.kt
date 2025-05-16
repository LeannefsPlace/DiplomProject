package com.ivan.auth_service.repository

import com.ivan.auth_service.model.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SessionRepository : JpaRepository<Session, String> {

    fun findAllByUserId(userId: Int): List<Session>

    @Modifying
    @Query("DELETE FROM Session s WHERE s.userId = :userId AND s.id != :excludedSessionId")
    fun deleteAllByUserIdExcept(userId: Int, excludedSessionId: String)

    @Modifying
    fun deleteAllByUserId(userId: Int)
}