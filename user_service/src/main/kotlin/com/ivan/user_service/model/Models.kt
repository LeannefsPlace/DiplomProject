package com.ivan.user_service.model

import com.ivan.user_service.repository.SkillRepository
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, unique = true, length = 50)
    val login: String,

    @Column(nullable = false, unique = true, length = 100)
    var email: String,

    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,

    @Column(name = "full_name", length = 100)
    var fullName: String? = null,

    @Column(name = "avatar_url", length = 255)
    var avatarUrl: String? = null,

    @Column(name = "global_role", nullable = false, length = 20)
    var globalRole: String = "user",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userSkills: MutableSet<UserSkill> = mutableSetOf()

    fun getSkillIds(): Set<Int> = userSkills.map { it.skill.id }.toSet()

    fun updateSkills(newSkillIds: Set<Int>, skillRepository: SkillRepository) {
        val skillsToRemove = userSkills.filter { !newSkillIds.contains(it.skill.id) }
        userSkills.removeAll(skillsToRemove)

        val existingSkillIds = userSkills.map { it.skill.id }.toSet()
        newSkillIds.filterNot { existingSkillIds.contains(it) }.forEach { skillId ->
            skillRepository.findById(skillId).let { skill ->
                userSkills.add(UserSkill(user = this, skill = skill.orElseThrow()))
            }
        }
    }
}

@Entity
@Table(name = "skills")
class Skill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, length = 50)
    var type: String
) {
    @OneToMany(mappedBy = "skill", cascade = [CascadeType.ALL], orphanRemoval = true)
    val userSkills: MutableSet<UserSkill> = mutableSetOf()
}

@Entity
@Table(name = "user_skills")
class UserSkill(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    val skill: Skill
)