plugins {
    id("java")
}

group = "net.flamgop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("org.jocl:jocl:2.0.5")
    implementation("dev.hollowcube:minestom-ce:8715f4305d")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}