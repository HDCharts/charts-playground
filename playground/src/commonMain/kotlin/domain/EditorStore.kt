package domain

import kotlinx.coroutines.flow.StateFlow

interface EditorStore {
    val state: StateFlow<ChartEditorState>

    fun dispatch(action: EditorAction)
}
