plugins {
    java
}

group = "io.github.unimat45"
version = "2.0"
description = "coord-plugin"

// Oldest Minecraft version the plugin supports. The plugin is compiled against this API,
// so any use of a method that doesn't exist on 1.8 fails the build.
// (Nothing older is published, and pre-1.8 servers don't expose Gson as com.google.gson.)
val minMinecraftVersion = "1.8"

// Latest patch of every Minecraft line up to the newest supported one.
// `./gradlew compatCheck` (also run by `./gradlew build`) compiles the sources against each,
// catching APIs that were removed or changed in newer versions.
val compatMinecraftVersions = listOf(
    "1.8", "1.8.8", "1.9.4", "1.10.2", "1.11.2", "1.12.2", "1.13.2", "1.14.4", "1.15.2", "1.16.5", "1.17.1", "1.18.2",
    "1.19.4", "1.20.6", "1.21.11", "26.1.2", "26.2", "26.3",
)

fun spigotApi(mc: String) = "org.spigotmc:spigot-api:$mc-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/public/") {
        name = "spigot-repo"
    }
}

dependencies {
    compileOnly(spigotApi(minMinecraftVersion))
    testImplementation("junit:junit:4.11")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    // Java 8 bytecode: 1.8-1.16 servers commonly run on Java 8, newer ones load it fine
    options.release.set(8)
    // Silence "source/target value 8 is obsolete" warnings on newer JDKs
    options.compilerArgs.add("-Xlint:-options")
}

val compatCheck = tasks.register("compatCheck") {
    group = "verification"
    description = "Compiles the plugin against every supported Spigot API version."
}

compatMinecraftVersions.forEach { mc ->
    val id = mc.replace('.', '_')
    val apiClasspath = configurations.create("spigotApi_$id") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
    dependencies.add(apiClasspath.name, spigotApi(mc))

    val compileTask = tasks.register<JavaCompile>("compileAgainst_$id") {
        group = "verification"
        description = "Compiles the plugin against spigot-api $mc."
        source = sourceSets.main.get().java
        classpath = apiClasspath
        destinationDirectory.set(layout.buildDirectory.dir("compat/$mc"))
    }
    compatCheck { dependsOn(compileTask) }
}

tasks.check {
    dependsOn(compatCheck)
}

tasks.processResources {
    // Keep plugin.yml's version in sync with the Gradle project version
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
