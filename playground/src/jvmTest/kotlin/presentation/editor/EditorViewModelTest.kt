package presentation.editor

import domain.ChartEditorState
import domain.ChartType
import domain.EditorAction
import domain.EditorStore
import domain.RightPanelTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals

class EditorViewModelTest {
    @Test
    fun dispatch_forwards_actions_to_store() {
        val store = TestEditorStore()
        val viewModel = EditorViewModel(store)

        viewModel.dispatch(EditorAction.SelectChart(ChartType.PIE))

        assertEquals(EditorAction.SelectChart(ChartType.PIE), store.lastAction)
    }

    private class TestEditorStore : EditorStore {
        private val stateFlow =
            MutableStateFlow(
                ChartEditorState(
                    selectedChartType = ChartType.LINE,
                    rightPanelTab = RightPanelTab.SETTINGS,
                    sessions = emptyMap(),
                    primaryChartTypes = emptyList(),
                    overflowChartTypes = emptyList(),
                ),
            )

        var lastAction: EditorAction? = null
            private set

        override val state: StateFlow<ChartEditorState> = stateFlow

        override fun dispatch(action: EditorAction) {
            lastAction = action
        }
    }
}
