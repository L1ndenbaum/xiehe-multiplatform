package com.xiehe.spine.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val code: Int,
    val message: String,
    val data: T? = null,
    val timestamp: String? = null,
)

@Serializable
data class ApiErrorEnvelope(
    val code: Int? = null,
    val message: String,
    @SerialName("error_code") val errorCode: String? = null,
    val path: String? = null,
    val timestamp: String? = null,
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    @SerialName("remember_me") val rememberMe: Boolean,
)

@Serializable
data class RefreshRequest(
    @SerialName("refresh_token") val refreshToken: String,
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
data class UserDto(
    val id: Int,
    val username: String,
    val email: String? = null,
    @SerialName("full_name") val fullName: String? = null,
)

@Serializable
data class DashboardOverview(
    @SerialName("total_patients") val totalPatients: Int,
    @SerialName("new_patients_today") val newPatientsToday: Int,
    @SerialName("new_patients_week") val newPatientsWeek: Int,
    @SerialName("active_patients") val activePatients: Int,
    @SerialName("total_images") val totalImages: Int,
    @SerialName("images_today") val imagesToday: Int,
    @SerialName("images_week") val imagesWeek: Int,
    @SerialName("pending_images") val pendingImages: Int,
    @SerialName("processed_images") val processedImages: Int,
    @SerialName("completion_rate") val completionRate: Double,
    @SerialName("average_processing_time") val averageProcessingTime: Double,
    @SerialName("system_alerts") val systemAlerts: Int,
)

@Serializable
data class PatientPageData(
    val items: List<PatientSummary>,
    val pagination: Pagination,
)

@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class PatientSummary(
    val id: Int,
    @SerialName("patient_id") val patientId: String,
    val name: String,
    val gender: String,
    val age: Int,
    val phone: String? = null,
    val status: String? = null,
)

@Serializable
data class PatientDetail(
    val id: Int,
    @SerialName("patient_id") val patientId: String,
    val name: String,
    val gender: String,
    @SerialName("birth_date") val birthDate: String,
    val age: Int,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    @SerialName("emergency_contact_name") val emergencyContactName: String? = null,
    @SerialName("emergency_contact_phone") val emergencyContactPhone: String? = null,
    @SerialName("id_card") val idCard: String? = null,
)

@Serializable
data class CreatePatientRequest(
    @SerialName("patient_id") val patientId: String,
    val name: String,
    val gender: String,
    @SerialName("birth_date") val birthDate: String,
    val phone: String,
    @SerialName("id_card") val idCard: String,
    val address: String,
)
