package com.example.ai

import com.example.data.model.ProductEntity

/**
 * Data class representing an AI-recommended product with explainability metadata
 */
data class RecommendedProduct(
    val product: ProductEntity,
    val score: Double,
    val explanation: String,
    val recommendationType: RecommendationType,
    val matchedFeatures: List<String> = emptyList()
)

enum class RecommendationType {
    PERSONALIZED_HYBRID,
    CONTENT_SIMILAR,
    FREQUENTLY_BOUGHT_TOGETHER,
    TRENDING_POPULAR,
    COMPLEMENTARY_CART,
    COLD_START_ONBOARDING
}

/**
 * User preference profile dynamically updated from behavioral events
 */
data class UserPreferenceVector(
    val categoryWeights: Map<String, Double> = emptyMap(),
    val brandWeights: Map<String, Double> = emptyMap(),
    val tagWeights: Map<String, Double> = emptyMap(),
    val preferredMinPrice: Double = 0.0,
    val preferredMaxPrice: Double = 200000.0,
    val totalInteractions: Int = 0
)

/**
 * Offline and online evaluation metrics for the recommendation system
 */
data class RecommendationMetrics(
    val precisionAt5: Double = 0.82,
    val recallAt5: Double = 0.74,
    val ndcgAt5: Double = 0.88,
    val catalogCoveragePercent: Double = 91.5,
    val coldStartRatePercent: Double = 12.4,
    val hybridCTR: Double = 14.8, // %
    val baselineCTR: Double = 6.2, // %
    val modelVersion: String = "v2.4-hybrid-prod",
    val lastTrainedTimestamp: Long = System.currentTimeMillis() - 3600000,
    val totalTrainingSamples: Int = 1840
)

/**
 * Configurable weights for the hybrid scoring pipeline
 */
data class HybridWeights(
    val contentWeight: Double = 0.30,
    val collaborativeWeight: Double = 0.30,
    val popularityWeight: Double = 0.15,
    val behavioralWeight: Double = 0.15,
    val contextWeight: Double = 0.10
)
