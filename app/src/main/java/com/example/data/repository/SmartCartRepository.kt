package com.example.data.repository

import com.example.ai.HybridRecommendationEngine
import com.example.ai.RecommendationMetrics
import com.example.ai.RecommendedProduct
import com.example.ai.SentimentAnalyzer
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.provider.MerchantProviderRegistry
import com.example.data.seed.SeedData
import com.example.util.MerchantUrlSecurityValidator
import com.example.util.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class CartItemWithProduct(
    val cartItem: CartItemEntity,
    val product: ProductEntity
)

data class PromoValidationResult(
    val isValid: Boolean,
    val discountAmount: Double,
    val message: String,
    val campaign: PromoCampaignEntity? = null
)

class SmartCartRepository(
    private val database: AppDatabase,
    private val recommendationEngine: HybridRecommendationEngine = HybridRecommendationEngine()
) {
    val userDao = database.userDao()
    val productDao = database.productDao()
    val cartDao = database.cartDao()
    val wishlistDao = database.wishlistDao()
    val orderDao = database.orderDao()
    val reviewDao = database.reviewDao()
    val userEventDao = database.userEventDao()
    val aiExperimentDao = database.aiExperimentDao()
    val auditLogDao = database.auditLogDao()

    // Marketplace DAOs
    val merchantDao = database.merchantDao()
    val merchantOfferDao = database.merchantOfferDao()
    val priceHistoryDao = database.priceHistoryDao()
    val priceAlertDao = database.priceAlertDao()
    val promoCampaignDao = database.promoCampaignDao()
    val merchantCouponDao = database.merchantCouponDao()
    val voucherListingDao = database.voucherListingDao()
    val voucherTransactionDao = database.voucherTransactionDao()
    val externalClickDao = database.externalClickDao()
    val notificationDao = database.notificationDao()

    val merchantRegistry = MerchantProviderRegistry()
    private val voucherPurchaseMutex = Mutex()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _aiMetrics = MutableStateFlow(recommendationEngine.metrics)
    val aiMetrics: StateFlow<RecommendationMetrics> = _aiMetrics.asStateFlow()

    val seedingJob: Job = CoroutineScope(Dispatchers.IO).launch {
        checkAndSeedDatabase()
    }

    private suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val userCount = userDao.getUserCount()
        if (userCount == 0) {
            // 1. Seed demo users
            val users = SeedData.getDemoUsers()
            userDao.insertUsers(users)

            // 2. Seed multi-category products
            val products = SeedData.getProducts()
            productDao.insertProducts(products)

            // 3. Seed user events
            val events = SeedData.getInitialEvents()
            userEventDao.insertEvents(events)

            // 4. Seed reviews
            val reviews = SeedData.getInitialReviews()
            reviewDao.insertReviews(reviews)

            // 5. Seed orders and items
            val (orders, items) = SeedData.getInitialOrders()
            for (order in orders) {
                orderDao.insertOrder(order)
            }
            orderDao.insertOrderItems(items)

            // 6. Seed A/B experiment
            aiExperimentDao.insertExperiment(SeedData.getInitialExperiment())

            // 7. Seed Merchants & Offers
            val merchants = SeedData.getDemoMerchants()
            merchantDao.insertMerchants(merchants)

            val offers = SeedData.getDemoOffers(products)
            merchantOfferDao.insertOffers(offers)

            // 8. Seed Price History
            val pricePoints = SeedData.getDemoPriceHistory(products)
            priceHistoryDao.insertPriceHistory(pricePoints)

            // 9. Seed Promo Campaigns & Merchant Coupons
            promoCampaignDao.insertCampaigns(SeedData.getDemoPromoCampaigns())
            merchantCouponDao.insertCoupons(SeedData.getDemoMerchantCoupons())

            // 10. Seed Vouchers
            voucherListingDao.insertVouchers(SeedData.getDemoVouchers())

            // 11. Seed Notifications
            for (notif in SeedData.getDemoNotifications()) {
                notificationDao.insertNotification(notif)
            }

            // 12. Initial model training
            val updatedMetrics = recommendationEngine.retrainModel(products, events, items)
            _aiMetrics.value = updatedMetrics

            // 13. Audit log
            auditLogDao.insertLog(
                AuditLogEntity(
                    actorEmail = "system@smartcart.ai",
                    action = "INITIAL_SEED_AND_TRAIN",
                    details = "Initialized catalog with ${products.size} products across 15 categories, 7 merchants, promo campaigns and trained hybrid recommendation model."
                )
            )

            // Set default user to demo customer
            _currentUser.value = users.firstOrNull { it.role == "customer" }
        } else {
            // Set default current user
            val demoUser = userDao.getUserByEmail("demo.customer@example.com")
            _currentUser.value = demoUser
            retrainRecommendationModel()
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
    }

    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    // --- User Registration & Profile Management ---

    suspend fun registerUserAccount(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        preferredCategories: String,
        preferredBrands: String,
        minPrice: Double,
        maxPrice: Double
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return@withContext Result.failure(Exception("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return@withContext Result.failure(Exception("Password must be at least 6 characters long."))
        }

        val existing = userDao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return@withContext Result.failure(Exception("An account with this email already exists."))
        }

        val count = userDao.getUserCount() + 1
        var userCode = "SC-USER-%06d".format(count)
        if (userDao.getUserByUserCode(userCode) != null) {
            userCode = "SC-USER-%06d-%03d".format(count, (100..999).random())
        }
        val hashedPassword = SecurityUtils.hashPassword(password)

        val newUser = UserEntity(
            userCode = userCode,
            email = cleanEmail,
            passwordHash = hashedPassword,
            fullName = fullName.trim(),
            phone = phone.trim(),
            role = "customer",
            preferredCategories = preferredCategories,
            preferredBrands = preferredBrands,
            preferredMinPrice = minPrice,
            preferredMaxPrice = maxPrice,
            riskStatus = "NORMAL"
        )

        val id = userDao.insertUser(newUser)
        val created = newUser.copy(id = id)
        _currentUser.value = created

        // Send welcome notification
        notificationDao.insertNotification(
            NotificationEntity(
                userId = id,
                title = "Welcome to Shop Mind AI!",
                message = "Your personalized smart shopping assistant is ready with your ID: $userCode. Use promo code SMART10 for 10% off.",
                type = "PROMO"
            )
        )

        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = created.email,
                action = "USER_REGISTERED",
                details = "User registered with Code: $userCode and preferences: $preferredCategories"
            )
        )

        Result.success(created)
    }

    suspend fun loginUser(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return@withContext Result.failure(Exception("User with this email not found."))

        val hashedInput = SecurityUtils.hashPassword(password)
        val matches = user.passwordHash == hashedInput || user.passwordHash == password
        if (!matches) {
            return@withContext Result.failure(Exception("Incorrect password. Please try again."))
        }

        _currentUser.value = user
        Result.success(user)
    }

    suspend fun updateUserPreferences(categories: String, minPrice: Double, maxPrice: Double) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val updated = user.copy(
            preferredCategories = categories,
            preferredMinPrice = minPrice,
            preferredMaxPrice = maxPrice
        )
        userDao.updateUser(updated)
        _currentUser.value = updated
    }

    suspend fun updateUserProfile(
        fullName: String,
        phone: String,
        preferredCategories: String,
        preferredBrands: String,
        minPrice: Double,
        maxPrice: Double
    ) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val updated = user.copy(
            fullName = fullName,
            phone = phone,
            preferredCategories = preferredCategories,
            preferredBrands = preferredBrands,
            preferredMinPrice = minPrice,
            preferredMaxPrice = maxPrice
        )
        userDao.updateUser(updated)
        _currentUser.value = updated
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in."))
        if (user.passwordHash != oldPass) {
            return@withContext Result.failure(Exception("Current password does not match."))
        }
        val updated = user.copy(passwordHash = newPass)
        userDao.updateUser(updated)
        _currentUser.value = updated
        Result.success(Unit)
    }

    suspend fun deleteUserAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext Result.failure(Exception("Not logged in."))
        userDao.deleteUserById(user.id)
        cartDao.clearCartForUser(user.id)
        wishlistDao.clearWishlistForUser(user.id)
        userEventDao.clearEventsForUser(user.id)

        // Switch back to demo customer
        val demo = userDao.getUserByEmail("demo.customer@example.com")
        _currentUser.value = demo
        Result.success(Unit)
    }

    // --- Product & Catalog Operations ---

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun getProductByIdSync(id: Long): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.getProductByIdSync(id)
    }

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(category)

    fun searchProducts(query: String): Flow<List<ProductEntity>> =
        productDao.searchProducts(query)

    fun getAllCategories(): Flow<List<String>> = productDao.getAllCategories()

    fun getAllBrands(): Flow<List<String>> = productDao.getAllBrands()

    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts(15)

    fun getHighestDiscountProducts(minDiscount: Int = 15): Flow<List<ProductEntity>> =
        productDao.getHighestDiscountProducts(minDiscount)

    // --- AI Recommendations & Personalization ---

    suspend fun getPersonalizedRecommendations(limit: Int = 10): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsSync()
        val user = _currentUser.value
        val events = if (user != null) userEventDao.getEventsForUserSync(user.id) else emptyList()
        recommendationEngine.getPersonalizedRecommendations(user, products, events, limit)
    }

    suspend fun getSimilarProducts(referenceProduct: ProductEntity, limit: Int = 6): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsSync()
        recommendationEngine.getSimilarProducts(referenceProduct, products, limit)
    }

    suspend fun getFrequentlyBoughtTogether(referenceProduct: ProductEntity, limit: Int = 4): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsSync()
        recommendationEngine.getFrequentlyBoughtTogether(referenceProduct, products, limit)
    }

    suspend fun getTrendingProducts(limit: Int = 8): List<RecommendedProduct> = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsSync()
        recommendationEngine.getTrendingProducts(products, limit)
    }

    suspend fun retrainRecommendationModel(): RecommendationMetrics = withContext(Dispatchers.IO) {
        val products = productDao.getAllProductsSync()
        val events = userEventDao.getAllEventsSync()
        val orderItems = orderDao.getAllOrderItemsSync()
        val metrics = recommendationEngine.retrainModel(products, events, orderItems)
        _aiMetrics.value = metrics

        val email = _currentUser.value?.email ?: "admin@smartcart.ai"
        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = email,
                action = "RETRAIN_MODEL",
                details = "Recommendation model retrained (${metrics.modelVersion}) with ${metrics.totalTrainingSamples} data points. NDCG@5: ${metrics.ndcgAt5}."
            )
        )
        metrics
    }

    // --- User Events & Telemetry ---

    suspend fun logUserEvent(eventType: String, productId: Long? = null, metadata: String = "") = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: 1L
        userEventDao.insertEvent(
            UserEventEntity(
                userId = userId,
                productId = productId,
                eventType = eventType,
                metadata = metadata,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearUserInteractionHistory() = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        userEventDao.clearEventsForUser(user.id)
    }

    fun getRecentEvents(limit: Int = 50): Flow<List<UserEventEntity>> = userEventDao.getRecentEvents(limit)

    // --- Cart & Wishlist ---

    fun getCartItems(userId: Long): Flow<List<CartItemEntity>> = cartDao.getCartItems(userId)

    suspend fun addToCart(productId: Long, quantity: Int = 1) = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: 1L
        val existing = cartDao.getCartItemByProduct(userId, productId)
        if (existing != null) {
            cartDao.updateCartItemQuantity(existing.id, existing.quantity + quantity)
        } else {
            cartDao.insertCartItem(
                CartItemEntity(userId = userId, productId = productId, quantity = quantity)
            )
        }
        logUserEvent("cart_add", productId, "quantity: $quantity")
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(cartItemId)
        } else {
            cartDao.updateCartItemQuantity(cartItemId, quantity)
        }
    }

    suspend fun removeFromCart(cartItemId: Long, productId: Long? = null) = withContext(Dispatchers.IO) {
        cartDao.deleteCartItem(cartItemId)
        if (productId != null) {
            logUserEvent("cart_remove", productId)
        }
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: return@withContext
        cartDao.clearCart(userId)
    }

    fun getWishlistItems(userId: Long): Flow<List<WishlistItemEntity>> = wishlistDao.getWishlistItems(userId)

    fun getWishlistProductIds(userId: Long): Flow<List<Long>> = wishlistDao.getWishlistProductIds(userId)

    suspend fun isWishlisted(productId: Long): Boolean = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: return@withContext false
        wishlistDao.isWishlisted(userId, productId)
    }

    suspend fun toggleWishlist(productId: Long): Boolean = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: 1L
        val exists = wishlistDao.isWishlisted(userId, productId)
        if (exists) {
            wishlistDao.deleteWishlistItem(userId, productId)
            false
        } else {
            wishlistDao.insertWishlistItem(
                WishlistItemEntity(userId = userId, productId = productId)
            )
            logUserEvent("wishlist", productId)
            true
        }
    }

    // --- Orders & Checkout ---

    fun getOrdersForUser(userId: Long): Flow<List<OrderEntity>> = orderDao.getOrdersForUser(userId)

    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = orderDao.getOrderItems(orderId)

    suspend fun placeOrder(
        shippingAddress: String,
        paymentMethod: String,
        discountCode: String = ""
    ): OrderEntity = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: throw IllegalStateException("Not logged in")
        val cartItems = cartDao.getCartItemsSync(user.id)
        if (cartItems.isEmpty()) throw IllegalStateException("Cart is empty")

        var total = 0.0
        val orderItemsToInsert = mutableListOf<OrderItemEntity>()

        for (item in cartItems) {
            val product = productDao.getProductByIdSync(item.productId) ?: continue
            val subtotal = product.finalPrice * item.quantity
            total += subtotal

            // Decrement stock safely
            val newStock = (product.stock - item.quantity).coerceAtLeast(0)
            productDao.updateProduct(product.copy(stock = newStock))

            orderItemsToInsert.add(
                OrderItemEntity(
                    orderId = 0,
                    productId = product.id,
                    productName = product.name,
                    productPrice = product.finalPrice,
                    quantity = item.quantity,
                    subtotal = subtotal
                )
            )
            logUserEvent("purchase", product.id, "order_item")
        }

        // Validate promo code
        val promoResult = validatePromoCode(discountCode, total)
        val discount = if (promoResult.isValid) promoResult.discountAmount else 0.0
        val finalAmount = (total - discount).coerceAtLeast(0.0)
        val orderNumber = "ORD-${System.currentTimeMillis() % 1000000}"

        val order = OrderEntity(
            orderNumber = orderNumber,
            userId = user.id,
            totalAmount = total,
            discountAmount = discount,
            shippingFee = 0.0,
            finalAmount = finalAmount,
            status = "Confirmed",
            shippingAddress = shippingAddress,
            paymentMethod = paymentMethod,
            paymentStatus = "Paid",
            trackingNumber = "TRK-${(100000..999999).random()}",
            createdAt = System.currentTimeMillis()
        )

        val orderId = orderDao.insertOrder(order)
        val itemsWithOrderId = orderItemsToInsert.map { it.copy(orderId = orderId) }
        orderDao.insertOrderItems(itemsWithOrderId)

        // Clear cart
        cartDao.clearCart(user.id)

        // Record A/B test conversion
        val exp = aiExperimentDao.getActiveExperimentSync()
        if (exp != null) {
            aiExperimentDao.recordConversionB(exp.id, finalAmount)
        }

        order.copy(id = orderId)
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
        val adminEmail = _currentUser.value?.email ?: "admin@smartcart.ai"
        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "UPDATE_ORDER_STATUS",
                details = "Order #$orderId changed to status '$status'"
            )
        )
    }

    // --- Reviews ---

    fun getReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> = reviewDao.getReviewsForProduct(productId)

    suspend fun addReview(productId: Long, rating: Int, text: String) = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: return@withContext
        val sentimentRes = SentimentAnalyzer.analyze(text, rating)

        val review = ReviewEntity(
            productId = productId,
            userId = user.id,
            userName = user.fullName,
            rating = rating,
            reviewText = text,
            sentiment = sentimentRes.sentiment,
            sentimentScore = sentimentRes.score,
            createdAt = System.currentTimeMillis()
        )
        reviewDao.insertReview(review)

        // Update product rating and review count
        val product = productDao.getProductByIdSync(productId)
        if (product != null) {
            val newCount = product.reviewCount + 1
            val newRating = ((product.rating * product.reviewCount) + rating) / newCount
            productDao.updateProduct(
                product.copy(
                    rating = String.format("%.1f", newRating).toDoubleOrNull() ?: newRating,
                    reviewCount = newCount
                )
            )
        }

        logUserEvent("rate", productId, "rating: $rating | sentiment: ${sentimentRes.sentiment}")
    }

    // --- External Merchants & Offers Comparison ---

    fun getOffersForProduct(productId: Long): Flow<List<MerchantOfferEntity>> =
        merchantOfferDao.getOffersForProduct(productId)

    suspend fun getOffersForProductSync(productId: Long): List<MerchantOfferEntity> = withContext(Dispatchers.IO) {
        merchantOfferDao.getOffersForProductSync(productId)
    }

    fun getAllOffers(): Flow<List<MerchantOfferEntity>> = merchantOfferDao.getAllOffers()

    fun getTopDiscountOffers(limit: Int = 20): Flow<List<MerchantOfferEntity>> =
        merchantOfferDao.getTopDiscountOffers(limit)

    fun getAllMerchants(): Flow<List<MerchantEntity>> = merchantDao.getAllMerchants()

    fun getActiveMerchants(): Flow<List<MerchantEntity>> = merchantDao.getActiveMerchants()

    suspend fun recordExternalClick(
        productId: Long,
        offerId: Long,
        merchantId: Long,
        merchantName: String,
        productTitle: String,
        destinationUrl: String,
        sourcePage: String,
        campaignId: String = ""
    ) = withContext(Dispatchers.IO) {
        val isAllowed = MerchantUrlSecurityValidator.isAllowedRedirect(destinationUrl)
        if (!isAllowed) {
            throw SecurityException("Untrusted or non-HTTPS merchant redirect blocked: $destinationUrl")
        }

        val userId = _currentUser.value?.id ?: 1L
        externalClickDao.insertClick(
            ExternalClickEntity(
                userId = userId,
                productId = productId,
                offerId = offerId,
                merchantId = merchantId,
                merchantName = merchantName,
                productTitle = productTitle,
                destinationUrl = destinationUrl,
                sourcePage = sourcePage,
                campaignId = campaignId,
                timestamp = System.currentTimeMillis()
            )
        )
        // Strictly recorded as 'Outbound Click', never as 'Purchase'
        logUserEvent("outbound_click", productId, "Outbound Click -> $merchantName | $destinationUrl")
    }

    fun getRecentExternalClicks(limit: Int = 50): Flow<List<ExternalClickEntity>> =
        externalClickDao.getRecentClicks(limit)

    // --- Price History & Alerts ---

    fun getPriceHistory(productId: Long): Flow<List<PriceHistoryEntity>> =
        priceHistoryDao.getPriceHistoryForProduct(productId)

    fun getPriceAlertsForUser(userId: Long): Flow<List<PriceAlertEntity>> =
        priceAlertDao.getAlertsForUser(userId)

    suspend fun createPriceAlert(
        productId: Long,
        productName: String,
        productCategory: String,
        targetPrice: Double,
        currentPrice: Double
    ): Long = withContext(Dispatchers.IO) {
        if (targetPrice <= 0.0) {
            throw IllegalArgumentException("Target price must be greater than zero.")
        }

        val userId = _currentUser.value?.id ?: 1L
        val existing = priceAlertDao.getAlert(userId, productId)
        if (existing != null) {
            priceAlertDao.updateAlert(
                existing.copy(
                    targetPrice = targetPrice,
                    currentPrice = currentPrice,
                    isTriggered = false
                )
            )
            existing.id
        } else {
            priceAlertDao.insertAlert(
                PriceAlertEntity(
                    userId = userId,
                    productId = productId,
                    productName = productName,
                    productCategory = productCategory,
                    targetPrice = targetPrice,
                    currentPrice = currentPrice
                )
            )
        }
    }

    suspend fun checkAndTriggerPriceAlerts(productId: Long, newPrice: Double) = withContext(Dispatchers.IO) {
        val alerts = priceAlertDao.getActiveAlertsForProduct(productId)
        alerts.forEach { alert ->
            if (newPrice <= alert.targetPrice && !alert.isTriggered) {
                priceAlertDao.updateAlert(
                    alert.copy(
                        currentPrice = newPrice,
                        isTriggered = true,
                        triggeredAt = System.currentTimeMillis()
                    )
                )
                notificationDao.insertNotification(
                    NotificationEntity(
                        userId = alert.userId,
                        title = "Price Drop Alert: ${alert.productName}",
                        message = "Price dropped to ₹${String.format("%,.0f", newPrice)}! Your target of ₹${String.format("%,.0f", alert.targetPrice)} has been met.",
                        type = "PRICE_DROP"
                    )
                )
            }
        }
    }

    suspend fun deletePriceAlert(alertId: Long) = withContext(Dispatchers.IO) {
        priceAlertDao.deleteAlert(alertId)
    }

    // --- Promo Codes & Merchant Coupons ---

    suspend fun validatePromoCode(code: String, subtotal: Double): PromoValidationResult = withContext(Dispatchers.IO) {
        val trimmed = code.trim().uppercase()
        if (trimmed.isBlank()) {
            return@withContext PromoValidationResult(false, 0.0, "")
        }

        val campaign = promoCampaignDao.getCampaignByCode(trimmed)
        if (campaign == null || campaign.status != "ACTIVE") {
            return@withContext PromoValidationResult(false, 0.0, "Invalid or inactive promo code '$trimmed'.")
        }

        val now = System.currentTimeMillis()
        if (now > campaign.expiryDate) {
            return@withContext PromoValidationResult(false, 0.0, "Promo code '$trimmed' has expired.")
        }
        if (now < campaign.startDate) {
            return@withContext PromoValidationResult(false, 0.0, "Promo code '$trimmed' is not yet active.")
        }

        if (subtotal < campaign.minOrderValue) {
            return@withContext PromoValidationResult(
                false,
                0.0,
                "Minimum cart value of ₹${String.format("%,.0f", campaign.minOrderValue)} required for code '$trimmed'."
            )
        }

        val discount = if (campaign.discountType == "PERCENTAGE") {
            val calc = subtotal * (campaign.discountValue / 100.0)
            calc.coerceAtMost(campaign.maxDiscount)
        } else {
            campaign.discountValue.coerceAtMost(campaign.maxDiscount).coerceAtMost(subtotal)
        }

        PromoValidationResult(
            isValid = true,
            discountAmount = discount,
            message = "Code '$trimmed' applied! You saved ₹${String.format("%,.0f", discount)}.",
            campaign = campaign
        )
    }

    fun getActivePromoCampaigns(): Flow<List<PromoCampaignEntity>> = promoCampaignDao.getActiveCampaigns()

    fun getAllPromoCampaigns(): Flow<List<PromoCampaignEntity>> = promoCampaignDao.getAllCampaigns()

    suspend fun createPromoCampaign(campaign: PromoCampaignEntity) = withContext(Dispatchers.IO) {
        promoCampaignDao.insertCampaign(campaign)
    }

    fun getAllMerchantCoupons(): Flow<List<MerchantCouponEntity>> = merchantCouponDao.getAllCoupons()

    // --- Peer-to-Peer Voucher Marketplace ---

    fun getVerifiedVouchers(): Flow<List<VoucherListingEntity>> = voucherListingDao.getVerifiedVouchers()

    fun getVouchersForSeller(sellerId: Long): Flow<List<VoucherListingEntity>> =
        voucherListingDao.getVouchersForSeller(sellerId)

    fun getPendingVerificationVouchers(): Flow<List<VoucherListingEntity>> =
        voucherListingDao.getPendingVerificationVouchers()

    fun getAllVouchers(): Flow<List<VoucherListingEntity>> = voucherListingDao.getAllVouchers()

    suspend fun createVoucherListing(
        brand: String,
        voucherType: String,
        faceValue: Double,
        sellingPrice: Double,
        expiryDate: String,
        terms: String,
        verificationInfo: String,
        sellerAgreement: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val user = _currentUser.value ?: throw IllegalStateException("Must be logged in to list vouchers")
        if (user.riskStatus == "BLOCKED") {
            throw IllegalStateException("Your account is restricted from listing vouchers.")
        }
        if (faceValue <= 0.0 || sellingPrice <= 0.0) {
            throw IllegalArgumentException("Face value and selling price must be greater than zero.")
        }
        if (sellingPrice > faceValue) {
            throw IllegalArgumentException("Selling price cannot exceed face value.")
        }

        val discount = SecurityUtils.calculateVerifiedDiscount(faceValue, sellingPrice)
        val maskedInfo = SecurityUtils.maskVoucherCode(verificationInfo)

        val voucher = VoucherListingEntity(
            sellerUserId = user.id,
            sellerName = user.fullName,
            sellerEmail = user.email,
            brand = brand,
            voucherType = voucherType,
            faceValue = faceValue,
            sellingPrice = sellingPrice,
            discountPercent = discount,
            expiryDate = expiryDate,
            terms = terms,
            verificationInfo = maskedInfo,
            status = "PENDING_VERIFICATION",
            sellerConfirmationAgreed = sellerAgreement,
            createdAt = System.currentTimeMillis()
        )

        val id = voucherListingDao.insertVoucher(voucher)

        // Notify user about submission
        notificationDao.insertNotification(
            NotificationEntity(
                userId = user.id,
                title = "Voucher Submitted for Verification",
                message = "Your $brand voucher (₹${String.format("%,.0f", faceValue)}) was submitted and is pending compliance review.",
                type = "VOUCHER_STATUS"
            )
        )

        id
    }

    suspend fun updateVoucherStatus(voucherId: Long, newStatus: String, reason: String = "") = withContext(Dispatchers.IO) {
        voucherListingDao.updateVoucherStatus(voucherId, newStatus, reason, System.currentTimeMillis())
        val v = voucherListingDao.getVoucherById(voucherId)
        if (v != null) {
            val title = if (newStatus == "VERIFIED") "Voucher Listing Approved!" else "Voucher Listing Update"
            val msg = if (newStatus == "VERIFIED") {
                "Your ${v.brand} ₹${String.format("%,.0f", v.faceValue)} voucher is now verified and live in the Marketplace."
            } else {
                "Your ${v.brand} voucher status changed to $newStatus. Reason: ${reason.ifBlank { "Policy review." }}"
            }
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = v.sellerUserId,
                    title = title,
                    message = msg,
                    type = "VOUCHER_STATUS"
                )
            )
        }
    }

    suspend fun buyVoucher(voucherId: Long, paymentMethod: String): VoucherTransactionEntity = withContext(Dispatchers.IO) {
        // Concurrency lock prevents race conditions and double-selling
        voucherPurchaseMutex.withLock {
            val buyer = _currentUser.value ?: throw IllegalStateException("Must be logged in to purchase vouchers")
            if (buyer.riskStatus == "BLOCKED") {
                throw IllegalStateException("Your account is restricted from making purchases.")
            }

            val voucher = voucherListingDao.getVoucherById(voucherId)
                ?: throw IllegalStateException("Voucher not found")

            if (voucher.status != "VERIFIED") {
                throw IllegalStateException("Voucher is no longer available (Status: ${voucher.status})")
            }

            if (voucher.sellerUserId == buyer.id) {
                throw IllegalStateException("You cannot purchase your own voucher listing.")
            }

            val token = "TXN-ESCROW-${(100000..999999).random()}"
            val transaction = VoucherTransactionEntity(
                voucherId = voucher.id,
                buyerUserId = buyer.id,
                buyerName = buyer.fullName,
                sellerUserId = voucher.sellerUserId,
                brand = voucher.brand,
                amountPaid = voucher.sellingPrice,
                paymentMethod = "$paymentMethod (Demo Escrow)",
                paymentStatus = "COMPLETED",
                transactionToken = token,
                createdAt = System.currentTimeMillis()
            )

            voucherTransactionDao.insertTransaction(transaction)

            // Mark voucher as SOLD immediately
            voucherListingDao.updateVoucherStatus(voucher.id, "SOLD", "Purchased by ${buyer.fullName}")

            // Notify Buyer with Demo Escrow assurance
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = buyer.id,
                    title = "Voucher Purchased (Demo Escrow)",
                    message = "You purchased ${voucher.brand} ₹${String.format("%,.0f", voucher.faceValue)} for ₹${String.format("%,.0f", voucher.sellingPrice)}. Token: $token. Funds held in Demo Escrow until verified.",
                    type = "VOUCHER_STATUS"
                )
            )

            // Notify Seller
            notificationDao.insertNotification(
                NotificationEntity(
                    userId = voucher.sellerUserId,
                    title = "Voucher Sold!",
                    message = "Your ${voucher.brand} ₹${String.format("%,.0f", voucher.faceValue)} voucher was sold for ₹${String.format("%,.0f", voucher.sellingPrice)} to ${buyer.fullName}.",
                    type = "VOUCHER_STATUS"
                )
            )

            transaction
        }
    }

    fun getTransactionsForBuyer(userId: Long): Flow<List<VoucherTransactionEntity>> =
        voucherTransactionDao.getTransactionsForBuyer(userId)

    fun getTransactionsForSeller(userId: Long): Flow<List<VoucherTransactionEntity>> =
        voucherTransactionDao.getTransactionsForSeller(userId)

    // --- Notifications ---

    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAllAsReadForUser(userId)
    }

    suspend fun deleteNotification(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.deleteNotification(id)
    }

    suspend fun clearAllNotifications(userId: Long) = withContext(Dispatchers.IO) {
        notificationDao.clearAllNotificationsForUser(userId)
    }

    // --- Admin Operations ---

    suspend fun addProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val id = productDao.insertProduct(product)
        val adminEmail = _currentUser.value?.email ?: "admin@smartcart.ai"
        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "CREATE_PRODUCT",
                details = "Created product '${product.name}' in category '${product.category}' (Price: ₹${product.price})"
            )
        )
        id
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        val adminEmail = _currentUser.value?.email ?: "admin@smartcart.ai"
        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "UPDATE_PRODUCT",
                details = "Updated product '${product.name}' (Stock: ${product.stock}, Price: ₹${product.price})"
            )
        )
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        merchantOfferDao.deleteOffersForProduct(product.id)
        productDao.deleteProduct(product)
        val adminEmail = _currentUser.value?.email ?: "admin@smartcart.ai"
        auditLogDao.insertLog(
            AuditLogEntity(
                actorEmail = adminEmail,
                action = "DELETE_PRODUCT",
                details = "Deleted product '${product.name}' (ID: ${product.id})"
            )
        )
    }

    suspend fun deleteUserAccount(userId: Long) = withContext(Dispatchers.IO) {
        cartDao.clearCartForUser(userId)
        wishlistDao.clearWishlistForUser(userId)
        userEventDao.clearEventsForUser(userId)
        notificationDao.clearAllNotificationsForUser(userId)
        userDao.deleteUserById(userId)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = null
        }
    }

    fun getActiveExperiment(): Flow<AIExperimentEntity?> = aiExperimentDao.getActiveExperiment()

    suspend fun recordExperimentClick(isVariantB: Boolean) = withContext(Dispatchers.IO) {
        val exp = aiExperimentDao.getActiveExperimentSync() ?: return@withContext
        if (isVariantB) aiExperimentDao.recordClickB(exp.id) else aiExperimentDao.recordClickA(exp.id)
    }

    fun getAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs(50)

    suspend fun getAdminSummaryStats(): Map<String, Any> = withContext(Dispatchers.IO) {
        val totalRevenue = orderDao.getTotalRevenue() ?: 0.0
        val totalOrders = orderDao.getOrderCount()
        val totalProducts = productDao.getProductCount()
        val totalCustomers = userDao.getUserCount()
        val totalReviews = reviewDao.getTotalReviewCount()
        val totalEvents = userEventDao.getEventCount()
        val totalClicks = externalClickDao.getTotalClicksCount()

        mapOf(
            "totalRevenue" to totalRevenue,
            "totalOrders" to totalOrders,
            "totalProducts" to totalProducts,
            "totalCustomers" to totalCustomers,
            "totalReviews" to totalReviews,
            "totalEvents" to totalEvents,
            "totalClicks" to totalClicks
        )
    }
}
