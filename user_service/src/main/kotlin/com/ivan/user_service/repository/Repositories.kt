package com.ivan.user_service.repository

import com.ivan.user_service.model.Skill
import com.ivan.user_service.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Int> {
    fun findByLogin(login: String): User?

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN u.userSkills us
        WHERE us.skill.id IN :skillIds
    """)
    fun findBySkillIds(skillIds: List<Int>): List<User>
}

@Repository
interface SkillRepository : JpaRepository<Skill, Int> {
    fun findByType(type: String): List<Skill>
}