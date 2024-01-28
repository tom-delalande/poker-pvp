package server

import logic.InitialPlayer
import logic.createInitialHandState

fun createAvailableGames() {
    if (playersInQueue.size > 1) {
        val gameId = idGenerator.nextInt(0, 500)
        val player1 = playersInQueue.removeFirst()
        val player2 = playersInQueue.removeFirst()
        games.add(
            WebsocketGame(
                gameId,
                listOf(player1, player2),
                createInitialHandState(
                    listOf(
                        InitialPlayer(player1, 20),
                        InitialPlayer(player2, 20),
                    ),
                    1,
                    2,
                    0
                ),
                mutableListOf()
            )
        )
    }
}