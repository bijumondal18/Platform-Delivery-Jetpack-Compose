package com.platform.platformdelivery.data.models

data class LoginResponse(
    var success: Boolean,
    var message: String,
    var data: LoginData
)

data class LoginData(
    var userId: String,
    var name: String,
)