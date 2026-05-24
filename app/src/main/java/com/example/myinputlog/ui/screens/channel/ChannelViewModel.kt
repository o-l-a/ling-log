package com.example.myinputlog.ui.screens.channel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.myinputlog.data.repository.StorageDataRepository
import com.example.myinputlog.ui.navigation.ChannelRoute
import com.example.myinputlog.ui.screens.video.ChannelMetadata
import com.example.myinputlog.ui.screens.video.toChannelMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, private val storageDataRepository: StorageDataRepository
) : ViewModel() {
    private val channelRoute = savedStateHandle.toRoute<ChannelRoute>()
    private val courseId = channelRoute.courseId
    private val channelId = channelRoute.channelId

    private val _channelMetadata = MutableStateFlow(ChannelMetadata())
    private val _loadingState = MutableStateFlow<ChannelLoadState>(ChannelLoadState.Loading)

    val channelUiState: StateFlow<ChannelUiState> = combine(
        _channelMetadata, _loadingState
    ) { meta, state ->
        when (state) {
            is ChannelLoadState.Loading -> ChannelUiState.Loading
            is ChannelLoadState.Error -> ChannelUiState.Error
            is ChannelLoadState.Success -> ChannelUiState.Success(
                channelMetadata = meta,
                channelLoadState = state
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChannelUiState.Loading
    )

    init {
        loadChannel()
    }

    private fun loadChannel() {
        viewModelScope.launch {
            val channelMetadata =
                storageDataRepository.getYouTubeChannel(courseId, channelId)?.toChannelMetadata()
            if (channelMetadata != null) {
                _channelMetadata.value = channelMetadata
                _loadingState.value = ChannelLoadState.Success
            } else {
                _loadingState.value = ChannelLoadState.Error
            }
        }
    }
}