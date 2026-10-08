plugins {
    // Applies fabric-loom-remap up to 1.21.11 and fabric-loom on 26.1+ (unobfuscated)
    id("dev.kikugie.loom-back-compat")
}

fun prop(name: String): String = project.property(name).toString()

val mc = sc.current.version
val requiredJava = if (sc.current.parsed >= "26.1") JavaVersion.VERSION_25 else JavaVersion.VERSION_21
// The dev client's game folder: 1.21.11 keeps the project's own run/ (it holds the devtest world),
// the other versions use versions/<version>/run.
val gameDir = if (mc == "1.21.11") rootProject.file("run") else file("run")

version = "${prop("mod.version")}+$mc"
group = prop("mod.group")
base { archivesName.set(prop("mod.archives_name")) }

repositories {
    maven("https://masa.dy.fi/maven/sakura-ryoko") { name = "SakuraRyoko" }
}

// Dev-only automated test (src/devtest): loaded in the dev clients, never part of the release jar.
val main: SourceSet = sourceSets["main"]
val devtest: SourceSet = sourceSets.create("devtest") {
    compileClasspath += main.compileClasspath + main.output
    runtimeClasspath += main.runtimeClasspath + main.output
}

val tweakeroo = "fi.dy.masa.tweakeroo:tweakeroo-fabric-$mc:${prop("deps.tweakeroo")}"
val malilib = "fi.dy.masa.malilib:malilib-fabric-$mc:${prop("deps.malilib")}"

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")

    // Tweakeroo and MaLiLib of this Minecraft version: compiled against, never bundled.
    modCompileOnly(tweakeroo) { isTransitive = false }
    modCompileOnly(malilib) { isTransitive = false }
    modLocalRuntime(tweakeroo) { isTransitive = false }
    modLocalRuntime(malilib) { isTransitive = false }
}

// The dev clients don't unpack the jars Tweakeroo carries inside (conditional-mixin): they are taken out
// of Tweakeroo's jar here and put next to it.
val tweakerooNested = layout.buildDirectory.dir("tweakeroo-nested").get().asFile
run {
    val jar = configurations.detachedConfiguration(dependencies.create(tweakeroo)).apply { isTransitive = false }.singleFile
    copy {
        from(zipTree(jar)) { include("META-INF/jars/*.jar") }
        eachFile { path = name }
        includeEmptyDirs = false
        into(tweakerooNested)
    }
    dependencies { "modLocalRuntime"(fileTree(tweakerooNested) { include("*.jar") }) }
}

java {
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava
    toolchain { languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion)) }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(requiredJava.majorVersion.toInt())
    options.encoding = "UTF-8"
}

val templateProps = mapOf(
    "version" to prop("mod.version"),
    "minecraft_version" to prop("mod.mc_compat"),
    "loader_compat" to prop("mod.loader_compat"),
    "java_compat" to prop("mod.java_compat"),
    "tweakeroo_compat" to prop("mod.tweakeroo_compat"),
    "mixin_java" to "JAVA_${requiredJava.majorVersion}",
    // Mixins that only exist for some versions (their sources are empty on the others)
    "extra_mixins" to if (sc.current.parsed < "1.21.11") ",\n\t\t\"LegacyDebugRendererMixin\"" else "",
)

tasks.named<ProcessResources>("processResources") {
    inputs.properties(templateProps)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(templateProps) }
}

tasks.named<Jar>("jar") {
    val baseName = prop("mod.archives_name")
    inputs.property("archivesName", baseName)
    from(rootProject.file("LICENSE")) { rename { "${it}_$baseName" } }
}

loom {
    mods {
        register(prop("mod.id")) { sourceSet(main) }
        register("freecamplus_devtest") { sourceSet(devtest) }
    }
    runs {
        // The dev client runs the dev test in the world "devtest" of its game folder.
        named("client") {
            source(devtest)
            runDir(gameDir.relativeTo(projectDir).path)
            programArgs("--quickPlaySingleplayer", "devtest")
        }
        // The same dev test inside Fabric's client game test, which makes a fresh world first
        // (gradlew :<version>:runClientGameTest). Screenshots: build/run/clientGameTest/screenshots/devtest.
        register("clientGameTest") {
            client()
            source(devtest)
            configName = "Client Game Test"
            property("fabric.client.gametest")
            runDir("build/run/clientGameTest")
            programArgs("--username", "FcpGameTest")
            ideConfigGenerated(false)
        }
    }
}

tasks.named("runClientGameTest") {
    doFirst { delete(layout.buildDirectory.dir("run/clientGameTest")) }
}

// The dev test as a jar of its own (build/devtest/), for a game folder of its own, e.g. a throwaway copy of a
// real one. Up to 1.21.11 it is translated to the names the real game uses, the same step Loom does for the
// release jar (gradlew :<version>:remapDevtestJar); on 26.1+ gradlew :<version>:devtestJar.
val devtestJar = tasks.register<Jar>("devtestJar") {
    from(devtest.output)
    archiveBaseName.set("freecamplus-devtest")
    destinationDirectory.set(layout.buildDirectory.dir("devtest"))
    if (!loomx.isUnobfuscated) {
        archiveClassifier.set("dev")
        destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
    }
}
if (!loomx.isUnobfuscated) {
    tasks.register<net.fabricmc.loom.task.RemapJarTask>("remapDevtestJar") {
        inputFile.set(devtestJar.flatMap { it.archiveFile })
        archiveBaseName.set("freecamplus-devtest")
        destinationDirectory.set(layout.buildDirectory.dir("devtest"))
        addNestedDependencies.set(false)
        classpath.from(devtest.compileClasspath)
    }
}
