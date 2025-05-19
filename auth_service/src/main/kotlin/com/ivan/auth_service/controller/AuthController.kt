package com.ivan.auth_service.controller

import com.ivan.auth_service.model.UserModel
import com.ivan.auth_service.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.security.auth.login.CredentialException

@RestController
class AuthController(private val authService: AuthService) {

    @PostMapping("/auth/login", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun login(
        @RequestBody userModel: UserModel,
    ) : ResponseEntity<String> {
        return try {
            ResponseEntity.ok(authService.loginWithSessionCreation(userModel).id!!)
        } catch (e:CredentialException){
            ResponseEntity<String>("Wrong password!", HttpStatus.FORBIDDEN)
        } catch (e:Exception){
            ResponseEntity<String>("User with such login not found!", HttpStatus.FORBIDDEN)
        }
    }

    @PostMapping("/auth/register", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun register(
        @RequestBody userModel: UserModel,
    ) : ResponseEntity<String> {
        return try {
            if (authService.register(userModel)) ResponseEntity.ok( "success") else ResponseEntity("fail", HttpStatus.BAD_REQUEST)
        } catch (e:Exception){
            ResponseEntity("Error while registering: + ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/auth/logout", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun logout(
        @RequestBody userModel: UserModel,
    ) : ResponseEntity<String>  {
        return try {
            if (authService.logout(userModel)) ResponseEntity.ok( "success") else ResponseEntity("fail", HttpStatus.BAD_REQUEST)
        } catch (e:Exception){
            ResponseEntity("Error while logout: + ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/auth/edit", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun edit(
        @RequestBody updateModel: UpdateModel,
    ) : ResponseEntity<String> {
        return try {
            if (authService.changePassword(updateModel.toUserModel(), updateModel.password)) ResponseEntity.ok( "success") else ResponseEntity("fail", HttpStatus.BAD_REQUEST)
        } catch (e:Exception){
            ResponseEntity("Error while changing password: + ${e.message}", HttpStatus.BAD_REQUEST)
        }
    }

    data class UpdateModel(
        val newPassword: String,
    ): UserModel(){
        fun toUserModel(): UserModel {
            return UserModel(
                login = this.login,
                email = this.email,
                password = this.newPassword,
                fullName = this.fullName,
                globalRole = this.globalRole
            )
        }
    }
}