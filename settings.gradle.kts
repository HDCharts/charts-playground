pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "charts-playground"

include(":playground")

// Required local development wiring:
// charts and charts-demo-shared are always resolved from a local charts checkout.
val localChartsPath = System.getProperty("chartsLocalPath") ?: "../charts"
val localChartsDir = file(localChartsPath)
check(localChartsDir.resolve("settings.gradle.kts").exists()) {
    "charts repo not found at '$localChartsPath'. Provide -DchartsLocalPath=<path-to-charts>."
}
check(localChartsDir.resolve("charts").exists()) {
    "charts module is missing in '$localChartsPath'."
}
check(localChartsDir.resolve("charts-demo-shared").exists()) {
    "charts-demo-shared module is missing in '$localChartsPath'."
}
val chartsSettingsFile = localChartsDir.resolve("settings.gradle.kts")
val chartsSettingsContent = chartsSettingsFile.readText()
check(
    !chartsSettingsContent.contains("include(\":playground\")") ||
        localChartsDir.resolve("playground").exists(),
) {
    "charts checkout at '$localChartsPath' still includes :playground, but that directory is missing. " +
        "Use a charts branch where split is complete or point chartsLocalPath to a compatible checkout."
}

includeBuild(localChartsDir) {
    dependencySubstitution {
        substitute(module("io.github.dautovicharis:charts"))
            .using(project(":charts"))
        substitute(module("io.github.dautovicharis:charts-demo-shared"))
            .using(project(":charts-demo-shared"))
    }
}
