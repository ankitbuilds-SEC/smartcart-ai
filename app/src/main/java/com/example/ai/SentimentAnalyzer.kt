package com.example.ai

import java.util.Locale

object SentimentAnalyzer {

    private val positiveWords = setOf(
        "excellent", "amazing", "great", "fantastic", "awesome", "love", "best", "perfect",
        "superb", "durable", "premium", "fast", "reliable", "worth", "smooth", "crisp",
        "stunning", "impressive", "high quality", "recommend", "happy", "flawless", "clear"
    )

    private val negativeWords = setOf(
        "terrible", "bad", "horrible", "awful", "worst", "poor", "broken", "cheap",
        "slow", "lag", "defective", "waste", "disappointed", "faulty", "useless",
        "overpriced", "scam", "fragile", "noisy", "fails", "drains"
    )

    data class SentimentResult(
        val sentiment: String, // "Positive", "Neutral", "Negative"
        val score: Double // 0.0 (very negative) to 1.0 (very positive)
    )

    fun analyze(text: String, rating: Int): SentimentResult {
        val lower = text.lowercase(Locale.ROOT)
        var posCount = 0
        var negCount = 0

        positiveWords.forEach { word ->
            if (lower.contains(word)) posCount++
        }
        negativeWords.forEach { word ->
            if (lower.contains(word)) negCount++
        }

        // Combine text sentiment with star rating (1-5 normalized to 0.0-1.0)
        val ratingScore = (rating.coerceIn(1, 5) - 1.0) / 4.0
        val textScore = when {
            posCount + negCount == 0 -> ratingScore
            else -> posCount.toDouble() / (posCount + negCount).toDouble()
        }

        val blendedScore = (0.6 * textScore + 0.4 * ratingScore).coerceIn(0.0, 1.0)

        val sentiment = when {
            blendedScore >= 0.65 -> "Positive"
            blendedScore <= 0.35 -> "Negative"
            else -> "Neutral"
        }

        return SentimentResult(sentiment = sentiment, score = blendedScore)
    }
}
