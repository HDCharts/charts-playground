package data

import dev.hdcode.charts.sampleshared.data.barSampleUseCase
import dev.hdcode.charts.sampleshared.data.histogramSampleUseCase
import dev.hdcode.charts.sampleshared.data.lineSampleUseCase
import dev.hdcode.charts.sampleshared.data.multiLineSampleUseCase
import dev.hdcode.charts.sampleshared.data.pieSampleUseCase
import dev.hdcode.charts.sampleshared.data.radarSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedAreaSampleUseCase
import dev.hdcode.charts.sampleshared.data.stackedBarSampleUseCase
import domain.ChartData
import domain.ChartType
import domain.ColorValue
import domain.EditorAction
import domain.LineStyleState
import domain.RightPanelTab
import domain.RowId
import domain.SettingChange
import domain.SettingDescriptor
import domain.ValidationIssueCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EditorStoreTest {
    private fun newStore() = InMemoryEditorStore(chartCatalog, snapshotMetadata = null)

    @Test
    fun switching_chart_types_preserves_session_state() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateTitle("Line Session Title"))
        store.dispatch(EditorAction.SelectChart(ChartType.PIE))
        store.dispatch(EditorAction.UpdateTitle("Pie Session Title"))
        store.dispatch(EditorAction.SelectChart(ChartType.LINE))

        val lineTitle =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .draft.title
        val pieTitle =
            store.state.value.sessions
                .getValue(ChartType.PIE)
                .draft.title
        assertEquals("Line Session Title", lineTitle)
        assertEquals("Pie Session Title", pieTitle)
    }

    @Test
    fun invalid_editor_value_keeps_last_known_good_applied_data() {
        val store = newStore()
        val before =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .validatedSpec.data
        val beforeCode =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .generatedCode

        store.dispatch(
            EditorAction.UpdateDataTableCell(
                rowId = RowId(1),
                columnId = "value",
                value = "oops",
            ),
        )

        val afterSession =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(before, afterSession.validatedSpec.data)
        assertEquals(
            "oops",
            afterSession.draft.dataTable.rows
                .first()
                .cells
                .getValue("value"),
        )
        assertTrue(
            afterSession.validation.issues.any { issue ->
                issue.code == ValidationIssueCode.INVALID_NUMBER && issue.path?.rowId == RowId(1)
            },
        )
        assertTrue(afterSession.validation is domain.ChartValidationState.Invalid)
        assertEquals(beforeCode, afterSession.generatedCode)
    }

    @Test
    fun row_actions_use_stable_ids_after_another_row_is_deleted() {
        val store = newStore()

        store.dispatch(EditorAction.DeleteRow(RowId(1)))
        store.dispatch(
            EditorAction.UpdateDataTableCell(
                rowId = RowId(2),
                columnId = "value",
                value = "77",
            ),
        )

        val table =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .draft.dataTable
        assertEquals(RowId(2), table.rows.first().id)
        assertEquals(
            "77",
            table.rows
                .first()
                .cells
                .getValue("value"),
        )
    }

    @Test
    fun setting_changes_are_applied_by_the_store_and_regenerate_code() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateSetting(SettingChange.FloatValue("lineAlpha", 0.8f)))

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(0.8f, (session.draft.styleState as LineStyleState).lineAlpha)
        assertTrue(session.generatedCode.contains("lineAlpha = 0.8f"))
    }

    @Test
    fun store_applies_each_setting_descriptor_type() {
        val store = newStore()

        chartCatalog.charts.forEach { definition ->
            store.dispatch(EditorAction.SelectChart(definition.type))
            val session =
                store.state.value.sessions
                    .getValue(definition.type)

            session.settings.forEach { descriptor ->
                when (descriptor) {
                    is SettingDescriptor.Section,
                    SettingDescriptor.Divider,
                    -> Unit
                    is SettingDescriptor.Toggle ->
                        store.dispatch(
                            EditorAction.UpdateSetting(
                                SettingChange.BooleanValue(descriptor.id, !descriptor.defaultValue),
                            ),
                        )
                    is SettingDescriptor.Slider ->
                        store.dispatch(
                            EditorAction.UpdateSetting(SettingChange.FloatValue(descriptor.id, descriptor.min)),
                        )
                    is SettingDescriptor.Dropdown ->
                        store.dispatch(
                            EditorAction.UpdateSetting(
                                SettingChange.TextValue(descriptor.id, descriptor.options.first().value),
                            ),
                        )
                    is SettingDescriptor.Color ->
                        store.dispatch(
                            EditorAction.UpdateSetting(
                                SettingChange.ColorValue(descriptor.id, ColorValue(0xFFFF0000L)),
                            ),
                        )
                    is SettingDescriptor.ColorPalette ->
                        store.dispatch(
                            EditorAction.UpdateSetting(
                                SettingChange.ColorListValue(
                                    descriptor.id,
                                    List(descriptor.itemCount(session)) { ColorValue(0xFFFF0000L) },
                                ),
                            ),
                        )
                }
            }

            assertTrue(
                store.state.value.sessions
                    .getValue(definition.type)
                    .generatedCode
                    .isNotBlank(),
            )
        }
    }

    @Test
    fun right_panel_tab_is_updated_by_the_store() {
        val store = newStore()

        store.dispatch(EditorAction.SelectRightPanelTab(RightPanelTab.CODE))

        assertEquals(RightPanelTab.CODE, store.state.value.rightPanelTab)
    }

    @Test
    fun delete_row_stops_at_the_chart_minimum() {
        val store = newStore()
        val initial =
            store.state.value.sessions
                .getValue(ChartType.LINE)

        repeat(initial.draft.dataTable.rows.size - initial.draft.dataTable.minRows) {
            val rowId =
                store.state.value.sessions
                    .getValue(ChartType.LINE)
                    .draft.dataTable.rows
                    .first()
                    .id
            store.dispatch(EditorAction.DeleteRow(rowId))
        }
        val minimum =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .draft.dataTable

        store.dispatch(EditorAction.DeleteRow(minimum.rows.first().id))

        assertEquals(minimum.minRows, minimum.rows.size)
        assertEquals(
            minimum,
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .draft.dataTable,
        )
    }

    @Test
    fun randomize_keeps_the_selected_chart_valid() {
        val store = newStore()

        store.dispatch(EditorAction.Randomize)

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertTrue(session.validation.invalidRowIds.isEmpty())
        assertEquals(
            session.draft.dataTable.rows.size,
            (session.validation as domain.ChartValidationState.Valid).appliedRowCount,
        )
        assertTrue(session.generatedCode.isNotBlank())
    }

    @Test
    fun reset_restores_chart_defaults() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateTitle("Changed"))
        store.dispatch(EditorAction.UpdateSetting(SettingChange.FloatValue("lineAlpha", 0.2f)))
        store.dispatch(EditorAction.Reset)

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(domain.LINE_CHART_TITLE, session.draft.title)
        assertEquals(null, (session.draft.styleState as LineStyleState).lineAlpha)
        assertTrue(session.generatedCode.isNotBlank())
    }

    @Test
    fun pie_row_count_updates_after_add_row_and_schema_item_count_tracks_it() {
        val store = newStore()
        store.dispatch(EditorAction.SelectChart(ChartType.PIE))

        val beforeSession =
            store.state.value.sessions
                .getValue(ChartType.PIE)
        val beforeRows = beforeSession.draft.dataTable.rows
        val beforeData = beforeSession.validatedSpec.data as ChartData.SingleSeries
        val beforeCount = beforeData.values.size

        store.dispatch(EditorAction.AddRow)

        val afterSession =
            store.state.value.sessions
                .getValue(ChartType.PIE)
        val afterRows = afterSession.draft.dataTable.rows
        val afterData = afterSession.validatedSpec.data as ChartData.SingleSeries
        assertEquals(beforeCount + 1, afterData.values.size)
        assertEquals(RowId(afterRows.maxOf { row -> row.id.value }), afterRows.last().id)
        assertEquals(beforeRows.map { row -> row.id }, afterRows.dropLast(1).map { row -> row.id })
        assertEquals(afterRows.last().cells.getValue("label"), afterData.labels?.last())

        val descriptor =
            chartCatalog
                .definition(ChartType.PIE)
                .settingsSchema(afterSession)
                .filterIsInstance<SettingDescriptor.ColorPalette>()
                .firstOrNull()
        assertNotNull(descriptor)
        assertEquals(afterData.values.size, descriptor.itemCount(afterSession))
    }

    @Test
    fun default_sessions_use_charts_sample_use_cases() {
        val state = newStore().state.value

        val pieData =
            state.sessions
                .getValue(ChartType.PIE)
                .validatedSpec.data as ChartData.SingleSeries
        val pieSample = pieSampleUseCase().initialPieSample()
        assertEquals(
            pieSample.slices.map { it.value },
            pieData.values,
        )
        assertEquals(pieSample.slices.map { it.label }, pieData.labels)

        val lineData =
            state.sessions
                .getValue(ChartType.LINE)
                .validatedSpec.data as ChartData.SingleSeries
        val lineDataSet = lineSampleUseCase().initialLineDataSet()
        assertEquals(
            lineDataSet.data.item.points
                .map(Double::toFloat),
            lineData.values,
        )
        assertEquals(
            lineDataSet.data.item.labels
                .toList(),
            lineData.labels,
        )

        val barData =
            state.sessions
                .getValue(ChartType.BAR)
                .validatedSpec.data as ChartData.SingleSeries
        val barDataSet = barSampleUseCase().initialBarDataSet()
        assertEquals(
            barDataSet.data.item.points
                .map(Double::toFloat),
            barData.values,
        )
        assertEquals(
            barDataSet.data.item.labels
                .toList(),
            barData.labels,
        )

        val histogramData =
            state.sessions
                .getValue(ChartType.HISTOGRAM)
                .validatedSpec.data as ChartData.SingleSeries
        val histogramDataSet = histogramSampleUseCase().initialHistogramDataSet()
        assertEquals(
            histogramDataSet.data.item.points
                .map(Double::toFloat),
            histogramData.values,
        )
        assertEquals(
            histogramDataSet.data.item.labels
                .toList(),
            histogramData.labels,
        )

        val multiLineData =
            state.sessions
                .getValue(ChartType.MULTI_LINE)
                .validatedSpec.data as ChartData.MultiSeries
        val multiLineDataSet = multiLineSampleUseCase().initialMultiLineSample().dataSet
        assertEquals(multiLineDataSet.data.categories.toList(), multiLineData.xLabels)
        assertEquals(
            multiLineDataSet.data.items.map { item ->
                item.label
            },
            multiLineData.series.map { series -> series.name },
        )
        assertEquals(
            multiLineDataSet.data.items.map { item -> item.item.points.map(Double::toFloat) },
            multiLineData.series.map { series -> series.values },
        )

        val areaData =
            state.sessions
                .getValue(ChartType.AREA)
                .validatedSpec.data as ChartData.MultiSeries
        val areaDataSet = stackedAreaSampleUseCase().initialStackedAreaSample().dataSet
        assertEquals(areaDataSet.data.categories.toList(), areaData.xLabels)
        assertEquals(
            areaDataSet.data.items.map { item -> item.label },
            areaData.series.map { series -> series.name },
        )
        assertEquals(
            areaDataSet.data.items.map { item -> item.item.points.map(Double::toFloat) },
            areaData.series.map { series -> series.values },
        )

        val stackedBarData =
            state.sessions
                .getValue(
                    ChartType.STACKED_BAR,
                ).validatedSpec.data as ChartData.StackedSeries
        val stackedBarDataSet = stackedBarSampleUseCase().initialStackedBarSample().dataSet
        assertEquals(stackedBarDataSet.data.items.map { item -> item.label }, stackedBarData.segmentNames)
        assertEquals(stackedBarDataSet.data.categories.toList(), stackedBarData.bars.map { bar -> bar.label })
        assertEquals(
            stackedBarDataSet.data.categories.indices.map { categoryIndex ->
                stackedBarDataSet.data.items.map { item ->
                    item.item.points[categoryIndex].toFloat()
                }
            },
            stackedBarData.bars.map { bar -> bar.values },
        )

        val radarData =
            state.sessions
                .getValue(ChartType.RADAR)
                .validatedSpec.data as ChartData.RadarSeries
        val radarDataSet = radarSampleUseCase().initialRadarSample().customDataSet
        assertEquals(radarDataSet.data.categories.toList(), radarData.axes)
        assertEquals(
            radarDataSet.data.items.map { item -> item.label },
            radarData.entries.map { entry -> entry.name },
        )
        assertEquals(
            radarDataSet.data.items.map { item -> item.item.points.map(Double::toFloat) },
            radarData.entries.map { entry -> entry.values },
        )
    }
}
