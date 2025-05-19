package com.ivan.project_service.kafka

import com.ivan.project_service.model.ProjectRole
import com.ivan.project_service.service.ProjectService
import com.ivan.project_service.service.ProjectUserService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.thread

@Component
class DatabaseInitializer(
    private val projectService: ProjectService,
    private val projectUserService: ProjectUserService
) {
    @PostConstruct
    fun init() {
        Thread.sleep(3000)
        thread {
            runBlocking {
                for (a:Int in 1..8)
                    projectService.createProject(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.CREATE,
                        userId = a,
                        name = "project number " + a,
                        isActive = Random().nextBoolean(),
                        description = "Project about doing something with code. This is project number ${a}"
                    )
                    )
            }

            runBlocking {
                projectUserService.assign(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.ASSIGN,
                        userId = 9,
                        projectId = 1,
                        projectRole = ProjectRole.MEMBER
                    )
                )

                projectUserService.assign(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.ASSIGN,
                        userId = 9,
                        projectId = 5,
                        projectRole = ProjectRole.MANAGER
                    )
                )

                projectUserService.assign(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.ASSIGN,
                        userId = 5,
                        projectId = 7,
                        projectRole = ProjectRole.MEMBER
                    )
                )

                projectUserService.assign(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.ASSIGN,
                        userId = 3,
                        projectId = 6,
                        projectRole = ProjectRole.MANAGER
                    )
                )

                projectUserService.assign(
                    ProjectCommandEvent(
                        eventId = UUID.randomUUID().toString(),
                        commandType = ProjectEventType.ASSIGN,
                        userId = 3,
                        projectId = 7,
                        projectRole = ProjectRole.MEMBER
                    )
                )
            }
        }
    }
}
