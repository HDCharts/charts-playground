plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.build.config)
    alias(libs.plugins.compose.compiler)
}

val chartsDisplayVersion =
    (project.findProperty("chartsDisplayVersion") as String?)
        ?.takeIf { it.isNotBlank() }
        ?: "dev-local"
val localChartsModuleVersion = "dev-local"
val localChartsDependency = "io.github.dautovicharis:charts:$localChartsModuleVersion"
val localChartsSampleSharedDependency = "io.github.dautovicharis:sample-shared:$localChartsModuleVersion"

kotlin {
    jvmToolchain(
        libs.versions.java
            .get()
            .toInt(),
    )

    jvm()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "Playground.js"
            }
            binaries.executable()
        }
    }

    sourceSets {
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
                // These coordinates are always substituted to local projects via includeBuild in settings.gradle.kts.
                implementation(localChartsDependency)
                implementation(localChartsSampleSharedDependency)
                implementation(libs.compose.mpp.runtime)
                implementation(libs.compose.mpp.ui)
                implementation(
                    "org.jetbrains.kotlin:kotlin-compiler-embeddable:${libs.versions.kotlin.multiplatform.get()}",
                )
                implementation(libs.kotlin.compose.compiler.plugin.embeddable)
            }
        }

        commonMain.dependencies {
            implementation(libs.compose.mpp.runtime)
            implementation(libs.compose.mpp.foundation)
            implementation(libs.compose.mpp.material3)
            implementation(libs.compose.mpp.material.icons.extended)
            implementation(libs.compose.mpp.ui)
            implementation(libs.compose.mpp.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.datetime)
            // These coordinates are always substituted to local projects via includeBuild in settings.gradle.kts.
            implementation(localChartsDependency)
            implementation(localChartsSampleSharedDependency)
        }
    }
}

buildConfig {
    packageName("config")
    buildConfigField("CHARTS_VERSION", chartsDisplayVersion)
    buildConfigField(
        "SNAPSHOT_METADATA_CHARTS_SHA",
        (project.findProperty("snapshotMetadataChartsSha") as String?).orEmpty(),
    )
    buildConfigField(
        "SNAPSHOT_METADATA_PLAYGROUND_SHA",
        (project.findProperty("snapshotMetadataPlaygroundSha") as String?).orEmpty(),
    )
    buildConfigField(
        "SNAPSHOT_METADATA_PUBLISHED_AT",
        (project.findProperty("snapshotMetadataPublishedAt") as String?).orEmpty(),
    )
    useKotlinOutput()
}

compose.resources {
    packageOfResClass = "chartsproject.playground.generated.resources"
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    val generatedBuildPath =
        layout.buildDirectory
            .get()
            .asFile
            .path + "/"
    filter {
        exclude("**/build/**")
        exclude { element ->
            element.file.path.startsWith(generatedBuildPath)
        }
    }
}
