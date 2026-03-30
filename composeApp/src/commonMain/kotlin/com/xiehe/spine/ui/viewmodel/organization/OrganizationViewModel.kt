package com.xiehe.spine.ui.viewmodel.organization

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.organization.OrganizationInvitation
import com.xiehe.spine.data.organization.OrganizationMember
import com.xiehe.spine.data.organization.OrganizationRepository
import com.xiehe.spine.data.organization.OrganizationTeamSummary
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class OrganizationTab {
    MEMBERS,
    INVITES,
}

data class OrganizationUiState(
    val loading: Boolean = false,
    val actionLoading: Boolean = false,
    val activeTab: OrganizationTab = OrganizationTab.MEMBERS,
    val search: String = "",
    val teams: List<OrganizationTeamSummary> = emptyList(),
    val selectedTeamId: Int? = null,
    val members: List<OrganizationMember> = emptyList(),
    val filteredMembers: List<OrganizationMember> = emptyList(),
    val invitations: List<OrganizationInvitation> = emptyList(),
    val filteredInvitations: List<OrganizationInvitation> = emptyList(),
    val noticeMessage: String? = null,
    val errorMessage: String? = null,
)

class OrganizationViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(OrganizationUiState())
    val state: StateFlow<OrganizationUiState> = _state.asStateFlow()

    fun load(
        session: UserSession,
        repository: OrganizationRepository,
        onSessionUpdated: (UserSession) -> Unit,
        silent: Boolean = false,
    ) {
        scope.launch {
            if (!silent) {
                _state.update { it.copy(loading = true, errorMessage = null) }
            }

            val teamsResultDeferred = async { repository.loadMyTeams(session) }
            val invitationsResultDeferred = async { repository.loadMyInvitations(session) }

            val teamsResult = teamsResultDeferred.await()
            val invitationsResult = invitationsResultDeferred.await()

            val teams = (teamsResult as? AppResult.Success)?.data?.second?.items.orEmpty()
            val selectedTeamId = resolveSelectedTeamId(
                current = _state.value.selectedTeamId,
                teams = teams,
            )
            val membersResult = if (selectedTeamId != null) {
                repository.loadTeamMembers(
                    session = (teamsResult as? AppResult.Success)?.data?.first ?: session,
                    teamId = selectedTeamId,
                )
            } else {
                null
            }

            listOfNotNull(
                (teamsResult as? AppResult.Success)?.data?.first,
                (invitationsResult as? AppResult.Success)?.data?.first,
                (membersResult as? AppResult.Success)?.data?.first,
            ).lastOrNull()?.let(onSessionUpdated)

            val members = (membersResult as? AppResult.Success)?.data?.second?.members.orEmpty()
            val invitations = (invitationsResult as? AppResult.Success)?.data?.second?.items.orEmpty()
            val failure = listOfNotNull(
                teamsResult as? AppResult.Failure,
                invitationsResult as? AppResult.Failure,
                membersResult as? AppResult.Failure,
            ).firstOrNull()

            _state.update { current ->
                val next = current.copy(
                    loading = false,
                    teams = teams,
                    selectedTeamId = selectedTeamId,
                    members = members,
                    invitations = invitations,
                    errorMessage = failure?.message,
                )
                next.withFilteredData()
            }
        }
    }

    fun updateSearch(value: String) {
        _state.update { current ->
            current.copy(search = value).withFilteredData()
        }
    }

    fun selectTab(value: OrganizationTab) {
        _state.update { current ->
            current.copy(activeTab = value, search = "").withFilteredData()
        }
    }

    fun respondInvitation(
        session: UserSession,
        repository: OrganizationRepository,
        invitation: OrganizationInvitation,
        accept: Boolean,
        onSessionUpdated: (UserSession) -> Unit,
    ) {
        val invitationId = invitation.stableId ?: return
        scope.launch {
            _state.update { it.copy(actionLoading = true, errorMessage = null, noticeMessage = null) }
            when (val result = repository.respondInvitation(session, invitationId, accept)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update { current ->
                        val next = current.copy(
                            actionLoading = false,
                            invitations = current.invitations.filterNot { it.stableId == invitationId },
                            noticeMessage = result.data.second,
                            errorMessage = null,
                        )
                        next.withFilteredData()
                    }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(actionLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    private fun resolveSelectedTeamId(
        current: Int?,
        teams: List<OrganizationTeamSummary>,
    ): Int? {
        if (teams.isEmpty()) {
            return null
        }
        return current?.takeIf { candidate -> teams.any { it.id == candidate } } ?: teams.first().id
    }

    private fun OrganizationUiState.withFilteredData(): OrganizationUiState {
        return copy(
            filteredMembers = members.filter { it.matchesSearch(search) },
            filteredInvitations = invitations.filter { it.matchesSearch(search) },
        )
    }

    private fun OrganizationMember.matchesSearch(keyword: String): Boolean {
        val normalized = keyword.trim().lowercase()
        if (normalized.isBlank()) {
            return true
        }
        return listOfNotNull(
            realName,
            fullName,
            username,
            email,
            department,
            role,
            status,
        ).any { value ->
            value.lowercase().contains(normalized)
        }
    }

    private fun OrganizationInvitation.matchesSearch(keyword: String): Boolean {
        val normalized = keyword.trim().lowercase()
        if (normalized.isBlank()) {
            return true
        }
        return listOfNotNull(
            teamName,
            team?.name,
            inviterName,
            inviter?.realName,
            inviter?.fullName,
            inviter?.username,
            inviterEmail,
            inviter?.email,
            email,
            role,
            status,
            message,
        ).any { value ->
            value.lowercase().contains(normalized)
        }
    }
}

val OrganizationUiState.selectedTeam: OrganizationTeamSummary?
    get() = teams.firstOrNull { it.id == selectedTeamId }

fun OrganizationUiState.currentMember(userId: Int): OrganizationMember? {
    return members.firstOrNull { it.userId == userId }
}

val OrganizationInvitation.stableId: Int?
    get() = id ?: invitationId
