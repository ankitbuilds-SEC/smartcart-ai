package com.example.util

import java.security.MessageDigest
import java.util.Locale

/**
 * Security & Data Integrity Utilities for SmartCart AI
 */
object SecurityUtils {

    /**
     * SHA-256 Hashing for user passwords
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return hashPassword(password).equals(hash, ignoreCase = true)
    }

    /**
     * Masks voucher codes and sensitive redemption identifiers
     */
    fun maskVoucherCode(code: String): String {
        val trimmed = code.trim()
        if (trimmed.length <= 4) return "****"
        val prefix = trimmed.take(3)
        val suffix = trimmed.takeLast(4)
        return "$prefix-****-$suffix"
    }

    /**
     * Server-side verified discount percentage calculation
     * formula: discount = (original_price - current_price) / original_price * 100
     */
    fun calculateVerifiedDiscount(originalPrice: Double, currentPrice: Double): Int {
        if (originalPrice <= 0.0 || originalPrice <= currentPrice) return 0
        val discount = ((originalPrice - currentPrice) / originalPrice) * 100.0
        return discount.toInt().coerceIn(0, 99)
    }

    /**
     * Multi-signal Deal Scoring logic:
     * Deal Score = discount strength + price drop + historical comparison + merchant confidence
     */
    fun calculateDealScore(
        discountPercent: Int,
        priceDropAmount: Double,
        isLowestIn30Days: Boolean,
        merchantRating: Double
    ): Double {
        val discountStrength = (discountPercent * 0.45).coerceIn(0.0, 45.0)
        val priceDropScore = ((priceDropAmount / 1500.0) * 20.0).coerceIn(0.0, 20.0)
        val historicalBonus = if (isLowestIn30Days) 20.0 else 5.0
        val merchantConfidence = ((merchantRating / 5.0) * 15.0).coerceIn(0.0, 15.0)

        return (discountStrength + priceDropScore + historicalBonus + merchantConfidence).coerceIn(0.0, 100.0)
    }
}

/**
 * Structured Search Filters parsed from Natural Language queries
 */
data class StructuredSearchFilter(
    val category: String? = null,
    val subcategory: String? = null,
    val brand: String? = null,
    val color: String? = null,
    val maxPrice: Double? = null,
    val minPrice: Double? = null,
    val targetAudience: String? = null,
    val keywords: List<String> = emptyList()
) {
    companion object {
        fun parseFromNaturalLanguage(query: String): StructuredSearchFilter {
            val qLower = query.lowercase(Locale.ROOT)

            // 1. Budget extraction
            var maxPrice: Double? = null
            val underRegex = Regex("(?:under|below|less than|within|max)\\s*(?:₹|rs\\.?)?\\s*([0-9]+(?:,[0-9]+)*)")
            val match = underRegex.find(qLower)
            if (match != null) {
                maxPrice = match.groupValues[1].replace(",", "").toDoubleOrNull()
            }

            // 2. Category & Subcategory mapping
            var category: String? = null
            var subcategory: String? = null
            when {
                qLower.contains("shoe") || qLower.contains("sneaker") -> {
                    category = "Fashion"
                    subcategory = "Footwear"
                }
                qLower.contains("shirt") || qLower.contains("jeans") || qLower.contains("jacket") || qLower.contains("hoodie") -> {
                    category = "Fashion"
                    subcategory = "Apparel"
                }
                qLower.contains("headphone") || qLower.contains("earbud") || qLower.contains("audio") || qLower.contains("earphones") -> {
                    category = "Audio & Wearables"
                }
                qLower.contains("phone") || qLower.contains("smartphone") || qLower.contains("mobile") -> {
                    category = "Smartphones"
                }
                qLower.contains("toy") || qLower.contains("lego") || qLower.contains("board game") || qLower.contains("robot") || qLower.contains("10 year old") || qLower.contains("kid") -> {
                    category = "Toys"
                }
                qLower.contains("beauty") || qLower.contains("serum") || qLower.contains("perfume") || qLower.contains("skincare") -> {
                    category = "Beauty"
                }
                qLower.contains("book") || qLower.contains("novel") -> {
                    category = "Books"
                }
                qLower.contains("grocery") || qLower.contains("coffee") || qLower.contains("tea") -> {
                    category = "Grocery"
                }
                qLower.contains("laptop") || qLower.contains("computer") -> {
                    category = "Electronics"
                    subcategory = "Laptops"
                }
                qLower.contains("gaming") || qLower.contains("ps5") || qLower.contains("console") -> {
                    category = "Gaming"
                }
            }

            // 3. Color extraction
            val colors = listOf("black", "white", "blue", "red", "green", "silver", "gold", "grey", "gray")
            val color = colors.firstOrNull { qLower.contains(it) }

            // 4. Brand extraction
            val brands = listOf("apple", "samsung", "sony", "nike", "adidas", "bose", "dell", "asus", "levis", "lego", "nestle")
            val brand = brands.firstOrNull { qLower.contains(it) }

            // 5. Target Audience
            val targetAudience = if (qLower.contains("10 year old") || qLower.contains("kid") || qLower.contains("child")) {
                "Kids / 10 Year Old"
            } else if (qLower.contains("interview") || qLower.contains("formal") || qLower.contains("office")) {
                "Formal / Professional"
            } else null

            // 6. Keywords
            val keywords = qLower.split(" ").filter { it.length > 3 && !it.startsWith("under") && !it.startsWith("₹") }

            return StructuredSearchFilter(
                category = category,
                subcategory = subcategory,
                brand = brand,
                color = color,
                maxPrice = maxPrice,
                targetAudience = targetAudience,
                keywords = keywords
            )
        }
    }
}
