package com.xiehe.spine.data.organization

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.ApiClient
import com.xiehe.spine.data.auth.AuthRepository

class OrganizationRepository(
    private val apiClient: ApiClient,
    private val authRepository: AuthRepository,
) {
    suspend fun loadMyTeams(
        session: UserSession,
    ): AppResult<Pair<UserSession, OrganizationTeamListData>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(path = "/permissions/teams/my", accessToken = activeSession.accessToken)
        }
    }

    suspend fun loadTeamMembers(
        session: UserSession,
        teamId: Int,
    ): AppResult<Pair<UserSession, OrganizationTeamMembersData>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(
                path = "/permissions/teams/$teamId/members",
                accessToken = activeSession.accessToken,
            )
        }
    }

    suspend fun loadMyInvitations(
        session: UserSession,
    ): AppResult<Pair<UserSession, OrganizationInvitationListData>> {
        return withRefresh(session) { activeSession ->
            apiClient.get(
                path = "/permissions/invitations/my",
                accessToken = activeSession.accessToken,
            )
        }
    }

    suspend fun respondInvitation(
        session: UserSession,
        invitationId: Int,
        accept: Boolean,
    ): AppResult<Pair<UserSession, String>> {
        return withRefresh(session) { activeSession ->
            apiClient.postForMessage(
                path = "/permissions/invitations/$invitationId/respond",
                body = OrganizationInvitationRespondRequest(accept = accept),
                accessToken = activeSession.accessToken,
            )
        }
    }

    private suspend inline fun <reified T> withRefresh(
        session: UserSession,
        crossinline action: suspend (UserSession) -> AppResult<T>,
    ): AppResult<Pair<UserSession, T>> {
        return when (val first = action(session)) {
            is AppResult.Success -> AppResult.Success(session to first.data)
            is AppResult.Failure -> {
                if (!first.isUnauthorized) {
                    first
                } else {
                    when (val refreshed = authRepository.refreshToken(session)) {
                        is AppResult.Success -> {
                            when (val second = action(refreshed.data)) {
                                is AppResult.Success -> AppResult.Success(refreshed.data to second.data)
                                is AppResult.Failure -> second
                            }
                        }

                        is AppResult.Failure -> refreshed
                    }
                }
            }
        }
    }
}
