package com.mealmatch.data.model

/**
 * Represents the price range of a restaurant.
 * @param symbol The string representation (e.g., "$$").
 */
enum class Budget(val symbol: String) {
    ANY("Any"),
    CHEAP("$"),
    MODERATE("$$"),
    EXPENSIVE("$$$"),
    VERY_EXPENSIVE("$$$$")
}
