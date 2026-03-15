package model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PlaygroundViewModel(
    val registry: PlaygroundChartRegistry = playgroundChartRegistry,
) : ViewModel() {
    private val _state = MutableStateFlow(defaultPlaygroundState(registry))
    val state: StateFlow<PlaygroundState> = _state.asStateFlow()

    fun dispatch(action: PlaygroundAction) {
        if (action is PlaygroundAction.LoadSnapshotMetadata) {
            val shouldStartFetch =
                _state.value.snapshotMetadataLoading && _state.value.snapshotMetadata == null

            _state.update { currentState ->
                PlaygroundReducer.reduce(
                    state = currentState,
                    action = action,
                    registry = registry,
                )
            }

            if (shouldStartFetch) {
                fetchSnapshotMetadata()
            }
            return
        }

        _state.update { currentState ->
            PlaygroundReducer.reduce(
                state = currentState,
                action = action,
                registry = registry,
            )
        }
    }

    private fun fetchSnapshotMetadata() {
        viewModelScope.launch {
            val metadata =
                runCatching {
                    val response = window.fetch(SNAPSHOT_METADATA_URL).await()
                    if (!response.ok) return@runCatching null
                    parseSnapshotMetadata(response.text().await())
                }.getOrNull()

            dispatch(PlaygroundAction.SnapshotMetadataLoaded(metadata))
        }
    }
}

private const val SNAPSHOT_METADATA_URL = "/static/_meta/playground-snapshot-publish.json"
private val snapshotMetadataJson = Json { ignoreUnknownKeys = true }

private fun parseSnapshotMetadata(rawJson: String): SnapshotPublishMetadata? =
    snapshotMetadataJson.decodeFromString(rawJson)
