package codegen

import codegen.line.LineChartCodeGenerator
import codegen.multiline.MultiLineChartCodeGenerator
import kotlin.test.Test
import kotlin.test.assertTrue

class CodegenEdgeCasesTest {
    @Test
    fun empty_collections_produce_deterministic_source_fragments() {
        val singleSeriesCode = LineChartCodeGenerator().generate(LineCodegenConfig(points = emptyList())).code
        val multiSeriesCode =
            MultiLineChartCodeGenerator()
                .generate(
                    MultiLineCodegenConfig(
                        series = emptyList(),
                        categories = emptyList(),
                    ),
                ).code

        assertTrue("listOf()" in singleSeriesCode)
        assertTrue("val items = listOf(" in multiSeriesCode)
        assertTrue(singleSeriesCode.endsWith("\n"))
        assertTrue(multiSeriesCode.endsWith("\n"))
    }
}
