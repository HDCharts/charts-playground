package model

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PlaygroundValidationResult(
    val sanitizedEditor: DataEditorState?,
    val dataModel: PlaygroundDataModel?,
    val message: String,
    val invalidRowIds: Set<Int> = emptySet(),
)

enum class PlaygroundRightPanelTab {
    SETTINGS,
    CODE,
}

data class PlaygroundChartSession(
    val chartType: ChartType,
    val title: String,
    val editorState: DataEditorState,
    val appliedData: PlaygroundDataModel,
    val styleState: PlaygroundStyleState,
    val validationMessage: String?,
    val invalidRowIds: Set<Int>,
    val codegenMode: CodegenMode,
)

@Serializable
data class SnapshotPublishMetadata(
    @SerialName("source_sha")
    val sourceSha: String,
    @SerialName("charts_version")
    val chartsVersion: String,
    @SerialName("published_at")
    val publishedAt: String,
)

data class PlaygroundState(
    val selectedChartType: ChartType,
    val rightPanelTab: PlaygroundRightPanelTab,
    val sessions: Map<ChartType, PlaygroundChartSession>,
    val snapshotMetadata: SnapshotPublishMetadata? = null,
    val snapshotMetadataLoading: Boolean = true,
)

interface PlaygroundChartDefinition {
    val type: ChartType
    val displayName: String
    val defaultTitle: String

    fun defaultDataModel(): PlaygroundDataModel

    fun defaultStyleState(): PlaygroundStyleState

    fun createEditorState(model: PlaygroundDataModel): DataEditorState

    fun validate(editorState: DataEditorState): PlaygroundValidationResult

    fun newRowCells(
        rowIndex: Int,
        columns: List<DataEditorColumn>,
    ): Map<String, String>

    fun randomize(editorState: DataEditorState): DataEditorState

    fun settingsSchema(session: PlaygroundChartSession): List<SettingDescriptor>

    @Composable
    fun renderPreview(
        session: PlaygroundChartSession,
        modifier: Modifier = Modifier,
    )

    @Composable
    fun generateCode(session: PlaygroundChartSession): GeneratedSnippet

    fun resetSession(codegenMode: CodegenMode = CodegenMode.MINIMAL): PlaygroundChartSession {
        val dataModel = defaultDataModel()
        val editor = createEditorState(dataModel)
        return PlaygroundChartSession(
            chartType = type,
            title = defaultTitle,
            editorState = editor,
            appliedData = dataModel,
            styleState = defaultStyleState(),
            validationMessage = null,
            invalidRowIds = emptySet(),
            codegenMode = codegenMode,
        )
    }
}

data class PlaygroundChartRegistry(
    val charts: List<PlaygroundChartDefinition>,
    val primaryChartTypes: List<ChartType>,
    val overflowChartTypes: List<ChartType>,
) {
    private val byType: Map<ChartType, PlaygroundChartDefinition> = charts.associateBy { chart -> chart.type }

    fun definition(type: ChartType): PlaygroundChartDefinition =
        byType[type]
            ?: error("Missing chart definition for $type")
}
