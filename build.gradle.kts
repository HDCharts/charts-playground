plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
}

group = "io.github.dautovicharis"
version = providers.gradleProperty("playgroundVersion").get()

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")
    }

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
            ignoreFailures.set(false)
            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }
    }
}

tasks.register("playgroundTest") {
    group = "verification"
    description = "Relevant tests for the playground project"
    dependsOn(":playground:jvmTest")
    dependsOn(":playground:jsTest")
}

tasks.register("ciCompile") {
    group = "verification"
    description = "CI-focused compile task set without packaging"
    dependsOn(":playground:compileKotlinJvm")
    dependsOn(":playground:compileKotlinJs")
}

tasks.register("ciAssemble") {
    group = "verification"
    description = "CI-focused assemble task set using dev/debug outputs"
    dependsOn(":playground:jvmJar")
    dependsOn(":playground:jsBrowserDevelopmentExecutableDistribution")
}

tasks.register("buildSrcKtlintCheck") {
    group = "verification"
    description = "No-op: this repository has no buildSrc module."
}
