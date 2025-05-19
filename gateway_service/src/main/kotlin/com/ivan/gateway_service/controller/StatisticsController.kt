package com.ivan.gateway_service.controller

import com.ivan.gateway_service.kafka.TaskStatistics
import com.ivan.gateway_service.service.DetailedProjectService
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/statistics")
class StatisticsController(
    private val detailedProjectService: DetailedProjectService
) {
    @GetMapping("/{projectId}")
    suspend fun getStatsForProject(
        @PathVariable("projectId") projectId: Int,
    ) : ResponseEntity<TaskStatistics>{
        return ResponseEntity.ok(detailedProjectService.getProject(projectId).project.statistics)
    }
}