package com.ivan.user_service.service

import com.ivan.user_service.model.User
import com.ivan.user_service.model.UserSkill
import com.ivan.user_service.repository.SkillRepository
import com.ivan.user_service.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val skillRepository: SkillRepository
) {

    @Transactional
    fun createUser(
        login: String,
        email: String,
        passwordHash: String,
        fullName: String? = null,
        globalRole: String = "user",
        skillIds: List<Int> = emptyList()
    ): User {
        val existingUser = userRepository.findByLogin(login)
        if (existingUser != null) {
            throw IllegalArgumentException("User with login $login already exists")
        }

        val user = User(
            login = login,
            email = email,
            passwordHash = passwordHash,
            fullName = fullName,
            globalRole = globalRole
        ).apply {
            skillIds.forEach { skillId ->
                val skill = skillRepository.findById(skillId)
                    .orElseThrow { IllegalArgumentException("Skill with id $skillId not found") }
                userSkills.add(UserSkill(user = this, skill = skill))
            }
        }

        return userRepository.save(user)
    }

    fun getUserById(id: Int): User? =
        userRepository.findById(id).orElse(null)

    fun getUserByLogin(login: String): User? =
        userRepository.findByLogin(login)

    @Transactional
    fun updateUser(
        userId: Int,
        email: String? = null,
        fullName: String? = null,
        avatarUrl: String? = null,
        passwordHash: String? = null,
        globalRole: String? = null,
        skillIds: List<Int>? = null
    ): User {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        email?.let { user.email = it }
        fullName?.let { user.fullName = it }
        avatarUrl?.let { user.avatarUrl = it }
        passwordHash?.let { user.passwordHash = it }
        globalRole?.let { user.globalRole = it }

        skillIds?.let {user.updateSkills(skillIds.toSet(), skillRepository)}

        return userRepository.save(user)
    }

    @Transactional
    fun deleteUser(userId: Int): Boolean =
        userRepository.findById(userId).map {
            userRepository.delete(it)
            true
        }.orElse(false)

    fun findUsersBySkills(skillIds: List<Int>): List<User> =
        userRepository.findBySkillIds(skillIds)

    fun getAllUsers(): List<User> =
        userRepository.findAll()
}