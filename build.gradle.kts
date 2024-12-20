import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { kotlin("jvm") version "1.9.22" }

group = "de.akquinet.jkl"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  val arrowVersion = "1.2.4"
  implementation("io.arrow-kt:arrow-core:$arrowVersion")
  implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

  val kotestVersion = "5.9.1"
  testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")

  val kotestExtensionsArrowVersion = "1.4.0"
  implementation("io.kotest.extensions:kotest-assertions-arrow:$kotestExtensionsArrowVersion")
}

tasks.test { useJUnitPlatform() }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
