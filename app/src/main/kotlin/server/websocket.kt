package server

import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import java.util.Collections
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.html.FlowContent
import logic.Card
import logic.HandState
import logic.finishTurnForSeat
import logic.performCall
import logic.performCheckFold
import logic.performRaise
import logic.prepareNextHand
import templates.ActionBlock
import templates.HandStateForPlayer
import templates.Opponent
import templates.actions
import templates.communityCards
import templates.opponents
import templates.playerHandStrength
import templates.playerLastAction
import templates.playerStack
import templates.pocketCards
import templates.pot

class WebsocketGame(
    val gameId: Int,
    val players: List<Int>,
    var hand: HandState,
    val playerWebsockets: MutableList<PlayerWebsocket>,
)

data class PlayerWebsocket(
    val playerId: Int,
    val socket: WebSocketSession,
    var playerState: HandStateForPlayer,
)

data class Action(
    val action: String,
    val amount: Int?,
)

val playersInQueue: MutableList<Int> = Collections.synchronizedList(mutableListOf<Int>())
val games: MutableList<WebsocketGame> = Collections.synchronizedList(mutableListOf<WebsocketGame>())

suspend fun WebsocketGame.handleAction(playerId: Int, action: Action) {
    val seatId = hand.seats.indexOfFirst { it.playerId == playerId }
    hand = when (action.action) {
        "CheckFold" -> hand.performCheckFold(seatId)
        "Call" -> hand.performCall(seatId)
        "Raise" -> hand.performRaise(seatId, action.amount ?: 0)
        else -> hand
    }
    hand = hand.finishTurnForSeat(seatId)
    val numberOfInPlayers = hand.seats.filter { !it.out && it.stack > 0 }
    val gameFinished = hand.finished && numberOfInPlayers.size == 1

    hand = hand.copy(
        gameFinished = gameFinished,
    )

    updateHandForPlayers()
    if (hand.finished && !hand.gameFinished) {
        scheduleNextHand()
    }
}

private suspend fun WebsocketGame.updateHandForPlayers() {
    playerWebsockets.forEach {
        val previousState = it.playerState
        val newState = hand.createHandStateForPlayer(gameId, it.playerId)
        it.playerState = newState
        it.socket.sendNewUIChangesInPlayerState(previousState, newState)
    }
}

fun HandState.createHandStateForPlayer(gameId: Int, playerId: Int): HandStateForPlayer {
    val seat = seats.first { it.playerId == playerId }
    val seatId = seats.indexOfFirst { it.playerId == playerId }
    val opponents = seats.filterNot { it.playerId == playerId }.map {
        Opponent(
            it.stack,
            it.playerId,
            if (finished) it.cards else emptyList(),
            it.lastAction,
            it.handStrength
        )
    }

    val outOfTurn = currentAction.seatInTurn != seatId
    val actions = ActionBlock(
        checkFoldLabel = if (currentAction.minRaise > seat.currentRaise) "Fold" else "Check",
        checkDisabled = (currentAction.minRaise > seat.currentRaise) || outOfTurn,
        callAmount = if (!outOfTurn && currentAction.minRaise > seat.currentRaise) currentAction.minRaise - seat.currentRaise else 0,
        callDisabled = outOfTurn || currentAction.minRaise <= seat.currentRaise,
        raiseDisabled = outOfTurn || currentAction.minRaise > seat.stack || seat.stack == 0,
        raiseAmounts = setOf(
            min(currentAction.minRaise + 1, seat.stack),
            (currentAction.minRaise + seat.stack - currentAction.minRaise) / 4,
            (currentAction.minRaise + seat.stack - currentAction.minRaise) / 2,
            (currentAction.minRaise + seat.stack - currentAction.minRaise),
        ).toList(),
        handFinished = finished,
        gameFinished = gameFinished,
        playerId = playerId,
    )
    return HandStateForPlayer(
        playerId = playerId,
        gameId = gameId,
        pocketCards = seat.cards,
        stack = seat.stack,
        pot = pot,
        lastAction = seat.lastAction,
        handStrength = seat.handStrength,
        opponents = opponents,
        communityCards = calculateShownCommunityCards(),
        actions = actions,
    )
}

private fun HandState.calculateShownCommunityCards(): List<Card> {
    val hidden = Card(
        suit = "Hidden",
        rank = -1,
    )
    if (finished) {
        return communityCards
    }
    val numberOfShownCards = when (round) {
        "Blinds" -> 0
        "Flop" -> 3
        "Turn" -> 4
        else -> 5
    }
    return communityCards.mapIndexed { index, card ->
        if (index > numberOfShownCards - 1) {
            hidden
        } else card
    }
}

private suspend fun WebsocketGame.scheduleNextHand() {
    delay(3000)
    hand = hand.prepareNextHand()
    updateHandForPlayers()
}

suspend fun WebSocketSession.sendNewUIChangesInPlayerState(
    previousState: HandStateForPlayer?,
    newState: HandStateForPlayer,
) {
    if (newState.opponents != previousState?.opponents) send { opponents(newState.opponents) }
    if (newState.communityCards != previousState?.communityCards) send { communityCards(newState.communityCards) }
    if (newState.pocketCards != previousState?.pocketCards) send { pocketCards(newState.pocketCards) }
    if (newState.handStrength != previousState?.handStrength) send { playerHandStrength(newState.handStrength) }
    if (newState.stack != previousState?.stack) send { playerStack(newState.stack) }
    if (newState.lastAction != previousState?.lastAction) send { playerLastAction(newState.lastAction) }
    if (newState.actions != previousState?.actions) send { actions(newState.actions) }
    if (newState.pot != previousState?.pot) send { pot(newState.pot) }
}

private suspend fun WebSocketSession.send(block: FlowContent.() -> Unit) {
    send(
        buildPacket {
            appendHTML().with {
                block()
            }
        }.readText()
    )
}

