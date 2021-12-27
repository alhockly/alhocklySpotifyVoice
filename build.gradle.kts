import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.41"
}

group = "SpotifyVoice"
version = "2.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}



dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("stdlib"))
    testCompile("junit", "junit", "4.4")
    testImplementation("junit", "junit", "4.4")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    implementation("org.seleniumhq.selenium:selenium-server:3.0.1")
    //compile("com.github.goxr3plus:java-google-speech-api:V2.1")
    compile("com.google.guava:guava:22.0")
    compile ("com.github.thelinmichael:spotify-web-api-java:master-SNAPSHOT")
    //compile ("com.github.jmdns:jmdns:master-SNAPSHOT")
    compile("javax.xml.ws:jaxws-api:2.2.11")
    compile("net.sourceforge.javaflacencoder:java-flac-encoder:0.3.7")
    compile("org.json:json:20210307")
    implementation("ai.picovoice:porcupine-java:2.0.2")

}

tasks.test {
    useJUnitPlatform()
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