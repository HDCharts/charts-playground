package testing

import androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.assertEquals

/** Compiles a generated snippet against the test classpath (charts library + Compose). */
fun assertSnippetCompiles(
    snippet: String,
    name: String,
) {
    val tempDir = createTempDirectory("generated-snippet-$name").toFile()
    val sourceFile = File(tempDir, "GeneratedSnippet_$name.kt")
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
        "Generated snippet '$name' failed to compile:\n$snippet\n\nCompiler output:\n$compilerOutput",
    )
}

private fun composeCompilerPluginJar(): File {
    val location = ComposePluginRegistrar::class.java.protectionDomain.codeSource.location
    return File(location.toURI())
}
