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
                ChartType.LINE to "620cc393ae67bd5e22d90110d740b7f7b9102d8ae02004d24b17513b2530cb8a",
                ChartType.BAR to "4f9126d74ea6696436a19c83d25beb03c4249403dc3ae409aa601b96ec05f0ee",
                ChartType.HISTOGRAM to "aa454ae726d4573da76771b00bebcd3d5bcb4ee0361a57d843731a0a914c9f3b",
                ChartType.PIE to "d093753fc43e7965b6668660ae7c49cfb83714eddfd1c8b9ce55c4015b520ff2",
                ChartType.RADAR to "70f2a8e4d1b14eaa5fe78921c56bbee7b46a3e7f37c7e211d78a5000b8c788e0",
                ChartType.AREA to "7d50e23d02c9928f23274c44850b54c8ee54f759290adbce1c77c193596fce10",
                ChartType.MULTI_LINE to "a45845c97d5f5876236b8ed89cb5c71f65eaf53a6b5218150d78f2324fab48a3",
                ChartType.STACKED_BAR to "a457583166f1dc7e79e7e04ecabb6a5cc7bc6670b8a3d878005de00696cfa095",
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
