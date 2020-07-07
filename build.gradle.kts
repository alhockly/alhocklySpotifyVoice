import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.41"
}

group = "SpotifyVoice"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}



dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.12")
    implementation("org.seleniumhq.selenium:selenium-server:3.0.1")
    compile("com.github.goxr3plus:java-google-speech-api:V2.1")
    compile("com.google.guava:guava:22.0")
    compile ("com.github.thelinmichael:spotify-web-api-java:master-SNAPSHOT")

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "com.spotifyVoice.Main"
    }

    // To add all of the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}