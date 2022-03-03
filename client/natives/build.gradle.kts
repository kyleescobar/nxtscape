plugins {
    id("fr.stardustenterprises.rust.wrapper") version "2.1.0"
}

rust {
    command = "cargo"
    outputs = mutableMapOf("" to System.mapLibraryName("nxtscape"))
    outputDirectory = "bin/"
    profile = "release"
    toolchain = "stable-x86_64-pc-windows-msvc"
}

tasks {
    register<Copy>("buildRust") {
        dependsOn(build)
        group = "build"
        val dir = project(":client").projectDir.resolve("src/main/resources/bin/")
        doFirst {
            dir.resolve("nxtscape.dll").deleteRecursively()
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(projectDir.resolve("build/rustOutput/bin/nxtscape.dll"))
        into(dir)
    }
}

