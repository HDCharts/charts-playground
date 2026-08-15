package codegen

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import codegen.AreaCodegenConfig
import codegen.BarCodegenConfig
import codegen.HistogramCodegenConfig
import codegen.LineCodegenConfig
import codegen.LinePointInput
import codegen.MultiLineCodegenConfig
import codegen.MultiSeriesCodegenInput
import codegen.PieCodegenConfig
import codegen.PieSliceInput
import codegen.RadarCodegenConfig
import codegen.StackedBarCodegenConfig
import codegen.StylePropertiesSnapshot
import codegen.area.AreaChartCodeGenerator
import codegen.bar.BarChartCodeGenerator
import codegen.histogram.HistogramChartCodeGenerator
import codegen.line.LineChartCodeGenerator
import codegen.multiline.MultiLineChartCodeGenerator
import codegen.pie.PieChartCodeGenerator
import codegen.radar.RadarChartCodeGenerator
import codegen.stackedbar.StackedBarChartCodeGenerator
import codegen.styleProperty
import domain.ColorValue
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class GeneratedSnippetCompilationTest {
    @Test
    fun generated_snippets_compile_for_all_chart_generators() {
        val snippets =
            listOf(
                LineChartCodeGenerator()
                    .generate(
                        LineCodegenConfig(
                            points =
                                listOf(
                                    LinePointInput(label = "Jan", value = 12f),
                                    LinePointInput(label = "Feb", value = 18f),
                                ),
                        ),
                    ).code,
                PieChartCodeGenerator()
                    .generate(
                        PieCodegenConfig(
                            rows =
                                listOf(
                                    PieSliceInput(label = "A", value = 24f),
                                    PieSliceInput(label = "B", value = 18f),
                                ),
                        ),
                    ).code,
                PieChartCodeGenerator()
                    .generate(
                        PieCodegenConfig(
                            rows =
                                listOf(
                                    PieSliceInput(label = "A", value = 24f, color = ColorValue(0xFF1D3557L)),
                                    PieSliceInput(label = "B", value = 18f, color = ColorValue(0xFF457B9DL)),
                                    PieSliceInput(label = "C", value = 12f, color = ColorValue(0xFFA8DADCL)),
                                ),
                            styleProperties =
                                StylePropertiesSnapshot(
                                    current =
                                        listOf(
                                            styleProperty("donutPercentage", 30f),
                                            styleProperty("pieAlpha", 0.6f),
                                            styleProperty("borderWidth", 5f),
                                            styleProperty("legendVisible", false),
                                        ),
                                    defaults =
                                        listOf(
                                            styleProperty("donutPercentage", 0f),
                                            styleProperty("pieAlpha", 0.4f),
                                            styleProperty("borderWidth", 3f),
                                            styleProperty("legendVisible", true),
                                        ),
                                ),
                        ),
                    ).code,
                BarChartCodeGenerator()
                    .generate(
                        BarCodegenConfig(
                            points =
                                listOf(
                                    PieSliceInput(label = "Mon", value = 12f),
                                    PieSliceInput(label = "Tue", value = 18f),
                                ),
                            styleProperties =
                                StylePropertiesSnapshot(
                                    current =
                                        listOf(
                                            styleProperty(
                                                "barColors",
                                                listOf(ColorValue(0xFFFF0000L), ColorValue(0xFF00FF00L)),
                                            ),
                                        ),
                                    defaults =
                                        listOf(
                                            styleProperty("barColors", emptyList()),
                                        ),
                                ),
                        ),
                    ).code,
                HistogramChartCodeGenerator()
                    .generate(
                        HistogramCodegenConfig(
                            points =
                                listOf(
                                    PieSliceInput(label = "0-10", value = 4f),
                                    PieSliceInput(label = "10-20", value = 9f),
                                ),
                        ),
                    ).code,
                MultiLineChartCodeGenerator()
                    .generate(
                        MultiLineCodegenConfig(
                            series =
                                listOf(
                                    MultiSeriesCodegenInput("Web", listOf(120f, 140f, 150f)),
                                    MultiSeriesCodegenInput("Mobile", listOf(80f, 90f, 95f)),
                                ),
                            categories = listOf("W1", "W2", "W3"),
                        ),
                    ).code,
                StackedBarChartCodeGenerator()
                    .generate(
                        StackedBarCodegenConfig(
                            series =
                                listOf(
                                    MultiSeriesCodegenInput("A", listOf(10f, 20f)),
                                    MultiSeriesCodegenInput("B", listOf(5f, 15f)),
                                ),
                            categories = listOf("Q1", "Q2"),
                        ),
                    ).code,
                AreaChartCodeGenerator()
                    .generate(
                        AreaCodegenConfig(
                            series =
                                listOf(
                                    MultiSeriesCodegenInput("Plan", listOf(60f, 80f)),
                                    MultiSeriesCodegenInput("Actual", listOf(55f, 70f)),
                                ),
                            categories = listOf("Jan", "Feb"),
                        ),
                    ).code,
                RadarChartCodeGenerator()
                    .generate(
                        RadarCodegenConfig(
                            series =
                                listOf(
                                    MultiSeriesCodegenInput("Android", listOf(80f, 75f, 70f)),
                                    MultiSeriesCodegenInput("iOS", listOf(78f, 74f, 72f)),
                                ),
                            categories = listOf("Perf", "UX", "Security"),
                        ),
                    ).code,
            )

        snippets.forEachIndexed { index, snippet ->
            assertSnippetCompiles(snippet, index)
        }
    }

    private fun assertSnippetCompiles(
        snippet: String,
        index: Int,
    ) {
        val tempDir = createTempDirectory("generated-snippet-$index").toFile()
        val sourceFile = File(tempDir, "GeneratedSnippet$index.kt")
        sourceFile.writeText(snippet)
        val outputDir = File(tempDir, "classes").apply { mkdirs() }

        val compilerOutput = ByteArrayOutputStream()
        val exitCode =
            K2JVMCompiler().exec(
                PrintStream(compilerOutput),
                "-jvm-target",
                "17",
                "-Xplugin=${composeCompilerPluginJar().absolutePath}",
                "-classpath",
                System.getProperty("java.class.path"),
                "-d",
                outputDir.absolutePath,
                sourceFile.absolutePath,
            )

        assertEquals(
            ExitCode.OK,
            exitCode,
            "Generated snippet failed to compile:\n$snippet\n\nCompiler output:\n$compilerOutput",
        )
    }

    private fun composeCompilerPluginJar(): File {
        val location = ComposePluginRegistrar::class.java.protectionDomain.codeSource.location
        return File(location.toURI())
    }
}
