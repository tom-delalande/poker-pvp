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
import templates.actions
import templates.game as gameTemplate
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import templates.index
import templates.queue
import templates.raiseMenu

fun Application.endpoints() {
    routing {
        index()
        queue()
        game()
    }
}

fun Routing.index() = get("/") {
    call.respondHtml {
        index()
    }
}

val idGenerator = Random

fun Routing.queue() = route("/queue") {
    get("/poll/{playerId}") {
        val playerId = call.parameters["playerId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val game = games.find { it.players.contains(playerId) } ?: return@get call.respondHtml {
            body {
                queue(playerId)
            }
        }
        call.respondHtml {
            val playerState = game.hand.createHandStateForPlayer(game.gameId, playerId)
            body {
                gameTemplate(playerState)
            }
        }
    }

    post {
        val playerId = idGenerator.nextInt(0, 500)
        playersInQueue.add(playerId)
        createAvailableGames()
        call.respondHtml {
            body {
                queue(playerId)
            }
        }
    }
}

fun Routing.game() = route("/game") {
    webSocket("/{gameId}/player/{playerId}/ws") {
        val gameId = call.parameters["gameId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val playerId = call.parameters["playerId"]?.toInt() ?: return@webSocket call.respond(HttpStatusCode.BadRequest)
        val game = games.first { it.gameId == gameId }
        val initialPlayerState = game.hand.createHandStateForPlayer(gameId, playerId)
        game.playerWebsockets.add(PlayerWebsocket(playerId, this, initialPlayerState))
        sendNewUIChangesInPlayerState(null, initialPlayerState)
        for (frame in incoming) {
            frame as? Frame.Text ?: continue
            val receivedText = frame.readText()
            val actionRequest = json.decodeFromString<ActionRequest>(receivedText)
            val action = Action(
                actionRequest.action,
                actionRequest.amount.toInt(),
            )
            game.handleAction(playerId, action)
        }
    }

    get("/raise-menu/{playerId}") {
        val playerId = call.parameters["playerId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val game = games.first { it.players.contains(playerId) }
        val player = game.playerWebsockets.first { it.playerId == playerId }
        call.respondHtml {
            body {
                raiseMenu(player.playerState.actions)
            }
        }
    }
    get("/raise-menu/{playerId}/back") {
        val playerId = call.parameters["playerId"]?.toInt() ?: return@get call.respond(HttpStatusCode.BadRequest)
        val game = games.first { it.players.contains(playerId) }
        val player = game.playerWebsockets.first { it.playerId == playerId }
        call.respondHtml {
            body {
                actions(player.playerState.actions)
            }
        }
    }
}

val json = Json {
    ignoreUnknownKeys = true
}

@Serializable
data class ActionRequest(
    val action: String,
    val amount: String,
)
