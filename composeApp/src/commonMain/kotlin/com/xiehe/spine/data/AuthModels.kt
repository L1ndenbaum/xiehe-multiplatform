package com.xiehe.spine.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    @SerialName("remember_me") val rememberMe: Boolean,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerialName("confirm_password") val confirmPassword: String,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class TokenPayload(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int? = null,
)

@Serializable
data class LoginData(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Int,
    val user: UserDto,
)

@Serializable
data class RefreshData(
    val tokens: TokenPayload,
)

@Serializable
data class RegisterData(
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)
