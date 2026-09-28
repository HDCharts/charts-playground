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
                ChartType.LINE to "964bf7fa80975e1fbb9717bc3206c4b5dbcbc39474a16b55e05ed83f9f186c63",
                ChartType.BAR to "a5a5b9e5cbf9c3da1a6c324a92dd6e03f66dddbbc54e80512d59ee8fd06cf4a8",
                ChartType.HISTOGRAM to "6150eec18dc243fbbb35ebcde46926c9c837eeea2a573010c6700faf3a2ba5a4",
                ChartType.PIE to "089e79654bc4a4e0fa386f41948a97bac60dc5799309ed40bf75ecaf368bb728",
                ChartType.RADAR to "fb9fdd8db1532330763a84b82e45593bedc800616f37b01cecc0399619a6deb4",
                ChartType.AREA to "3277ee670df968dd76f4129054ed82c22d4a29833d0fe85573f0167f0b6320d6",
                ChartType.MULTI_LINE to "d418be3bc9025e4dc38db98d978a8aa4df056ba349b8d55b6254744f45746a56",
                ChartType.STACKED_BAR to "a247769d85b738cff28e6822a32bbfb11b658c07718835fdcb3423886d5dc0fb",
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
