package codegen

const val CODEGEN_GENERATOR_VERSION = "charts-playground-codegen-v1"

data class GeneratedArtifact(
    val source: String,
    val target: String = "kotlin-compose",
    val generatorVersion: String = CODEGEN_GENERATOR_VERSION,
    val warnings: List<String> = emptyList(),
)
