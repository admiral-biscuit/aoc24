import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.9.22"
  application
}

group = "de.akquinet.jkl"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  val kotestVersion = "5.9.1"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

application { mainClass.set("MainKt") }
