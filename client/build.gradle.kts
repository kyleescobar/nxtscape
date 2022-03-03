plugins {
    kotlin("jvm")
}

dependencies {
    implementation(Kotlin.stdlib)
    implementation("org.tinylog:tinylog-api-kotlin:_")
    implementation("org.tinylog:tinylog-impl:_")
    implementation("net.java.dev.jna:jna:_")
    implementation("net.java.dev.jna:jna-platform:_")
}

tasks {
    register<JavaExec>("runClient") {
        group = "application"
        workingDir = projectDir
        mainClass.set("dev.nxtscape.client.Main")
        classpath = sourceSets["main"].runtimeClasspath
    }
}