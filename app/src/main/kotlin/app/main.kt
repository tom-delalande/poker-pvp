package app

import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration
import kotlinx.serialization.json.Json
import server.WebsocketGame
import server.endpoints


fun main() {
    embeddedServer(Netty, port = 9090) {
        install(WebSockets)
        install(ContentNegotiation) {
            json()
        }
        endpoints()
    }.start(wait = true)
}
