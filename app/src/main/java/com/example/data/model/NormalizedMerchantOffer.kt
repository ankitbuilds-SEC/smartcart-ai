package com.example.data.model

/**
 * Normalized Merchant Offer Model
 * Represents a standard, verified offer schema across all external merchant feeds.
 * Ensures consistent data representation across Amazon, Flipkart, Croma, Reliance Digital, Apple, Myntra, Tata CliQ, and Ajio.
 * Null values represent unprovided or unavailable metrics; no values are fabricated.
 */
data class NormalizedMerchantOffer(
    val productId: Long,
    val merchantId: Long,
    val merchantName: String,
    val title: String? = null,
    val price: Double,
    val originalPrice: Double? = null,
    val discount: Int? = null,
    val currency: String = "INR",
    val availability: String? = null,
    val deliveryEstimate: String? = null,
    val productUrl: String,
    val affiliateUrl: String? = null,
    val source: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false
)

/**
 * Extension mapper from Room entity to NormalizedMerchantOffer
 */
fun MerchantOfferEntity.toNormalized(productTitle: String? = null): NormalizedMerchantOffer {
    return NormalizedMerchantOffer(
        productId = this.productId,
        merchantId = this.merchantId,
        merchantName = this.merchantName,
        title = productTitle,
        price = this.currentPrice,
        originalPrice = if (this.originalPrice > this.currentPrice) this.originalPrice else null,
        discount = if (this.discountPercent > 0) this.discountPercent else null,
        currency = "INR",
        availability = this.availability.ifBlank { null },
        deliveryEstimate = this.deliveryInfo.ifBlank { null },
        productUrl = this.externalUrl,
        affiliateUrl = this.affiliateUrl.ifBlank { null },
        source = if (this.isLive) "Live Merchant Feed" else "Demo Catalog Integration",
        timestamp = this.lastUpdated,
        isVerified = this.isLive
    )
}
