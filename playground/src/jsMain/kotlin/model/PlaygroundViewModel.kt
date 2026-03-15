package model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PlaygroundViewModel(
    val registry: PlaygroundChartRegistry = playgroundChartRegistry,
) : ViewModel() {
    private val httpClient =
        HttpClient {
            install(ContentNegotiation) {
                json(snapshotMetadataJson)
            }
        }

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
                    val response = httpClient.get(SNAPSHOT_METADATA_URL)
                    if (!response.status.isSuccess()) return@runCatching null
                    response.body<SnapshotPublishMetadata>()
                }.getOrNull()

            dispatch(PlaygroundAction.SnapshotMetadataLoaded(metadata))
        }
    }

    override fun onCleared() {
        httpClient.close()
        super.onCleared()
    }
}

private const val SNAPSHOT_METADATA_URL = "/static/_meta/playground-snapshot-publish.json"
private val snapshotMetadataJson = Json { ignoreUnknownKeys = true }
