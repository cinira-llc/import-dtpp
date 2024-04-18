import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    antlr
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.noarg") version "1.9.23"
}

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
    implementation(platform("software.amazon.awssdk:bom:2.25.31"))
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.23.1"))
    //</editor-fold>

    //<editor-fold desc="ANTLR dependencies">
    antlr("org.antlr:antlr4:4.13.1")
    //</editor-fold>

    //<editor-fold desc="Compile-only dependencies">
    compileOnly("com.amazonaws:aws-lambda-java-core:1.2.3")
    compileOnly("software.amazon.awssdk:aws-core")
    //</editor-fold>

    //<editor-fold desc="Implementation dependencies">
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.5")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl")
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("org.apache.pdfbox:pdfbox-io:3.0.2")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework:spring-core")
    implementation("software.amazon.awssdk:s3")
    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.6.0")
    //</editor-fold>

    //<editor-fold desc="Test implementation dependencies">
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.7"))
    testImplementation("commons-codec:commons-codec")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.springframework:spring-beans")
    testImplementation("org.springframework:spring-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:localstack")
    testImplementation("software.amazon.awssdk:auth")
    testImplementation("software.amazon.awssdk:regions")
    testImplementation("software.amazon.awssdk:s3")
    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    testRuntimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.testcontainers:localstack")
    //</editor-fold>
}

repositories {
    mavenCentral()
    mavenLocal()
}

configurations {
    runtimeOnly {
        exclude("com.ibm.icu", "icu4j")
        exclude("org.slf4j", "jul-to-slf4j")
        exclude("org.apache.logging.log4j", "log4j-to-slf4j")
    }
}

kotlin {
    jvmToolchain {}
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

//tasks.generateGrammarSource {
//    arguments = arguments + listOf("-no-listener", "-visitor")
//}

tasks.jar {
    enabled = false
}

tasks.named("sourcesJar") {
    dependsOn(tasks.generateGrammarSource)
}

tasks.withType<ShadowJar> {
    archiveClassifier = "aws-lambda"
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
