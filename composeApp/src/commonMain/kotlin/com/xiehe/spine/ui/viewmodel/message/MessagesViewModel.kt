package com.xiehe.spine.ui.viewmodel.message
import com.xiehe.spine.ui.viewmodel.shared.BaseViewModel

import com.xiehe.spine.core.model.AppResult
import com.xiehe.spine.core.store.UserSession
import com.xiehe.spine.data.NotificationMessage
import com.xiehe.spine.data.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagesUiState(
    val loading: Boolean = false,
    val items: List<NotificationMessage> = emptyList(),
    val errorMessage: String? = null,
)

class MessagesViewModel : BaseViewModel() {
    private val _state = MutableStateFlow(MessagesUiState())
    val state: StateFlow<MessagesUiState> = _state.asStateFlow()

    fun load(
        session: UserSession,
        repository: NotificationRepository,
        onSessionUpdated: (UserSession) -> Unit,
        silent: Boolean = false,
    ) {
        scope.launch {
            if (!silent) {
                _state.update { it.copy(loading = true, errorMessage = null) }
            }
            when (val result = repository.loadMessages(session = session, page = 1, pageSize = 50)) {
                is AppResult.Success -> {
                    onSessionUpdated(result.data.first)
                    _state.update {
                        it.copy(
                            loading = false,
                            items = result.data.second.items,
                            errorMessage = null,
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }
}

