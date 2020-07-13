plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.72"
}

group = "no.knowledge"
version = "1.0-SNAPSHOT"

val kotlinVersion: String by project

repositories {
    mavenCentral()
}

dependencies {
    // check newer versions..
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("com.badlogicgames.gdx:gdx:1.9.10")
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl:1.9.10")
    implementation("com.badlogicgames.gdx:gdx-platform:1.9.10:natives-desktop")
    implementation("io.github.libktx:ktx-app:1.9.10-b2")
    implementation("io.github.libktx:ktx-graphics:1.9.10-b2")
}