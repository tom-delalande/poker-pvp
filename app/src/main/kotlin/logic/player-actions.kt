@file:Suppress("NAME_SHADOWING")

package logic

import kotlin.math.min

fun HandState.performCheckFold(seatId: Int): HandState {
    if (isActionOutOfTurn(seatId)) return this

    val seat = seats[seatId]
    return if (currentAction.minRaise > seat.currentRaise) {
        copy(
            seats = seats.mapIndexed { index, seat ->
                if (index == seatId) seat.copy(lastAction = "Fold", out = true) else seat
            }
        )
    } else {
        copy(
            seats = seats.mapIndexed { index, seat ->
                if (index == seatId) seat.copy(lastAction = "Check") else seat
            }
        )
    }
}

fun HandState.performCall(seatId: Int): HandState {
    if (isActionOutOfTurn(seatId)) return this

    val seat = seats[seatId]
    val callAmount = min(seat.stack, currentAction.minRaise - seat.currentRaise)
    return copy(
        pot = pot + callAmount,
        seats = seats.mapIndexed { index, seat -> if (index == seatId) seat.copy(
            lastAction = "Call",
            stack = seat.stack - callAmount,
            currentRaise = seat.currentRaise + callAmount, // This line was not in the previous version
        ) else seat }
    )
}

fun HandState.performRaise(seatId: Int, raiseAmount: Int): HandState {
    if (isActionOutOfTurn(seatId)) return this

    val seat = seats[seatId]
    return copy(
        pot = pot + raiseAmount,
        currentAction = currentAction.copy(
            minRaise = raiseAmount + seat.currentRaise,
            lastSeatToRaise = seatId,
        ),
        seats = seats.mapIndexed { index, seat -> if (index == seatId) seat.copy(
            lastAction = "Raise",
            stack = seat.stack - raiseAmount,
            currentRaise = seat.currentRaise + raiseAmount,
        ) else seat }
    )
}

private fun HandState.isActionOutOfTurn(seat: Int): Boolean {
    return currentAction.seatInTurn != seat
}
