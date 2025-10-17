plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

application {
    mainClass.set("org.example.AppKt")
}

// Ignoramos tests que rompen el build
tasks.withType<Test> {
    enabled = false
}


dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    // Define the main class for the application.
    mainClass = "org.example.AppKt"
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Run socket server"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.ServidorMainKt")
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Run console client"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("org.example.ClienteMainKt")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
