plugins {
    kotlin("jvm") version "1.9.24"
    application
}

group = "com.bytedance.tools"
val adapterVersion = file("../VERSION").readText().trim()
version = adapterVersion

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.9.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.bytedance.tools.codelocator.adapter.MainKt")
    applicationName = "grab"
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    inputs.property("appVersion", adapterVersion)
    filesMatching("codelocator-adapter.properties") {
        expand(mapOf("appVersion" to adapterVersion))
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Implementation-Version"] = adapterVersion
    }
}
