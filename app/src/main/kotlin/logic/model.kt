package logic

data class Card(
    val suit: String,
    val rank: Int,
)

data class Seat(
    val playerId: Int,
    val cards: List<Card>,
    val stack: Int,
    val out: Boolean,
    val lastAction: String,
    val currentRaise: Int,
    val handStrength: String,
)

data class CurrentAction(
    val seatInTurn: Int,
    val minRaise: Int,
    val lastSeatToRaise: Int,
)

data class HandState(
    val seats: List<Seat>,
    val smallBlindAmount: Int,
    val bigBlindAmount: Int,
    val currentAction: CurrentAction,
    val round: String,
    val communityCards: List<Card>,
    val pot: Int,
    val deck: List<Card>,
    val finished: Boolean,
    val gameFinished: Boolean,
    val winners: List<Int>,
)