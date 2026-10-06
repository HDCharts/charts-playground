package data.charts

import data.ChartCodegenService
import data.chartCatalog
import domain.ChartType
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratedArtifactGoldenTest {
    @Test
    fun every_chart_type_has_a_stable_generated_source_golden() {
        val service = ChartCodegenService()
        val actualHashes =
            chartCatalog.charts.associate { definition ->
                definition.type to sha256(service.generateArtifact(definition.resetSession().validatedSpec).source)
            }

        assertEquals(
            mapOf(
                ChartType.LINE to "815214000c6e7c988ce4131e2373caa5b18d2f8f4fb7ab0931207db433907949",
                ChartType.BAR to "6dd7e1e1eab350c6aaa61800e8b06f06aca6dfc4b7bab97e0a3453c8605bdbb8",
                ChartType.HISTOGRAM to "aa454ae726d4573da76771b00bebcd3d5bcb4ee0361a57d843731a0a914c9f3b",
                ChartType.PIE to "5ed87075df40a8d44085b20528a07c257e1ea6023807316ff06d971961090cac",
                ChartType.RADAR to "8c7c77fa0c39808bc3911867b86c0fbbd381fc10754381ca2c3191cad544083f",
                ChartType.AREA to "7d50e23d02c9928f23274c44850b54c8ee54f759290adbce1c77c193596fce10",
                ChartType.MULTI_LINE to "a45845c97d5f5876236b8ed89cb5c71f65eaf53a6b5218150d78f2324fab48a3",
                ChartType.STACKED_BAR to "a0659ba1d8b04fd8a65ca20e5c6377d2ace85aed969c5778df3e3c165a810fd6",
            ),
            actualHashes,
        )
    }
}

private fun sha256(value: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(value.encodeToByteArray())
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
