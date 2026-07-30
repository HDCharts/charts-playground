package presentation.editor

import androidx.lifecycle.ViewModel
import domain.EditorAction
import domain.EditorStore

class EditorViewModel(
    private val store: EditorStore,
) : ViewModel() {
    val state = store.state

    fun dispatch(action: EditorAction) {
        store.dispatch(action)
    }
}
