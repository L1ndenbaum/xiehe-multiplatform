package com.xiehe.spine.data

import com.xiehe.spine.core.store.InMemoryKeyValueStore
import com.xiehe.spine.core.store.KeyValueStore
import com.xiehe.spine.core.store.SessionStore
import com.xiehe.spine.core.store.ThemePreferenceRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val defaultBaseUrl = "http://115.190.121.59:8080/api/v1"

class AppContainer private constructor(
    val authRepository: AuthRepository,
    val dashboardRepository: DashboardRepository,
    val patientRepository: PatientRepository,
    val imageFileRepository: ImageFileRepository,
    val measurementRepository: MeasurementRepository,
    val themeRepository: ThemePreferenceRepository,
) {
    companion object {
        fun create(
            store: KeyValueStore,
            baseUrl: String = defaultBaseUrl,
            enableNetworkDiagnostics: Boolean = false,
            httpClient: HttpClient? = null,
        ): AppContainer {
            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
            val sharedHttpClient = httpClient ?: createDefaultHttpClient(
                json = json,
                enableNetworkDiagnostics = enableNetworkDiagnostics,
            )
            val instrumentedApiClient = ApiClient(
                httpClient = sharedHttpClient,
                baseUrl = baseUrl,
                enableDiagnostics = enableNetworkDiagnostics,
            )
            val sessionStore = SessionStore(store = store, json = json)
            val authRepository = AuthRepository(apiClient = instrumentedApiClient, sessionStore = sessionStore)
            return AppContainer(
                authRepository = authRepository,
                dashboardRepository = DashboardRepository(apiClient = instrumentedApiClient, authRepository = authRepository),
                patientRepository = PatientRepository(apiClient = instrumentedApiClient, authRepository = authRepository),
                imageFileRepository = ImageFileRepository(apiClient = instrumentedApiClient, authRepository = authRepository),
                measurementRepository = MeasurementRepository(apiClient = instrumentedApiClient, authRepository = authRepository),
                themeRepository = ThemePreferenceRepository(store = store),
            )
        }

        fun createInMemory(
            baseUrl: String = defaultBaseUrl,
            enableNetworkDiagnostics: Boolean = false,
            httpClient: HttpClient? = null,
        ): AppContainer {
            return create(
                store = InMemoryKeyValueStore(),
                baseUrl = baseUrl,
                enableNetworkDiagnostics = enableNetworkDiagnostics,
                httpClient = httpClient,
            )
        }

        private fun createDefaultHttpClient(
            json: Json,
            enableNetworkDiagnostics: Boolean,
        ): HttpClient {
            return HttpClient {
                install(ContentNegotiation) {
                    json(json)
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            // Keep network logging opt-in and non-crashing across targets.
                        }
                    }
                    level = if (enableNetworkDiagnostics) LogLevel.HEADERS else LogLevel.NONE
                }
            }
        }
    }
}
