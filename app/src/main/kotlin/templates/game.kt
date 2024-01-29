package templates

import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.button
import kotlinx.html.classes
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.input
import kotlinx.html.p
import logic.Card


fun FlowContent.game(state: HandStateForPlayer) = div {
    classes = setOf("flex flex-col justify-around h-full bg-neutral-300 app-container")
    attributes["hx-ext"] = "ws"
    attributes["ws-connect"] = "/game/${state.gameId}/player/${state.playerId}/ws"

    opponents(state.opponents)
    communityCards(state.communityCards)
    pot(state.pot)
    div {
        classes = setOf("flex flex-col gap-2 items-center")

        playerLastAction(state.lastAction)
        playerHandStrength(state.handStrength)
        playerStack(state.stack)
        pocketCards(state.pocketCards)
        actions(state.actions)
    }
}

fun FlowContent.opponents(opponents: List<Opponent>) =  div {
    id = "opponents"

    classes = setOf("flex flex-col justify-center items-center")

    opponents.map {
        div { +"Player#${it.playerId}" }
        stack(it.stack)
        if (it.handStrength != "None") {
            div {
                +it.handStrength
            }
        }
        lastAction(it.lastAction)
    }
}

fun FlowContent.communityCards(cards: List<Card>) = div {
    id = "community-cards"
    classes = setOf("flex gap-2 justify-center items-center")

    cards.map {
        card(it)
    }

}

fun FlowContent.card(card: Card) = div {
    classes =
        getStyleFromSuit(card.suit) + setOf("rounded-md w-10 h-10 md:w-16 md:h-16 text-2xl flex items-center justify-center gap-1 relative drop-shadow-sm text-2xl")

    when {
        card.suit == "Hidden" -> i { classes = setOf("fa-solid fa-eye-slash") }
        card.rank == 1 -> i { classes = setOf("fa-solid fa-a") }
        card.rank < 10 -> i { classes = setOf("fa-solid fa-${card.rank}") }
        card.rank == 10 -> div {
            i { classes = setOf("fa-solid fa-1") }
            i { classes = setOf("fa-solid fa-0") }
        }

        card.rank == 11 -> i { classes = setOf("fa-solid fa-chess-bishop") }
        card.rank == 12 -> i { classes = setOf("fa-solid fa-chess-queen") }
        card.rank == 13 -> i { classes = setOf("fa-solid fa-chess-king") }
    }
}

fun FlowContent.pot(pot: Int) = div {
    id = "pot"
    classes = setOf("flex justify-center items-center")

    stack(pot)
}

fun FlowContent.playerLastAction(lastAction: String) = div {
    id = "last-action"
    lastAction(lastAction)
}

fun FlowContent.lastAction(action: String) = div {
    when (action) {
        "Winner" -> {
            classes = setOf("border-2 border-yellow-900 rounded-md px-4 bg-yellow-300 text-yellow-900")
            +"Winner"
        }

        "None" -> {
            classes = setOf("border-2 border-neutral-300 rounded-md px-4 bg-neutral-300 text-neutral-300")
            +"None"
        }

        "Check" -> {
            classes = setOf("border-2 border-neutral-900 rounded-md px-4 bg-neutral-300 text-neutral-900")
            +"Check"
        }

        "Fold" -> {
            classes = setOf("border-2 border-red-900 rounded-md px-4 bg-red-300 text-red-900")
            +"Fold"
        }

        "Call" -> {
            classes = setOf("border-2 border-green-900 rounded-md px-4 bg-green-300 text-green-900")
            +"Call"
        }

        "Raise" -> {
            classes = setOf("border-2 border-yellow-900 rounded-md px-4 bg-yellow-400 text-yellow-900")
            +"Raise"
        }

        "Big Blind" -> {
            classes = setOf("border-2 border-yellow-900 rounded-md px-4 bg-yellow-400 text-yellow-900")
            +"Big Blind"
        }

        "Small Blind" -> {
            classes = setOf("border-2 border-yellow-900 rounded-md px-4 bg-yellow-200 text-yellow-900")
            +"Small Blind"
        }
    }
}

fun FlowContent.playerHandStrength(handStrength: String) = div {
    id = "hand-strength"
    if (handStrength != "None") div { +handStrength }
}

fun FlowContent.playerStack(stack: Int) = div {
    id = "stack"
    stack(stack)
}

fun FlowContent.stack(amount: Int) = div {
    classes = setOf("flex items-center gap-1")
    p { +amount.toString() }
    i { classes = setOf("fa-solid fa-coins") }
}

fun FlowContent.pocketCards(cards: List<Card>) = div {
    id = "pocket-cards"
    classes = setOf("flex gap-2 justify-center")

    cards.map { card(it) }
}

fun FlowContent.actions(actions: ActionBlock) {
    when {
        actions.gameFinished -> div {
            id = "actions"
            classes = setOf("flex flex-col items-center justify-center")
            +"Game Finished"
            a {
                href = "/"
                classes =
                    setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")
                +"Back"
            }
        }

        actions.handFinished -> div {
            id = "actions"
            +"Preparing next hand..."
        }

        else -> form {
            id = "actions"
            classes = setOf("flex gap-2 justify-center")

            attributes["ws-send"] = ""

            button {
                classes =
                    setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")
                name = "action"
                type = ButtonType.submit
                value = "CheckFold"
                disabled = actions.checkDisabled
                +actions.checkFoldLabel
            }
            button {
                classes =
                    setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")
                name = "action"
                type = ButtonType.submit
                value = "Call"
                disabled = actions.callDisabled
                +"Call"
                if (actions.callAmount > 0) {
                    +" ${actions.callAmount}"
                    i { classes = setOf("fa-solid fa-coins") }
                }
            }
            button {
                classes =
                    setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")

                attributes["hx-get"] = "/game/raise-menu/${actions.playerId}"
                attributes["hx-swap"] = "outerHTML"
                attributes["hx-target"] = "#actions"
                disabled = actions.raiseDisabled
                +"Raise"
            }

        }
    }
}

fun FlowContent.raiseMenu(actions: ActionBlock) = div {
    id = "actions"
    classes = setOf("flex gap-2 justify-center")

    button {
        classes =
            setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")
        attributes["hx-get"] = "game/raise-menu/${actions.playerId}/back"
        attributes["hx-swap"] = "outerHTML"
        attributes["hx-target"] = "#actions"
        +"Back"
    }
    div {
        classes = setOf("flex gap-2 justify-center")
        actions.raiseAmounts.map {
            form {
                attributes["ws-send"] = ""
                input {
                    classes = setOf("hidden")
                    type = InputType.number
                    name = "amount"
                    value = it.toString()
                }
                button {
                    classes =
                        setOf("px-4 py-2 min-w-max w-24 bg-gray-50 rounded-md active:scale-90 transition flex gap-2 items-center justify-center disabled:bg-neutral-300 disabled:border-2 disabled:border-gray-50 disabled:active:scale-100")
                    name = "action"
                    type = ButtonType.submit
                    value = "Raise"
                    +it.toString()
                    i { classes = setOf("fa-solid fa-coins") }
                }
            }
        }
    }
}

data class HandStateForPlayer(
    val playerId: Int,
    val gameId: Int,
    val pocketCards: List<Card>,
    val stack: Int,
    val pot: Int,
    val lastAction: String,
    val handStrength: String,
    val opponents: List<Opponent>,
    val communityCards: List<Card>,
    val actions: ActionBlock,
)

data class Opponent(
    val stack: Int,
    val playerId: Int,
    val cards: List<Card>,
    val lastAction: String,
    val handStrength: String,
)


data class ActionBlock(
    val checkFoldLabel: String,
    val checkDisabled: Boolean,
    val callDisabled: Boolean,
    val callAmount: Int,
    val raiseDisabled: Boolean,
    val raiseAmounts: List<Int>,
    val playerId: Int,
    val handFinished: Boolean,
    val gameFinished: Boolean,
)

fun getStyleFromSuit(suit: String): Set<String> = setOf(
    when (suit) {
        "hearts" -> "bg-red-400 text-white"
        "diamonds" -> "bg-green-400 text-white"
        "spades" -> "bg-neutral-600 text-white"
        "clubs" -> "bg-white text-neutral-600"
        "Hidden" -> "bg-neutral-400 text-white"
        else -> ""
    }
)