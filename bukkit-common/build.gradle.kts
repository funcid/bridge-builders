plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    compileOnly("cristalix:bukkit-core:21.01.30")
    compileOnly("cristalix:dark-paper:21.02.03")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0")

    compileOnly("clepto:clepto-bukkit:3.4.2")
    compileOnly("clepto:clepto-cristalix:3.0.2")
    compileOnly("implario:kotlin-api:1.1.1")
    compileOnly("implario:bukkit-tools:4.4.12")
    compileOnly("me.func:animation-api:2.6.21")

    compileOnly(project(":service-common"))
}