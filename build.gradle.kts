plugins {
    kotlin("jvm") version libs.versions.kotlin
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.stdlib)

    testImplementation(libs.test.assertk)
    testImplementation(libs.test.junitJupiter)
    testImplementation(libs.test.kotlin.coroutines)
    testImplementation(libs.test.turbine)

    testRuntimeOnly(libs.test.junitJupiter.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}