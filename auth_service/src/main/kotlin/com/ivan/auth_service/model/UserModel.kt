package com.ivan.auth_service.model

open class UserModel(
    val login: String = "",

    var email: String = "",

    val password: String = "",

    var fullName: String? = null,

    var globalRole: String? = null,
)