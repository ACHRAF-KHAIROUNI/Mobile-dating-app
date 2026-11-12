plugins { alias(libs.plugins.kotlin.jvm); alias(libs.plugins.kotlin.serialization); application }
repositories { mavenCentral() }
group = "fr.unica.miage.komeet"
version = "1.0.0"
application { mainClass.set("fr.unica.miage.komeet.ApplicationKt") }
kotlin { jvmToolchain(21) }
dependencies {
 implementation(libs.kotlin.stdlib); implementation(libs.kotlinx.serialization.json); implementation(libs.kotlinx.coroutines.core)
 implementation(libs.ktor.server.core); implementation(libs.ktor.server.netty); implementation(libs.ktor.server.auth); implementation(libs.ktor.server.auth.jwt); implementation(libs.ktor.server.content.negotiation); implementation(libs.ktor.serialization.kotlinx.json); implementation(libs.ktor.server.status.pages); implementation(libs.ktor.server.call.logging); implementation(libs.ktor.server.cors)
 implementation(libs.ktor.client.core); implementation(libs.ktor.client.cio); implementation(libs.ktor.client.content.negotiation)
 implementation(libs.exposed.core); implementation(libs.exposed.jdbc); implementation(libs.exposed.java.time); implementation(libs.sqlite.jdbc)
 implementation(libs.bcrypt); implementation(libs.dotenv); implementation(libs.logback.classic)
 testImplementation(libs.junit.jupiter); testImplementation(libs.junit.jupiter.api); testRuntimeOnly(libs.junit.jupiter.engine); testRuntimeOnly(libs.junit.platform.launcher); testImplementation(libs.ktor.server.test.host); testImplementation(libs.sqlite.jdbc)
}
tasks.test { useJUnitPlatform() }
