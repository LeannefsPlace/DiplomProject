package com.ivan.auth_service.controller

import com.ivan.auth_service.model.UserModel
import com.ivan.auth_service.service.AuthService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.security.auth.login.CredentialException

@RestController
class AuthController(private val authService: AuthService) {

    @PostMapping("/auth/login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun login(
        @RequestBody userModel: UserModel,
    ) : String {
        return try {
            authService.loginWithSessionCreation(userModel).id!!
        } catch (e:CredentialException){
            "Wrong password"
        } catch (e:Exception){
            "User not found: " + e.message
        }
    }

    @PostMapping("/auth/register", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun register(
        @RequestBody userModel: UserModel,
    ) : String {
        return try {
            if (authService.register(userModel)) "success" else "fail"
        } catch (e:Exception){
            "Error while registering: " + e.message
        }
    }

    @PostMapping("/auth/logout", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun logout(
        @RequestBody userModel: UserModel,
    ) : String {
        return try {
            if (authService.logout(userModel)) "success" else "fail"
        } catch (e:Exception){
            "Error while executing sessions: " + e.message
        }
    }


}