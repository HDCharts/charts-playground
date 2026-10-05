package presentation.editor

import androidx.lifecycle.ViewModel
import domain.EditorAction
import domain.EditorStore
import kotlin.time.Clock

class EditorViewModel(
    private val store: EditorStore,
    clock: Clock = Clock.System,
) : ViewModel() {
    val state = store.state

    val snapshotMetadata: SnapshotMetadataUi? = state.value.snapshotMetadata?.toUI(clock.now())

    fun dispatch(action: EditorAction) {
        store.dispatch(action)
    }
}
