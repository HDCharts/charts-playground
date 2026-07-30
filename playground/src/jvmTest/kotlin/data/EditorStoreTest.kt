package data

import domain.ChartData
import domain.ChartType
import domain.CodegenMode
import domain.ColorValue
import domain.EditorAction
import domain.LineStyleState
import domain.RightPanelTab
import domain.SettingChange
import domain.SettingDescriptor
import io.github.dautovicharis.charts.demoshared.data.barSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.histogramSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.lineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.multiLineSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.pieSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.radarSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedAreaSampleUseCase
import io.github.dautovicharis.charts.demoshared.data.stackedBarSampleUseCase
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
                .title
        val pieTitle =
            store.state.value.sessions
                .getValue(ChartType.PIE)
                .title
        assertEquals("Line Session Title", lineTitle)
        assertEquals("Pie Session Title", pieTitle)
    }

    @Test
    fun invalid_editor_value_keeps_last_known_good_applied_data() {
        val store = newStore()
        val before =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .data

        store.dispatch(
            EditorAction.UpdateDataTableCell(
                rowIndex = 0,
                columnId = "value",
                value = "oops",
            ),
        )

        val afterSession =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(before, afterSession.data)
        assertTrue(afterSession.validationMessage.orEmpty().contains("valid numeric"))
    }

    @Test
    fun setting_changes_are_applied_by_the_store_and_regenerate_code() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateSetting(SettingChange.FloatValue("lineAlpha", 0.8f)))

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(0.8f, (session.styleState as LineStyleState).lineAlpha)
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

        repeat(initial.dataTable.rows.size - initial.dataTable.minRows) {
            store.dispatch(EditorAction.DeleteRow(0))
        }
        val minimum =
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .dataTable

        store.dispatch(EditorAction.DeleteRow(0))

        assertEquals(minimum.minRows, minimum.rows.size)
        assertEquals(
            minimum,
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .dataTable,
        )
    }

    @Test
    fun randomize_keeps_the_selected_chart_valid() {
        val store = newStore()

        store.dispatch(EditorAction.Randomize)

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertTrue(session.invalidRowIds.isEmpty())
        assertTrue(session.validationMessage.orEmpty().contains("Applied"))
        assertTrue(session.generatedCode.isNotBlank())
    }

    @Test
    fun reset_restores_chart_defaults_and_preserves_codegen_mode() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateTitle("Changed"))
        store.dispatch(EditorAction.UpdateCodegenMode(CodegenMode.FULL))
        store.dispatch(EditorAction.UpdateSetting(SettingChange.FloatValue("lineAlpha", 0.2f)))
        store.dispatch(EditorAction.Reset)

        val session =
            store.state.value.sessions
                .getValue(ChartType.LINE)
        assertEquals(domain.LINE_CHART_TITLE, session.title)
        assertEquals(CodegenMode.FULL, session.codegenMode)
        assertEquals(null, (session.styleState as LineStyleState).lineAlpha)
        assertTrue(session.generatedCode.isNotBlank())
    }

    @Test
    fun pie_row_count_updates_after_add_row_and_schema_item_count_tracks_it() {
        val store = newStore()
        store.dispatch(EditorAction.SelectChart(ChartType.PIE))

        val beforeSession =
            store.state.value.sessions
                .getValue(ChartType.PIE)
        val beforeRows = beforeSession.dataTable.rows
        val beforeData = beforeSession.data as ChartData.SingleSeries
        val beforeCount = beforeData.values.size

        store.dispatch(EditorAction.AddRow)

        val afterSession =
            store.state.value.sessions
                .getValue(ChartType.PIE)
        val afterRows = afterSession.dataTable.rows
        val afterData = afterSession.data as ChartData.SingleSeries
        assertEquals(beforeCount + 1, afterData.values.size)
        assertEquals(afterRows.maxOf { row -> row.id }, afterRows.last().id)
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
    fun codegen_mode_is_persisted_per_chart_session() {
        val store = newStore()

        store.dispatch(EditorAction.UpdateCodegenMode(CodegenMode.FULL))
        store.dispatch(EditorAction.SelectChart(ChartType.PIE))
        store.dispatch(EditorAction.UpdateCodegenMode(CodegenMode.MINIMAL))
        store.dispatch(EditorAction.SelectChart(ChartType.LINE))

        assertEquals(
            CodegenMode.FULL,
            store.state.value.sessions
                .getValue(ChartType.LINE)
                .codegenMode,
        )
        assertEquals(
            CodegenMode.MINIMAL,
            store.state.value.sessions
                .getValue(ChartType.PIE)
                .codegenMode,
        )
    }

    @Test
    fun default_sessions_use_charts_sample_use_cases() {
        val state = newStore().state.value

        val pieData = state.sessions.getValue(ChartType.PIE).data as ChartData.SingleSeries
        val pieSample = pieSampleUseCase().initialPieSample()
        assertEquals(
            pieSample.dataSet.data.item.points
                .map(Double::toFloat),
            pieData.values,
        )
        assertEquals(pieSample.segmentKeys, pieData.labels)

        val lineData = state.sessions.getValue(ChartType.LINE).data as ChartData.SingleSeries
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

        val barData = state.sessions.getValue(ChartType.BAR).data as ChartData.SingleSeries
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

        val histogramData = state.sessions.getValue(ChartType.HISTOGRAM).data as ChartData.SingleSeries
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

        val multiLineData = state.sessions.getValue(ChartType.MULTI_LINE).data as ChartData.MultiSeries
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

        val areaData = state.sessions.getValue(ChartType.AREA).data as ChartData.MultiSeries
        val areaDataSet = stackedAreaSampleUseCase().initialStackedAreaSample().dataSet
        assertEquals(areaDataSet.data.categories.toList(), areaData.xLabels)
        assertEquals(areaDataSet.data.items.map { item -> item.label }, areaData.series.map { series -> series.name })
        assertEquals(
            areaDataSet.data.items.map { item -> item.item.points.map(Double::toFloat) },
            areaData.series.map { series -> series.values },
        )

        val stackedBarData =
            state.sessions
                .getValue(
                    ChartType.STACKED_BAR,
                ).data as ChartData.StackedSeries
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

        val radarData = state.sessions.getValue(ChartType.RADAR).data as ChartData.RadarSeries
        val radarDataSet = radarSampleUseCase().initialRadarSample().customDataSet
        assertEquals(radarDataSet.data.categories.toList(), radarData.axes)
        assertEquals(radarDataSet.data.items.map { item -> item.label }, radarData.entries.map { entry -> entry.name })
        assertEquals(
            radarDataSet.data.items.map { item -> item.item.points.map(Double::toFloat) },
            radarData.entries.map { entry -> entry.values },
        )
    }
}
