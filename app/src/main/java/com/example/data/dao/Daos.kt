package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE userCode = :userCode LIMIT 1")
    suspend fun getUserByUserCode(userCode: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY popularityScore DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products")
    suspend fun getAllProductsSync(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdSync(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE category = :category ORDER BY popularityScore DESC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE finalPrice BETWEEN :minPrice AND :maxPrice ORDER BY finalPrice ASC")
    fun getProductsByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT brand FROM products")
    fun getAllBrands(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Query("SELECT * FROM products WHERE stock <= :threshold ORDER BY stock ASC")
    fun getLowStockProducts(threshold: Int = 15): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE discountPercent >= :minDiscount ORDER BY discountPercent DESC")
    fun getHighestDiscountProducts(minDiscount: Int = 15): Flow<List<ProductEntity>>
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getCartForUser(userId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getCartItems(userId: Long): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItemsSync(userId: Long): List<CartItemEntity>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: Long, productId: Long): CartItemEntity?

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItemByProduct(userId: Long, productId: Long): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartItemQuantity(id: Long, quantity: Int)

    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeProductFromCart(userId: Long, productId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCartForUser(userId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistForUser(userId: Long): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWishlistItems(userId: Long): Flow<List<WishlistItemEntity>>

    @Query("SELECT productId FROM wishlist_items WHERE userId = :userId")
    fun getWishlistProductIds(userId: Long): Flow<List<Long>>

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getWishlistItem(userId: Long, productId: Long): WishlistItemEntity?

    @Query("SELECT COUNT(*) > 0 FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun isWishlisted(userId: Long, productId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity): Long

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeFromWishlist(userId: Long, productId: Long)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun deleteWishlistItem(userId: Long, productId: Long)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId")
    suspend fun clearWishlistForUser(userId: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun getOrderById(orderId: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items")
    suspend fun getAllOrderItemsSync(): List<OrderItemEntity>

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int

    @Query("SELECT SUM(finalAmount) FROM orders")
    suspend fun getTotalRevenue(): Double?
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentReviews(limit: Int = 20): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("SELECT AVG(rating) FROM reviews WHERE productId = :productId")
    suspend fun getAverageRating(productId: Long): Double?

    @Query("SELECT COUNT(*) FROM reviews WHERE productId = :productId")
    suspend fun getReviewCount(productId: Long): Int

    @Query("SELECT COUNT(*) FROM reviews")
    suspend fun getTotalReviewCount(): Int
}

@Dao
interface UserEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: UserEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<UserEventEntity>)

    @Query("SELECT * FROM user_events WHERE userId = :userId ORDER BY timestamp DESC")
    fun getEventsForUser(userId: Long): Flow<List<UserEventEntity>>

    @Query("SELECT * FROM user_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 100): Flow<List<UserEventEntity>>

    @Query("SELECT * FROM user_events")
    suspend fun getAllEventsSync(): List<UserEventEntity>

    @Query("SELECT * FROM user_events WHERE userId = :userId")
    suspend fun getEventsForUserSync(userId: Long): List<UserEventEntity>

    @Query("DELETE FROM user_events WHERE userId = :userId")
    suspend fun clearEventsForUser(userId: Long)

    @Query("SELECT COUNT(*) FROM user_events")
    suspend fun getEventCount(): Int
}

@Dao
interface AIExperimentDao {
    @Query("SELECT * FROM ai_experiments WHERE isActive = 1 LIMIT 1")
    fun getActiveExperiment(): Flow<AIExperimentEntity?>

    @Query("SELECT * FROM ai_experiments WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveExperimentSync(): AIExperimentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(exp: AIExperimentEntity): Long

    @Update
    suspend fun updateExperiment(exp: AIExperimentEntity)

    @Query("UPDATE ai_experiments SET impressionsA = impressionsA + 1 WHERE id = :id")
    suspend fun recordImpressionA(id: Long)

    @Query("UPDATE ai_experiments SET impressionsB = impressionsB + 1 WHERE id = :id")
    suspend fun recordImpressionB(id: Long)

    @Query("UPDATE ai_experiments SET clicksA = clicksA + 1 WHERE id = :id")
    suspend fun recordClickA(id: Long)

    @Query("UPDATE ai_experiments SET clicksB = clicksB + 1 WHERE id = :id")
    suspend fun recordClickB(id: Long)

    @Query("UPDATE ai_experiments SET conversionsA = conversionsA + 1, revenueA = revenueA + :revenue WHERE id = :id")
    suspend fun recordConversionA(id: Long, revenue: Double)

    @Query("UPDATE ai_experiments SET conversionsB = conversionsB + 1, revenueB = revenueB + :revenue WHERE id = :id")
    suspend fun recordConversionB(id: Long, revenue: Double)
}

@Dao
interface AuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 50): Flow<List<AuditLogEntity>>
}

// --- NEW MARKETPLACE & DEAL DAOS ---

@Dao
interface MerchantDao {
    @Query("SELECT * FROM merchants ORDER BY name ASC")
    fun getAllMerchants(): Flow<List<MerchantEntity>>

    @Query("SELECT * FROM merchants WHERE status = 'ACTIVE' ORDER BY name ASC")
    fun getActiveMerchants(): Flow<List<MerchantEntity>>

    @Query("SELECT * FROM merchants")
    suspend fun getAllMerchantsSync(): List<MerchantEntity>

    @Query("SELECT * FROM merchants WHERE id = :id LIMIT 1")
    suspend fun getMerchantById(id: Long): MerchantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchant(merchant: MerchantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerchants(merchants: List<MerchantEntity>)

    @Update
    suspend fun updateMerchant(merchant: MerchantEntity)
}

@Dao
interface MerchantOfferDao {
    @Query("SELECT * FROM merchant_offers WHERE productId = :productId ORDER BY currentPrice ASC")
    fun getOffersForProduct(productId: Long): Flow<List<MerchantOfferEntity>>

    @Query("SELECT * FROM merchant_offers WHERE productId = :productId ORDER BY currentPrice ASC")
    suspend fun getOffersForProductSync(productId: Long): List<MerchantOfferEntity>

    @Query("SELECT * FROM merchant_offers ORDER BY lastUpdated DESC")
    fun getAllOffers(): Flow<List<MerchantOfferEntity>>

    @Query("SELECT * FROM merchant_offers ORDER BY discountPercent DESC LIMIT :limit")
    fun getTopDiscountOffers(limit: Int = 20): Flow<List<MerchantOfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: MerchantOfferEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<MerchantOfferEntity>)

    @Update
    suspend fun updateOffer(offer: MerchantOfferEntity)

    @Query("DELETE FROM merchant_offers WHERE id = :id")
    suspend fun deleteOffer(id: Long)

    @Query("DELETE FROM merchant_offers WHERE productId = :productId")
    suspend fun deleteOffersForProduct(productId: Long)
}

@Dao
interface PriceHistoryDao {
    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY recordedAt ASC")
    fun getPriceHistoryForProduct(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY recordedAt ASC")
    suspend fun getPriceHistorySync(productId: Long): List<PriceHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricePoint(point: PriceHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(points: List<PriceHistoryEntity>)
}

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAlertsForUser(userId: Long): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getAlert(userId: Long, productId: Long): PriceAlertEntity?

    @Query("SELECT * FROM price_alerts WHERE isTriggered = 0")
    suspend fun getActiveAlertsSync(): List<PriceAlertEntity>

    @Query("SELECT * FROM price_alerts WHERE productId = :productId AND isTriggered = 0")
    suspend fun getActiveAlertsForProduct(productId: Long): List<PriceAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Update
    suspend fun updateAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)

    @Query("DELETE FROM price_alerts WHERE userId = :userId AND productId = :productId")
    suspend fun removeAlertForProduct(userId: Long, productId: Long)
}

@Dao
interface PromoCampaignDao {
    @Query("SELECT * FROM promo_campaigns WHERE status = 'ACTIVE' ORDER BY discountValue DESC")
    fun getActiveCampaigns(): Flow<List<PromoCampaignEntity>>

    @Query("SELECT * FROM promo_campaigns ORDER BY startDate DESC")
    fun getAllCampaigns(): Flow<List<PromoCampaignEntity>>

    @Query("SELECT * FROM promo_campaigns WHERE promoCode = :code AND status = 'ACTIVE' LIMIT 1")
    suspend fun getCampaignByCode(code: String): PromoCampaignEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: PromoCampaignEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(campaigns: List<PromoCampaignEntity>)

    @Update
    suspend fun updateCampaign(campaign: PromoCampaignEntity)
}

@Dao
interface MerchantCouponDao {
    @Query("SELECT * FROM merchant_coupons ORDER BY verifiedAt DESC")
    fun getAllCoupons(): Flow<List<MerchantCouponEntity>>

    @Query("SELECT * FROM merchant_coupons WHERE merchantId = :merchantId")
    fun getCouponsForMerchant(merchantId: Long): Flow<List<MerchantCouponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: MerchantCouponEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<MerchantCouponEntity>)
}

@Dao
interface VoucherListingDao {
    @Query("SELECT * FROM vouchers WHERE status = 'VERIFIED' ORDER BY createdAt DESC")
    fun getVerifiedVouchers(): Flow<List<VoucherListingEntity>>

    @Query("SELECT * FROM vouchers WHERE sellerUserId = :sellerId ORDER BY createdAt DESC")
    fun getVouchersForSeller(sellerId: Long): Flow<List<VoucherListingEntity>>

    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    fun getAllVouchers(): Flow<List<VoucherListingEntity>>

    @Query("SELECT * FROM vouchers WHERE status = 'PENDING_VERIFICATION' ORDER BY createdAt ASC")
    fun getPendingVerificationVouchers(): Flow<List<VoucherListingEntity>>

    @Query("SELECT * FROM vouchers WHERE id = :id LIMIT 1")
    suspend fun getVoucherById(id: Long): VoucherListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherListingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVouchers(vouchers: List<VoucherListingEntity>)

    @Update
    suspend fun updateVoucher(voucher: VoucherListingEntity)

    @Query("UPDATE vouchers SET status = :newStatus, rejectionReason = :reason, verifiedAt = :verifiedAt WHERE id = :id")
    suspend fun updateVoucherStatus(id: Long, newStatus: String, reason: String = "", verifiedAt: Long? = System.currentTimeMillis())
}

@Dao
interface VoucherTransactionDao {
    @Query("SELECT * FROM voucher_transactions WHERE buyerUserId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForBuyer(userId: Long): Flow<List<VoucherTransactionEntity>>

    @Query("SELECT * FROM voucher_transactions WHERE sellerUserId = :userId ORDER BY createdAt DESC")
    fun getTransactionsForSeller(userId: Long): Flow<List<VoucherTransactionEntity>>

    @Query("SELECT * FROM voucher_transactions ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<VoucherTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: VoucherTransactionEntity): Long
}

@Dao
interface ExternalClickDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClick(click: ExternalClickEntity): Long

    @Query("SELECT * FROM external_clicks ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentClicks(limit: Int = 100): Flow<List<ExternalClickEntity>>

    @Query("SELECT * FROM external_clicks WHERE userId = :userId ORDER BY timestamp DESC")
    fun getClicksForUser(userId: Long): Flow<List<ExternalClickEntity>>

    @Query("SELECT COUNT(*) FROM external_clicks")
    suspend fun getTotalClicksCount(): Int

    @Query("SELECT COUNT(*) FROM external_clicks WHERE merchantId = :merchantId")
    suspend fun getClicksCountForMerchant(merchantId: Long): Int
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsReadForUser(userId: Long)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAllNotificationsForUser(userId: Long)
}
