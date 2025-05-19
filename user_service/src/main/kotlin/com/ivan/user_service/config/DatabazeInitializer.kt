package com.ivan.user_service.config
import com.ivan.user_service.service.SkillService
import com.ivan.user_service.service.UserService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.thread

@Component
class DatabaseInitializer(
    private val userService: UserService,
    private val skillService: SkillService
) {
    @PostConstruct
    fun init() {
        thread {
            runBlocking {
                val skillNames =
                    arrayOf("SQL", "kotlin", "mongodb", "kafka", "reactive web", "leadership", "git administration")
                val typeNames =
                    arrayOf("Language", "Soft skill", "common lore")

                for (name in skillNames) {
                    skillService.createSkill(name,
                        typeNames.random())
                }
            }

            runBlocking {
                val names =
                    arrayOf("oleg", "admin", "artyom", "anton", "jason", "admin2", "semen", "lobanov", "kirill", "user3")

                for (name in names) {
                    val skillIds = LinkedList<Int>()
                    for(a: Int in 1..7) if (Random().nextBoolean()){
                        skillIds.add(a)
                    }
                    userService.createUser(
                        login = name,
                        email = name+"@gmail.com",
                        passwordHash = BCryptPasswordEncoder().encode(name),
                        fullName = name + " " + name,
                        globalRole = if (name.contains("admin")) "ADMIN" else "USER",
                        skillIds = skillIds
                    )
                }
            }
        }
    }
}
