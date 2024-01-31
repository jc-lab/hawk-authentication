import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Version.KOTLIN
}

val projectGroup = "kr.jclab.hawk"
val projectVersion = Version.PROJECT
group = projectGroup
version = projectVersion

repositories {
    mavenCentral()
}

allprojects {
    group = projectGroup
    version = projectVersion
}
