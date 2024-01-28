package logic

data class HandRating(
    val handStrength: String,
    val score: Double,
)

fun List<Card>.rateHand(): HandRating {
    val hands = calculateHands()
    val highCards = hands.highCard
    if (hands.straightFlush.isNotEmpty()) {
        val score = calculateHandScore("Straight Flush", hands.straightFlush, highCards)
        return HandRating(
            "Straight Flush",
            score,
        )
    }

    if (hands.straight.isNotEmpty()) {
        val score = calculateHandScore("Straight", hands.straight, highCards)
        return HandRating(
            "Straight",
            score,
        )
    }

    if (hands.flush.isNotEmpty()) {
        val score = calculateHandScore("Flush", hands.flush, highCards)
        return HandRating(
            "Flush",
            score,
        )
    }

    if (hands.fourOfAKind.isNotEmpty()) {
        val score = calculateHandScore("Four of a Kind", hands.fourOfAKind, highCards)
        return HandRating(
            "Four of a Kind",
            score,
        )
    }

    if (hands.fullHouse.isNotEmpty()) {
        val score = calculateHandScore(
            "Full House",
            listOf(hands.fullHouse.first().threeOfAKind, hands.fullHouse.first().pair),
            highCards
        )
        return HandRating(
            "Full House",
            score,
        )
    }
    if (hands.threeOfAKind.isNotEmpty()) {
        val score = calculateHandScore("Three of a Kind", hands.threeOfAKind, highCards)
        return HandRating(
            "Three of a Kind",
            score,
        )
    }
    if (hands.pair.size > 1) {
        val score = calculateHandScore("Two Pair", hands.pair, highCards)
        return HandRating(
            "Two Pair",
            score,
        )
    }
    if (hands.pair.isNotEmpty()) {
        val score = calculateHandScore("Pair", hands.pair, highCards)
        return HandRating(
            "Pair",
            score,
        )
    }
    val score = calculateHandScore("High Card", hands.highCard, highCards)
    return HandRating(
        "High Card",
        score,
    )
}

data class FullHouse(
    val threeOfAKind: Int,
    val pair: Int,
)

data class ScoredHands(
    val highCard: List<Int>,
    val pair: List<Int>,
    val threeOfAKind: List<Int>,
    val fullHouse: List<FullHouse>,
    val flush: List<Int>,
    val fourOfAKind: List<Int>,
    val straight: List<Int>,
    val straightFlush: List<Int>,
)

private fun List<Card>.calculateHands(): ScoredHands {
    val cardScores = listOf(0, 14, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)

    val sortedCards = sortedBy { cardScores[it.rank] }

    val rankFrequency = sortedCards.fold(mutableMapOf<Int, Int>()) { map, card ->
        val current = map[card.rank] ?: 0
        map[card.rank] = current + 1
        map
    }

    val suitFrequency = sortedCards.fold(mutableMapOf<String, Int>()) { map, card ->
        val current = map[card.suit] ?: 0
        map[card.suit] = current + 1
        map
    }

    val highCard = sortedCards.map { it.rank }
    val pairCombinations = rankFrequency.calculatePairCombinations()
    val flushes = suitFrequency.calculateFlushes(sortedCards)
    val straights = rankFrequency.calculateStraights()
    val straightFlushes = straights.filter { flushes.contains(it) }

    return ScoredHands(
        highCard,
        pairCombinations.pair,
        pairCombinations.threeOfAKind,
        pairCombinations.fullHouse,
        flushes,
        pairCombinations.fourOfAKind,
        straights,
        straightFlushes
    )
}

private data class PairCombinations(
    val pair: List<Int>,
    val threeOfAKind: List<Int>,
    val fourOfAKind: List<Int>,
    val fullHouse: List<FullHouse>,

    )

private fun Map<Int, Int>.calculatePairCombinations(): PairCombinations {
    val pair = mutableListOf<Int>()
    val threeOfAKind = mutableListOf<Int>()
    val fourOfAKind = mutableListOf<Int>()
    val fullHouse = mutableListOf<FullHouse>()

    forEach { (key, value) ->
        if (pair.isEmpty() && value >= 3) {
            fullHouse.add(
                FullHouse(
                    key,
                    pair.first()
                )
            )
        }
        if (threeOfAKind.isEmpty() && value >= 2) {
            fullHouse.add(
                FullHouse(
                    threeOfAKind.first(),
                    key,
                )
            )
        }
        if (value >= 2) pair.add(key)
        if (value >= 3) threeOfAKind.add(key)
        if (value >= 4) fourOfAKind.add(key)
    }
    return PairCombinations(
        pair,
        threeOfAKind,
        fourOfAKind,
        fullHouse,
    )
}

private fun Map<String, Int>.calculateFlushes(sortedCards: List<Card>): List<Int> {
    val flush = mutableListOf<Int>()
    forEach { (key, value) ->
        if (value > 5) {
            flush.add(sortedCards.first { it.suit == key }.rank)
        }
    }
    return flush
}

private fun Map<Int, Int>.calculateStraights(): List<Int> {
    val straight = mutableListOf<Int>()
    val ranks = mutableListOf<Int>()

    if (containsKey(14)) {
        ranks.add(1)
    }
    ranks.addAll(keys)

    ranks.forEachIndexed { index, rank ->
        if (ranks.size < index + 5) {
            return@forEachIndexed
        }
        if (
            ranks[index + 1] == rank + 1 &&
            ranks[index + 2] == rank + 2 &&
            ranks[index + 3] == rank + 3 &&
            ranks[index + 4] == rank + 4
        ) {
            straight.add(rank + 4)
        }
    }
    return straight
}

private fun calculateHandScore(handStrength: String, handCards: List<Int>, highCards: List<Int>): Double {
    val extraCards = highCards.filterNot { handCards.contains(it) }.toMutableList()
    while (extraCards.size < 4) {
        extraCards.add(0)
    }

    return when (handStrength) {
        "High Card" -> 0.007 * handCards[0] +
                0.00007 * highCards[0] +
                0.0000007 * highCards[1] +
                0.000000007 * highCards[2] +
                0.00000000007 * highCards[3]

        "Pair" -> 0.2 + 0.007 * handCards[0] +
                0.00007 * highCards[0] +
                0.0000007 * highCards[1] +
                0.000000007 * highCards[2]

        "Two Pair" -> 0.3 + 0.007 * handCards[0] +
                0.00007 * handCards[1] +
                0.0000007 * highCards[0]

        "Three of a Kind" -> 0.4 + 0.007 * handCards[0] +
                0.00007 * highCards[0] +
                0.0000007 * highCards[1]

        "Straight" -> 0.5 + 0.007 * handCards[0]
        "Flush" -> 0.6 + 0.007 * handCards[0]
        "Full House" -> 0.7 + 0.007 * handCards[0] + 0.00007 * handCards[1]
        "Four of a Kind" -> 0.8 + 0.007 * handCards[0] + 0.00007 * highCards[1]
        "Straight Flush" -> 0.9 + 0.007 * handCards[0]
        else -> 0.0
    }
}