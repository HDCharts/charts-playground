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
                ChartType.LINE to "06779321159827908b7792e0c365b5f55947c625bd33e696712d63b3ef9bb8a9",
                ChartType.BAR to "a243e5aed63f455b8694efa70018c07a0fe2f8d84cc76b34a50bca45da04de7c",
                ChartType.HISTOGRAM to "78706f8ab1d94e0085c07506f264ae594d477bc3c8b6ee67a090ae6ee2905269",
                ChartType.PIE to "f0c4a421264d1451322aec270ca800876276b90d4bbd3795acad4dced0f76f51",
                ChartType.RADAR to "86ee4d104c17b2ed32d66d80da3b93fa434e221f72cb7daa7d79cea27664ca6a",
                ChartType.AREA to "5961dc1c6a38acbd2a968d77122e96174238a2d4518205c3a50ad7b75771eec0",
                ChartType.MULTI_LINE to "ecfc58bac7ece56000b0ff1d5d6c02fbe610442e17616d3836ef152e283498fe",
                ChartType.STACKED_BAR to "328eee550c2fd12fb289c694df899b187f0725167326dd6acd37c1093bf1b299",
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
