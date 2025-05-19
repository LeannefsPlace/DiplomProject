package com.ivan.gateway_service.service

import com.ivan.gateway_service.kafka.SkillCommandEvent
import com.ivan.gateway_service.kafka.SkillCommandType
import com.ivan.gateway_service.kafka.SkillDto
import com.ivan.gateway_service.service.query.SkillQueryService
import org.springframework.stereotype.Service
import java.util.*

@Service
class SkillService(private val skillQueryService: SkillQueryService) {

    suspend fun listSkills() : List<SkillDto>{
        val response = skillQueryService.getSkillEvent(
            SkillCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = SkillCommandType.LIST
            )
        )

        return if (response.success && response.skills != null)
                response.skills
            else
                throw IllegalStateException("something went wrong while fetching data ${response.errorMessage}")
    }

    suspend fun listSkillsByType(type: String):List<SkillDto>{
        val response = skillQueryService.getSkillEvent(
            SkillCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = SkillCommandType.FIND_BY_TYPE,
                type = type
            )
        )

        return if (response.success && response.skills != null)
            response.skills
        else
            throw IllegalStateException("something went wrong while fetching data ${response.errorMessage}")
    }

    suspend fun deleteSkill(skillId:Int) : Boolean{
        val response = skillQueryService.getSkillEvent(
            SkillCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = SkillCommandType.DELETE,
                skillId = skillId
            )
        )

        return response.success
    }

    suspend fun getSkill(skillId:Int) : SkillDto{
        val response = skillQueryService.getSkillEvent(
            SkillCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = SkillCommandType.LIST
            )
        )

        return response.skills?.filter { it.id == skillId }?.firstOrNull()?:throw IllegalStateException("something went wrong while fetching data ${response.errorMessage}")
    }

    suspend fun addSkill(skillDto:SkillDto) : Int{
        val response = skillQueryService.getSkillEvent(
            SkillCommandEvent(
                eventId = UUID.randomUUID().toString(),
                commandType = SkillCommandType.CREATE,
                name = skillDto.name,
                type = skillDto.type
            )
        )

        return response.skills?.get(0)?.id ?:
            throw IllegalStateException(
                "something went wrong while fetching data ${response.errorMessage}"
            )
    }
}