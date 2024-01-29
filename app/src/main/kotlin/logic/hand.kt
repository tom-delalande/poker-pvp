package logic

import kotlin.math.floor

data class InitialPlayer(
    val playerId: Int,
    val stack: Int,
)

fun createInitialHandState(
    players: List<InitialPlayer>,
    smallBlind: Int,
    bigBlind: Int,
    remainingPot: Int = 0,
): HandState {
    val deck = createInitialDeck()
    val communityCards = listOf(
        deck.removeFirst(),
        deck.removeFirst(),
        deck.removeFirst(),
        deck.removeFirst(),
        deck.removeFirst(),
    )

    val seats = players.mapIndexed() { index, player ->
        val cards = listOf(deck.removeFirst(), deck.removeFirst())
        player.createSeat(index, smallBlind, bigBlind, cards)
    }

    return HandState(
        seats = seats,
        smallBlindAmount = smallBlind,
        bigBlindAmount = bigBlind,
        currentAction = CurrentAction(
            seatInTurn = 2 % seats.size,
            minRaise = bigBlind,
            lastSeatToRaise = -1,
        ),
        round = "Blinds",
        communityCards = communityCards,
        pot = smallBlind + bigBlind + remainingPot,
        deck = deck.toList(),
        finished = false,
        gameFinished = false,
        winners = listOf(),
    )
}

fun HandState.finishTurnForSeat(seatId: Int): HandState {
    if (currentAction.seatInTurn != seatId) return this
    val seatsIn = List(seats.filterNot { it.out }.size) { index -> index }
    if (seatsIn.size == 1) {
        return copy(
            winners = seatsIn,
            finished = true,
        ).finishHand()
    }

    var newSeatInTurn = (currentAction.seatInTurn + 1) % seats.size
    while (seats[newSeatInTurn].out) {
        newSeatInTurn = (newSeatInTurn + 1) % seats.size
    }

    if (newSeatInTurn == currentAction.lastSeatToRaise) {
        return copy(
            currentAction = currentAction.copy(seatInTurn = newSeatInTurn)
        ).finishRound()
    }

    val lastSeatToRaise = if (currentAction.lastSeatToRaise == -1) {
        currentAction.seatInTurn
    } else currentAction.lastSeatToRaise
    return copy(
        winners = winners,
        currentAction = currentAction.copy(
            seatInTurn = newSeatInTurn,
            lastSeatToRaise = lastSeatToRaise
        )
    )
}

private fun HandState.finishRound(): HandState {
    val newCurrentAction = CurrentAction(0, 0, -1)

    val everyoneAllIn = seats.none { !it.out && it.stack > 0 }
    val newSeats = seats.map {
        Seat(
            it.playerId,
            it.cards,
            it.stack,
            it.out,
            "None",
            0,
            "None",
        )
    }
    val newRound = when {
        round == "River" || everyoneAllIn -> {
            val winners = calculateWinners()
            return copy(
                currentAction = newCurrentAction,
                winners = winners,
                seats = newSeats.map { seat ->
                    seat.copy(
                        handStrength = (seat.cards + communityCards).rateHand().handStrength
                    )
                },
            ).finishHand()
        }

        round == "Blinds" -> "Flop"
        round == "Flop" -> "Turn"
        else -> "River"
    }
    return copy(
        round = newRound,
        seats = newSeats,
        currentAction = newCurrentAction,
    )
}

private fun HandState.finishHand(): HandState {
    val winners = calculateWinners()
    val payouts = handlePayouts()
    return payouts.copy(
        finished = true,
        winners = winners,
        seats = payouts.seats.mapIndexed { index, seat ->
            seat.copy(
                lastAction = if (winners.contains(index)) "Winner" else seat.lastAction,
            )
        },
    )
}

private fun HandState.calculateWinners(): List<Int> {
    val handRatings = seats.map { (it.cards + communityCards).rateHand().score }
    val ratingMap = handRatings.mapIndexed { index, rating -> index to rating }.toMap()
    val winningRating = handRatings.max()
    return ratingMap.filterValues { it == winningRating }.map { it.key }
}

private fun HandState.handlePayouts(): HandState {
    val winnings = floor(pot.toDouble() / winners.size).toInt()
    val excess = pot - winnings * winners.size

    val newSeats = seats.mapIndexed { index, seat ->
        if (winners.contains(index)) seat.copy(stack = seat.stack + winnings) else seat
    }
    return copy(
        pot = excess,
        seats = newSeats,
    )
}

fun HandState.prepareNextHand(): HandState {
    val players = seats.map {
        InitialPlayer(
            it.playerId,
            it.stack
        )
    }
    val orderedPlayers = listOf(players.first()) + players.subList(1, players.size)
    return createInitialHandState(
        orderedPlayers,
        smallBlindAmount,
        bigBlindAmount,
        pot,
    )
}

fun InitialPlayer.createSeat(index: Int, smallBlind: Int, bigBlind: Int, cards: List<Card>): Seat {
    val isSmallBlind = index == 0
    val isBigBlind = index == 1

    val lastAction = if (isSmallBlind) {
        "Small Blind"
    } else if (isBigBlind) {
        "Big Blind"
    } else {
        "None"
    }

    val stack = if (isSmallBlind) {
        stack - smallBlind
    } else if (isBigBlind) {
        stack - bigBlind
    } else {
        stack
    }

    val currentRaise = if (isSmallBlind) {
        smallBlind
    } else if (isBigBlind) {
        bigBlind
    } else {
        0
    }
    return Seat(
        playerId,
        lastAction = lastAction,
        currentRaise = currentRaise,
        stack = stack,
        out = false,
        cards = cards,
        handStrength = "None",
    )
}

private fun createInitialDeck(): MutableList<Card> {
    return listOf("hearts", "spades", "clubs", "diamonds").flatMap { suit ->
        (1..13).map { rank ->
            Card(suit, rank)
        }
    }.shuffled().toMutableList()
}