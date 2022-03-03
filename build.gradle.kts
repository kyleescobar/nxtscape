tasks.wrapper {
    gradleVersion = "7.3"
}

allprojects {
    group = "dev.nxtscape"
    version = "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io/")
    }
}