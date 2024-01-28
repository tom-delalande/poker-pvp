package app

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import java.time.Duration
import server.WebsocketGame
import server.endpoints


fun main() {
    embeddedServer(Netty, port = 8080) {
        install(WebSockets) {
            pingPeriodMillis = 0L
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        endpoints()
    }.start(wait = true)
}
