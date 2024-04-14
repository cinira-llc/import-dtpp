import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
//    kotlin("kapt") version "1.9.23" apply false
    antlr
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.springframework.boot") version "3.2.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.noarg") version "1.9.23"
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
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.1"))
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.4"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.1"))
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.701"))
    //</editor-fold>

    //<editor-fold desc="ANTLR dependencies">
    antlr("org.antlr:antlr4:4.9.3")
    //</editor-fold>

    //<editor-fold desc="Compile-only dependencies">
    compileOnly("com.amazonaws:aws-lambda-java-core:1.2.3")
    compileOnly("org.slf4j:slf4j-api")
    //</editor-fold>

    //<editor-fold desc="Implementation dependencies">
    implementation("commons-io:commons-io:2.16.1")
    implementation("com.amazonaws:aws-lambda-java-events:3.11.5")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.apache.commons:commons-compress:1.26.1")
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("org.apache.pdfbox:pdfbox-io:3.0.2")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework:spring-core")
    implementation("org.springframework.cloud:spring-cloud-function-context")
    implementation("io.awspring.cloud:spring-cloud-aws-s3")
    implementation("io.awspring.cloud:spring-cloud-aws-core") {
        exclude("com.amazonaws:aws-java-sdk-ec2")
        exclude("com.amazonaws:aws-java-sdk-kms")
    }
    implementation("com.amazonaws:aws-java-sdk-s3") {
        exclude("com.amazonaws:aws-java-sdk-kms")
    }

    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    runtimeOnly("com.amazonaws:aws-lambda-java-log4j2:1.5.1")
    runtimeOnly("io.awspring.cloud:spring-cloud-aws-core")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl")
    runtimeOnly("org.slf4j:jcl-over-slf4j")

    //</editor-fold>

    //<editor-fold desc="Test implementation dependencies">
    testImplementation("commons-codec:commons-codec")
    testImplementation("com.amazonaws:aws-lambda-java-serialization:1.1.5")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.3.1")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.springframework:spring-beans")
    testImplementation("org.springframework:spring-test")
    //</editor-fold>

    //<editor-fold desc="Runtime-only dependencies">
    testRuntimeOnly("ch.qos.logback:logback-classic")
    testRuntimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.springframework.boot:spring-boot-starter-test")
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
    archiveClassifier.set("")
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
