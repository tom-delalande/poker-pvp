package server

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import kotlin.random.Random
import kotlinx.html.body
import templates.index
import templates.queue
import templates.raiseMenu

fun Application.endpoints(games: MutableList<WebsocketGame>) {
    routing {
        index()
        queue()
        game(games)
    }
}

fun Routing.index() = get("/") {
    call.respondHtml {
        index()
    }
}

val idGenerator = Random(1)

fun Routing.queue() = route("/queue") {
    get("/poll/{playerId}") {
        val playerId = call.parameters["playerId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
        call.respondHtml {
            body {
                queue(playerId)
            }
        }
    }

    post {
        val playerId = idGenerator.nextInt()
        playersInQueue.add(playerId)
        createAvailableGames()
        call.respondHtml {
            body {
                queue(playerId)
            }
        }
    }
}

fun Routing.game(games: MutableList<WebsocketGame>) = route("/game") {
    webSocket("/{gameId}/player/{playerId}/ws") {
        val gameId = call.parameters["gameId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val playerId = call.parameters["playerId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val game = games.first { it.gameId == gameId }
        val initialPlayerState = game.hand.createHandStateForPlayer(gameId, playerId)
        game.playerWebsockets.add(PlayerWebsocket(playerId, this, initialPlayerState))
        sendNewUIChangesInPlayerState(null, initialPlayerState)
    }

    get("/raise-menu/{playerId}") {
        val playerId = call.parameters["playerId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val game = games.find { it.players.contains(playerId) }
        call.respondHtml {
            body {
            }
        }
    }
    get("/raise-menu/back") {

    }
}
