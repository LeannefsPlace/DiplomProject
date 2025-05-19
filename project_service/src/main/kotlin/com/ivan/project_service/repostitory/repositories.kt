package com.ivan.project_service.repostitory

import com.ivan.project_service.model.Project
import com.ivan.project_service.model.ProjectUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProjectRepository : JpaRepository<Project, Int> {
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.userId = :userId")
    fun findAllByUserId(userId: Int): List<Project>
}

@Repository
interface ProjectUserRepository : JpaRepository<ProjectUser, Int> {
    fun findByProjectIdAndUserId(projectId: Int, userId: Int): Optional<ProjectUser>
    fun existsByProjectIdAndUserId(projectId: Int, userId: Int): Boolean
    fun findAllByProject_Id(projectId: Int): MutableList<ProjectUser>
}