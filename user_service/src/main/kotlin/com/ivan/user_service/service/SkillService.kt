package com.ivan.user_service.service

import com.ivan.user_service.model.Skill
import com.ivan.user_service.repository.SkillRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SkillService(
    private val skillRepository: SkillRepository
) {

    // Создание навыка
    @Transactional
    fun createSkill(name: String, type: String): Skill =
        skillRepository.save(Skill(name = name, type = type))

    // Получение всех навыков
    fun getAllSkills(): List<Skill> =
        skillRepository.findAll()

    fun getAllSkillsByType(type: String): List<Skill> =
        skillRepository.findByType(type)

    // Обновление навыка
    @Transactional
    fun updateSkill(skillId: Int, name: String? = null, type: String? = null): Skill {
        val skill = skillRepository.findById(skillId)
            .orElseThrow { IllegalArgumentException("Skill not found") }

        name?.let { skill.name = it }
        type?.let { skill.type = it }

        return skillRepository.save(skill)
    }

    // Удаление навыка
    @Transactional
    fun deleteSkill(skillId: Int): Boolean =
        skillRepository.findById(skillId).map {
            skillRepository.delete(it)
            true
        }.orElse(false)
}