//import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
//import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
//    kotlin("kapt") version "1.9.23" apply false
//    id("org.springframework.boot") version "3.2.4" apply false
    antlr
//    id("com.github.johnrengelman.shadow") version "8.1.1"
//    id("org.springframework.boot") version "3.2.4"
//    kotlin("jvm") version "1.9.23"
//    kotlin("plugin.noarg") version "1.9.23"
}

apply(from = "./repository.gradle.kts")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    //<editor-fold desc="Platform dependencies">
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.4"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.4"))
    //</editor-fold>

    //<editor-fold desc="ANTLR dependencies">
    antlr(group = "org.antlr", name = "antlr4", version = "4.9.3")
    //</editor-fold>

    //<editor-fold desc="Compile-only dependencies">
    compileOnly(group = "com.amazonaws", name = "aws-lambda-java-core", version = "1.2.1")
    compileOnly(group = "org.slf4j", name = "slf4j-api")
    //</editor-fold>

    //<editor-fold desc="Implementation dependencies">
    implementation(group = "commons-io", name = "commons-io", version = "2.11.0")
    implementation(group = "com.amazonaws", name = "aws-lambda-java-events", version = "3.11.0")
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind")
    implementation(group = "com.fasterxml.jackson.datatype", name = "jackson-datatype-jsr310")
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin")
    implementation(group = "org.antlr", name = "antlr4-runtime", version = "4.9.3")
    implementation(group = "org.apache.commons", name = "commons-compress", version = "1.21")
    implementation(group = "org.apache.pdfbox", name = "pdfbox", version = "2.0.25")
    implementation(group = "org.slf4j", name = "slf4j-api")
    implementation(group = "org.springframework", name = "spring-core")
    implementation(group = "org.springframework.cloud", name = "spring-cloud-function-context")
    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    runtimeOnly(group = "com.amazonaws", name = "aws-lambda-java-log4j2", version = "1.5.1")
    runtimeOnly(group = "org.apache.logging.log4j", name = "log4j-slf4j-impl")
    runtimeOnly(group = "org.slf4j", name = "jcl-over-slf4j")
    //</editor-fold>

    //<editor-fold desc="Test implementation dependencies">
    testImplementation(group = "commons-codec", name = "commons-codec")
    testImplementation(group = "com.amazonaws", name = "aws-lambda-java-serialization", version = "1.0.0")
    testImplementation(group = "org.assertj", name = "assertj-core")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation(group = "org.mockito", name = "mockito-junit-jupiter")
    testImplementation(group = "org.mockito.kotlin", name = "mockito-kotlin", version = "4.0.0")
    testImplementation(group = "org.mockito", name = "mockito-core")
    testImplementation(group = "org.springframework", name = "spring-beans")
    testImplementation(group = "org.springframework", name = "spring-test")
    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    testRuntimeOnly(group = "ch.qos.logback", name = "logback-classic")
    testRuntimeOnly(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testRuntimeOnly(group = "org.springframework.boot", name = "spring-boot-starter-test")
    //</editor-fold>
}

repositories {
    val ciniraArtifacts: Action<RepositoryHandler> by rootProject.extra
    mavenCentral()
    ciniraArtifacts(this)
}

configurations.implementation {
    exclude(group = "com.ibm.icu", module = "icu4j")
    exclude(group = "commons-logging", module = "commons-logging")
}

//kotlin {
//    jvmToolchain {}
//}

//tasks.assemble {
//    dependsOn(tasks.shadowJar)
//}

//tasks.compileKotlin {
//    dependsOn(tasks.generateGrammarSource)
//}

//tasks.compileTestKotlin {
//    dependsOn(tasks.generateTestGrammarSource)
//}

tasks.generateGrammarSource {
//    arguments = arguments + listOf("-no-listener", "-visitor")
}

tasks.jar {
    enabled = false
}

//tasks.named("sourcesJar") {
//    dependsOn(tasks.generateGrammarSource)
//}

//tasks.withType<ShadowJar> {
//    archiveClassifier.set("")
//    transform(Log4j2PluginsCacheFileTransformer::class.java)
//}

tasks.withType<Test> {
    useJUnitPlatform()
}
