package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["userCode"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userCode: String = "", // e.g. "SC-USER-000001"
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val phone: String = "",
    val role: String = "customer", // "customer" or "admin"
    val preferredCategories: String = "", // Comma-delimited list of categories
    val preferredBrands: String = "", // Comma-delimited list of brands
    val preferredMinPrice: Double = 0.0,
    val preferredMaxPrice: Double = 200000.0,
    val riskStatus: String = "NORMAL", // "NORMAL", "REVIEW", "BLOCKED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["category"]),
        Index(value = ["brand"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val name: String,
    val slug: String,
    val description: String,
    val brand: String,
    val category: String,
    val subcategory: String = "",
    val price: Double,
    val discountPercent: Int = 0,
    val finalPrice: Double,
    val stock: Int = 50,
    val rating: Double = 4.5,
    val reviewCount: Int = 0,
    val imageUrl: String = "",
    val tags: String = "", // Comma-delimited keywords
    val specifications: String = "", // Key: Value pairs
    val popularityScore: Double = 50.0,
    val isFeatured: Boolean = false,
    val dataSource: String = "DEMO", // "DEMO", "LIVE", "CACHED"
    val providerName: String = "Local Catalog",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "cart_items",
    indices = [Index(value = ["userId", "productId"], unique = true)]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "wishlist_items",
    indices = [Index(value = ["userId", "productId"], unique = true)]
)
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "orders",
    indices = [Index(value = ["userId"]), Index(value = ["orderNumber"], unique = true)]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val userId: Long,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val finalAmount: Double,
    val status: String = "Confirmed", // "Confirmed", "Processing", "Shipped", "Out for Delivery", "Delivered", "Cancelled"
    val shippingAddress: String,
    val paymentMethod: String = "Simulated Card / UPI",
    val paymentStatus: String = "Paid",
    val trackingNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_items",
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

@Entity(
    tableName = "reviews",
    indices = [Index(value = ["productId"])]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val userId: Long,
    val userName: String,
    val rating: Int,
    val reviewText: String,
    val sentiment: String = "Positive", // "Positive", "Neutral", "Negative"
    val sentimentScore: Double = 0.85,
    val helpfulVotes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_events",
    indices = [Index(value = ["userId"]), Index(value = ["productId"]), Index(value = ["eventType"])]
)
data class UserEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long? = null,
    val eventType: String, // "view", "search", "click", "wishlist", "cart_add", "cart_remove", "purchase", "rate", "merchant_click", "deal_view", "voucher_view"
    val metadata: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_experiments")
data class AIExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentName: String,
    val variantA: String, // e.g. "Popularity Baseline"
    val variantB: String, // e.g. "Hybrid AI Engine"
    val impressionsA: Int = 0,
    val impressionsB: Int = 0,
    val clicksA: Int = 0,
    val clicksB: Int = 0,
    val conversionsA: Int = 0,
    val conversionsB: Int = 0,
    val revenueA: Double = 0.0,
    val revenueB: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorEmail: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- ADVANCED MARKETPLACE & DEAL INTELLIGENCE ENTITIES ---

@Entity(
    tableName = "merchants",
    indices = [Index(value = ["slug"], unique = true)]
)
data class MerchantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val slug: String,
    val logoUrl: String = "",
    val websiteUrl: String,
    val affiliateParam: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val isAuthorized: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "merchant_offers",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["merchantId"]),
        Index(value = ["productId", "merchantId"], unique = true)
    ]
)
data class MerchantOfferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val productId: Long,
    val merchantName: String,
    val currentPrice: Double,
    val originalPrice: Double,
    val discountPercent: Int,
    val couponCode: String = "",
    val couponDiscount: String = "",
    val externalUrl: String,
    val affiliateUrl: String = "",
    val availability: String = "In Stock",
    val deliveryInfo: String = "Free Delivery in 2-4 days",
    val isLive: Boolean = false, // false = Demo / cached seed data, true = Live verified provider
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "price_history",
    indices = [Index(value = ["productId"]), Index(value = ["recordedAt"])]
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val merchantName: String,
    val price: Double,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "price_alerts",
    indices = [Index(value = ["userId", "productId"])]
)
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val productName: String,
    val productCategory: String,
    val targetPrice: Double,
    val currentPrice: Double,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long? = null
)

@Entity(
    tableName = "promo_campaigns",
    indices = [Index(value = ["promoCode"], unique = true)]
)
data class PromoCampaignEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val promoCode: String,
    val campaignName: String,
    val discountType: String = "PERCENTAGE", // "PERCENTAGE", "FIXED"
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscount: Double = 500.0,
    val startDate: Long = System.currentTimeMillis(),
    val expiryDate: Long = System.currentTimeMillis() + 30L * 86400000L,
    val usageLimit: Int = 1000,
    val perUserLimit: Int = 1,
    val applicableCategories: String = "ALL",
    val status: String = "ACTIVE" // "ACTIVE", "EXPIRED", "PAUSED"
)

@Entity(
    tableName = "merchant_coupons",
    indices = [Index(value = ["merchantId"]), Index(value = ["code"])]
)
data class MerchantCouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantId: Long,
    val merchantName: String,
    val code: String,
    val discountDescription: String,
    val conditions: String,
    val expiryDate: String,
    val source: String = "Verified Merchant Feed",
    val verifiedAt: Long = System.currentTimeMillis(),
    val externalUrl: String = ""
)

@Entity(
    tableName = "vouchers",
    indices = [Index(value = ["sellerUserId"]), Index(value = ["status"])]
)
data class VoucherListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sellerUserId: Long,
    val sellerName: String,
    val sellerEmail: String,
    val brand: String,
    val voucherType: String = "Gift Card", // "Gift Card", "Store Voucher", "Promo Credit"
    val faceValue: Double,
    val sellingPrice: Double,
    val discountPercent: Int = 0,
    val expiryDate: String,
    val terms: String,
    val verificationInfo: String, // Masked identifier or reference number
    val quantity: Int = 1,
    val status: String = "PENDING_VERIFICATION", // "PENDING_VERIFICATION", "VERIFIED", "REJECTED", "EXPIRED", "SOLD", "CANCELLED"
    val rejectionReason: String = "",
    val sellerConfirmationAgreed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

@Entity(
    tableName = "voucher_transactions",
    indices = [Index(value = ["buyerUserId"]), Index(value = ["sellerUserId"])]
)
data class VoucherTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherId: Long,
    val buyerUserId: Long,
    val buyerName: String,
    val sellerUserId: Long,
    val brand: String,
    val amountPaid: Double,
    val paymentMethod: String = "Sandbox Card / UPI",
    val paymentStatus: String = "COMPLETED",
    val transactionToken: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "external_clicks",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["productId"]),
        Index(value = ["merchantId"]),
        Index(value = ["timestamp"])
    ]
)
data class ExternalClickEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val productId: Long,
    val offerId: Long,
    val merchantId: Long,
    val merchantName: String,
    val productTitle: String,
    val destinationUrl: String,
    val sourcePage: String, // "product_detail", "deals_page", "ai_assistant", "search"
    val campaignId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["userId"]), Index(value = ["isRead"])]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String = "SYSTEM", // "PRICE_DROP", "VOUCHER_STATUS", "DEAL", "PROMO", "SYSTEM"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
