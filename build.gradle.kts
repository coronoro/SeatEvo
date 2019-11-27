import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "htwk"
version = "1.0-SNAPSHOT"

repositories {

    mavenCentral()
    jcenter()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.beust:klaxon:5.1")
    compile(group= "org.restlet.jee", name= "org.restlet", version= "2.3.12")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    implementation("com.github.coronoro:marudorAPI:8d111dd520")
    compile( "org.jgrapht", "jgrapht-core",  "1.3.1")
    compile( "org.jgrapht", "jgrapht-io",  "1.3.1")
    compile( "org.jgrapht", "jgrapht-ext",  "1.3.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>(){
    kotlinOptions.jvmTarget = "1.8"
}





