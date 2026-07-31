package architecture

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

class ArchitectureBoundaryTest {
    @Test
    fun lower_level_packages_do_not_import_higher_level_packages() {
        val rules =
            listOf(
                BoundaryRule(
                    name = "domain",
                    relativePath = "domain",
                    forbiddenImports =
                        setOf(
                            "data.",
                            "codegen.",
                            "presentation.",
                            "androidx.compose.",
                            "io.github.dautovicharis.charts.",
                        ),
                ),
                BoundaryRule(
                    name = "codegen",
                    relativePath = "codegen",
                    forbiddenImports =
                        setOf(
                            "data.",
                            "presentation.",
                            "androidx.compose.",
                            "io.github.dautovicharis.charts.",
                        ),
                ),
                BoundaryRule(
                    name = "data",
                    relativePath = "data",
                    forbiddenImports = setOf("presentation.", "androidx.compose."),
                ),
                BoundaryRule(
                    name = "common presentation",
                    relativePath = "presentation",
                    forbiddenImports = setOf("data.", "codegen."),
                ),
            )

        val violations = rules.flatMap { rule -> rule.findViolations(sourceRoot()) }

        assertTrue(
            violations.isEmpty(),
            "Architecture boundary violations:\n${violations.joinToString("\n")}",
        )
    }

    @Test
    fun preview_renderers_require_validated_specs() {
        val rendererRoot = sourceRoot().resolve("presentation/chart/renderers")
        val rendererFiles = kotlinFiles(rendererRoot)
        val missingContract =
            rendererFiles.filterNot { file ->
                Files.readString(file).contains("ValidatedChartSpec")
            }

        assertTrue(
            missingContract.isEmpty(),
            "Preview renderers must consume ValidatedChartSpec: ${missingContract.joinToString()}",
        )
    }

    private fun sourceRoot(): Path =
        sequenceOf(
            Path.of("playground/src/commonMain/kotlin"),
            Path.of("src/commonMain/kotlin"),
        ).firstOrNull(Files::isDirectory)
            ?: error("Could not locate commonMain Kotlin source root")
}

private data class BoundaryRule(
    val name: String,
    val relativePath: String,
    val forbiddenImports: Set<String>,
) {
    fun findViolations(root: Path): List<String> {
        val packageRoot = root.resolve(relativePath)
        return kotlinFiles(packageRoot).flatMap { file ->
            Files.readAllLines(file).flatMap { line ->
                if (
                    line.startsWith("import ") &&
                    forbiddenImports.any { prefix ->
                        line.removePrefix("import ").startsWith(prefix)
                    }
                ) {
                    listOf("$name: $file: $line")
                } else {
                    emptyList()
                }
            }
        }
    }
}

private fun kotlinFiles(root: Path): List<Path> {
    if (!Files.isDirectory(root)) return emptyList()
    return Files.walk(root).use { paths ->
        paths.filter { path -> Files.isRegularFile(path) && path.toString().endsWith(".kt") }.toList()
    }
}
