package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.ParsedSearchQuery
import com.example.ai.RecommendationMetrics
import com.example.ai.RecommendedProduct
import com.example.data.model.*
import com.example.data.repository.CartItemWithProduct
import com.example.data.repository.SmartCartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class NavigationTab {
    HOME,
    DEALS,
    CATALOG,
    VOUCHERS,
    ASSISTANT,
    CART,
    ORDERS,
    PROFILE,
    ADMIN
}

enum class SortOption {
    POPULARITY,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    RATING,
    DISCOUNT,
    NEWEST
}

enum class MessageSender {
    USER,
    ASSISTANT
}

data class AssistantMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val recommendedProducts: List<ProductEntity> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class SmartCartViewModel(
    val repository: SmartCartRepository
) : ViewModel() {

    // Current navigation tab
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Active product for detail sheet
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Offers & Price history for selected product
    private val _selectedProductOffers = MutableStateFlow<List<MerchantOfferEntity>>(emptyList())
    val selectedProductOffers: StateFlow<List<MerchantOfferEntity>> = _selectedProductOffers.asStateFlow()

    private val _selectedProductPriceHistory = MutableStateFlow<List<PriceHistoryEntity>>(emptyList())
    val selectedProductPriceHistory: StateFlow<List<PriceHistoryEntity>> = _selectedProductPriceHistory.asStateFlow()

    // Search and filters
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null)
    val selectedBrand = MutableStateFlow<String?>(null)
    val priceRange = MutableStateFlow(0f..200000f)
    val minRatingFilter = MutableStateFlow(0f)
    val sortOption = MutableStateFlow(SortOption.POPULARITY)
    val parsedQuery = MutableStateFlow<ParsedSearchQuery?>(null)

    // Data streams
    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brands: StateFlow<List<String>> = repository.getAllBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUser

    // Cart items joined with products
    private val _cartWithProducts = MutableStateFlow<List<CartItemWithProduct>>(emptyList())
    val cartWithProducts: StateFlow<List<CartItemWithProduct>> = _cartWithProducts.asStateFlow()

    // Wishlist product IDs for fast lookup
    private val _wishlistIds = MutableStateFlow<Set<Long>>(emptySet())
    val wishlistIds: StateFlow<Set<Long>> = _wishlistIds.asStateFlow()

    // Customer orders
    private val _userOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val userOrders: StateFlow<List<OrderEntity>> = _userOrders.asStateFlow()

    // AI recommendations
    private val _recommendedForYou = MutableStateFlow<List<RecommendedProduct>>(emptyList())
    val recommendedForYou: StateFlow<List<RecommendedProduct>> = _recommendedForYou.asStateFlow()

    private val _trendingProducts = MutableStateFlow<List<RecommendedProduct>>(emptyList())
    val trendingProducts: StateFlow<List<RecommendedProduct>> = _trendingProducts.asStateFlow()

    private val _similarProducts = MutableStateFlow<List<RecommendedProduct>>(emptyList())
    val similarProducts: StateFlow<List<RecommendedProduct>> = _similarProducts.asStateFlow()

    private val _frequentlyBoughtTogether = MutableStateFlow<List<RecommendedProduct>>(emptyList())
    val frequentlyBoughtTogether: StateFlow<List<RecommendedProduct>> = _frequentlyBoughtTogether.asStateFlow()

    private val _cartComplementaryProducts = MutableStateFlow<List<RecommendedProduct>>(emptyList())
    val cartComplementaryProducts: StateFlow<List<RecommendedProduct>> = _cartComplementaryProducts.asStateFlow()

    val aiMetrics: StateFlow<RecommendationMetrics> = repository.aiMetrics

    // --- Marketplace, Deals & Offers Flows ---
    val allMerchants: StateFlow<List<MerchantEntity>> = repository.getAllMerchants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topDeals: StateFlow<List<MerchantOfferEntity>> = repository.getTopDiscountOffers(25)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val highestDiscountProducts: StateFlow<List<ProductEntity>> = repository.getHighestDiscountProducts(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePromoCampaigns: StateFlow<List<PromoCampaignEntity>> = repository.getActivePromoCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMerchantCoupons: StateFlow<List<MerchantCouponEntity>> = repository.getAllMerchantCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Promo Code in Cart
    val appliedPromoCode = MutableStateFlow("")
    val appliedDiscount = MutableStateFlow(0.0)
    val promoMessage = MutableStateFlow<String?>(null)

    // User Price Alerts & Notifications
    private val _userPriceAlerts = MutableStateFlow<List<PriceAlertEntity>>(emptyList())
    val userPriceAlerts: StateFlow<List<PriceAlertEntity>> = _userPriceAlerts.asStateFlow()

    private val _userNotifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val userNotifications: StateFlow<List<NotificationEntity>> = _userNotifications.asStateFlow()

    // Voucher Marketplace
    val verifiedVouchers: StateFlow<List<VoucherListingEntity>> = repository.getVerifiedVouchers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingVouchers: StateFlow<List<VoucherListingEntity>> = repository.getPendingVerificationVouchers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mySellerVouchers = MutableStateFlow<List<VoucherListingEntity>>(emptyList())
    val mySellerVouchers: StateFlow<List<VoucherListingEntity>> = _mySellerVouchers.asStateFlow()

    private val _myPurchasedVouchers = MutableStateFlow<List<VoucherTransactionEntity>>(emptyList())
    val myPurchasedVouchers: StateFlow<List<VoucherTransactionEntity>> = _myPurchasedVouchers.asStateFlow()

    // AI Assistant chat history
    private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "Hello! I am your Shop Mind AI Personal Shopping Concierge. I can help you find products grounded directly in our catalog across 15 categories, compare prices across Amazon, Flipkart, and Croma, or find active vouchers. What are you shopping for today?"
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages.asStateFlow()
    val isAssistantTyping = MutableStateFlow(false)

    // Admin & Analytics
    val activeExperiment = repository.getActiveExperiment()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val auditLogs = repository.getAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts = repository.getLowStockProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentClicks = repository.getRecentExternalClicks(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _adminStats = MutableStateFlow<Map<String, Any>>(emptyMap())
    val adminStats: StateFlow<Map<String, Any>> = _adminStats.asStateFlow()

    val isRetraining = MutableStateFlow(false)
    val retrainStatusMessage = MutableStateFlow<String?>(null)

    // UI Dialogs & checkout flow
    val showAuthDialog = MutableStateFlow(false)
    val showRegisterDialog = MutableStateFlow(false)
    val showOnboardingDialog = MutableStateFlow(false)
    val toastMessage = MutableStateFlow<String?>(null)
    val orderSuccess = MutableStateFlow<OrderEntity?>(null)

    init {
        // Observe current user changes to reload cart, wishlist, alerts, vouchers, etc.
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    loadUserData(user.id)
                    refreshRecommendations()
                }
            }
        }

        // Observe product catalog changes to recompute recommendations
        viewModelScope.launch {
            allProducts.collect { products ->
                if (products.isNotEmpty()) {
                    refreshRecommendations()
                    refreshAdminStats()
                }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectProduct(product: ProductEntity?) {
        _selectedProduct.value = product
        if (product != null) {
            viewModelScope.launch {
                repository.logUserEvent("view", product.id, "product_detail")
                // Load similar & frequently bought
                _similarProducts.value = repository.getSimilarProducts(product)
                _frequentlyBoughtTogether.value = repository.getFrequentlyBoughtTogether(product)

                // Load merchant offers
                repository.getOffersForProduct(product.id).collect { offers ->
                    _selectedProductOffers.value = offers
                }
            }
            viewModelScope.launch {
                repository.getPriceHistory(product.id).collect { history ->
                    _selectedProductPriceHistory.value = history
                }
            }
        } else {
            _selectedProductOffers.value = emptyList()
            _selectedProductPriceHistory.value = emptyList()
        }
    }

    private fun loadUserData(userId: Long) {
        viewModelScope.launch {
            repository.getCartItems(userId).collect { items ->
                val allProds = allProducts.value.associateBy { it.id }
                _cartWithProducts.value = items.mapNotNull { item ->
                    val prod = allProds[item.productId]
                    if (prod != null) CartItemWithProduct(item, prod) else null
                }

                if (items.isNotEmpty()) {
                    val firstProdId = items.first().productId
                    val firstProd = allProds[firstProdId]
                    if (firstProd != null) {
                        _cartComplementaryProducts.value = repository.getFrequentlyBoughtTogether(firstProd, 4)
                    }
                } else {
                    _cartComplementaryProducts.value = emptyList()
                }
            }
        }

        viewModelScope.launch {
            repository.getWishlistItems(userId).collect { items ->
                _wishlistIds.value = items.map { it.productId }.toSet()
            }
        }

        viewModelScope.launch {
            repository.getOrdersForUser(userId).collect { orders ->
                _userOrders.value = orders
            }
        }

        viewModelScope.launch {
            repository.getPriceAlertsForUser(userId).collect { alerts ->
                _userPriceAlerts.value = alerts
            }
        }

        viewModelScope.launch {
            repository.getNotificationsForUser(userId).collect { notifs ->
                _userNotifications.value = notifs
            }
        }

        viewModelScope.launch {
            repository.getVouchersForSeller(userId).collect { vouchers ->
                _mySellerVouchers.value = vouchers
            }
        }

        viewModelScope.launch {
            repository.getTransactionsForBuyer(userId).collect { txns ->
                _myPurchasedVouchers.value = txns
            }
        }
    }

    fun refreshRecommendations() {
        viewModelScope.launch {
            _recommendedForYou.value = repository.getPersonalizedRecommendations(12)
            _trendingProducts.value = repository.getTrendingProducts(10)
        }
    }

    fun refreshAdminStats() {
        viewModelScope.launch {
            _adminStats.value = repository.getAdminSummaryStats()
        }
    }

    // --- Search & Natural Language Parsing ---

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        if (newQuery.isBlank()) {
            parsedQuery.value = null
        } else {
            val parsed = com.example.ai.HybridRecommendationEngine().parseNaturalLanguageQuery(
                query = newQuery,
                availableCategories = categories.value,
                availableBrands = brands.value
            )
            parsedQuery.value = parsed
            if (parsed.targetCategory != null && selectedCategory.value == null) {
                selectedCategory.value = parsed.targetCategory
            }
            if (parsed.maxBudget != null) {
                priceRange.value = 0f..(parsed.maxBudget.toFloat().coerceIn(1000f, 200000f))
            }
        }
    }

    fun clearSearch() {
        searchQuery.value = ""
        parsedQuery.value = null
        selectedCategory.value = null
        selectedBrand.value = null
        priceRange.value = 0f..200000f
        minRatingFilter.value = 0f
        sortOption.value = SortOption.POPULARITY
    }

    // --- Cart & Wishlist ---

    fun addToCart(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(productId, quantity)
            toastMessage.value = "Added to cart"
        }
    }

    fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun removeFromCart(cartItemId: Long, productId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId, productId)
            toastMessage.value = "Item removed"
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            val added = repository.toggleWishlist(productId)
            toastMessage.value = if (added) "Saved to Wishlist" else "Removed from Wishlist"
            refreshRecommendations()
        }
    }

    // --- Promo Codes ---

    fun applyPromoCode(code: String, subtotal: Double) {
        viewModelScope.launch {
            val res = repository.validatePromoCode(code, subtotal)
            if (res.isValid) {
                appliedPromoCode.value = code.trim().uppercase()
                appliedDiscount.value = res.discountAmount
                promoMessage.value = res.message
                toastMessage.value = res.message
            } else {
                appliedPromoCode.value = ""
                appliedDiscount.value = 0.0
                promoMessage.value = res.message
                toastMessage.value = res.message
            }
        }
    }

    fun removePromoCode() {
        appliedPromoCode.value = ""
        appliedDiscount.value = 0.0
        promoMessage.value = null
    }

    fun placeOrder(address: String, paymentMethod: String, couponCode: String) {
        viewModelScope.launch {
            try {
                val order = repository.placeOrder(address, paymentMethod, couponCode)
                orderSuccess.value = order
                appliedPromoCode.value = ""
                appliedDiscount.value = 0.0
                promoMessage.value = null
                toastMessage.value = "Order placed successfully!"
                refreshRecommendations()
                refreshAdminStats()
            } catch (e: Exception) {
                toastMessage.value = "Failed: ${e.message}"
            }
        }
    }

    // --- External Clicks ---

    fun recordExternalClick(
        offer: MerchantOfferEntity,
        product: ProductEntity,
        sourcePage: String
    ) {
        viewModelScope.launch {
            val destUrl = offer.affiliateUrl.ifBlank { offer.externalUrl }
            repository.recordExternalClick(
                productId = product.id,
                offerId = offer.id,
                merchantId = offer.merchantId,
                merchantName = offer.merchantName,
                productTitle = product.name,
                destinationUrl = destUrl,
                sourcePage = sourcePage
            )
        }
    }

    // --- Price Tracking & Alerts ---

    fun createPriceAlert(product: ProductEntity, targetPrice: Double) {
        viewModelScope.launch {
            repository.createPriceAlert(
                productId = product.id,
                productName = product.name,
                productCategory = product.category,
                targetPrice = targetPrice,
                currentPrice = product.finalPrice
            )
            toastMessage.value = "Price alert set! We'll notify you if it drops below ₹${String.format("%,.0f", targetPrice)}"
        }
    }

    fun deletePriceAlert(alertId: Long) {
        viewModelScope.launch {
            repository.deletePriceAlert(alertId)
            toastMessage.value = "Price alert removed"
        }
    }

    // --- Voucher Marketplace ---

    fun createVoucherListing(
        brand: String,
        voucherType: String,
        faceValue: Double,
        sellingPrice: Double,
        expiryDate: String,
        terms: String,
        verificationInfo: String,
        agreementAccepted: Boolean
    ) {
        viewModelScope.launch {
            try {
                repository.createVoucherListing(
                    brand = brand,
                    voucherType = voucherType,
                    faceValue = faceValue,
                    sellingPrice = sellingPrice,
                    expiryDate = expiryDate,
                    terms = terms,
                    verificationInfo = verificationInfo,
                    sellerAgreement = agreementAccepted
                )
                toastMessage.value = "Voucher submitted for verification!"
            } catch (e: Exception) {
                toastMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun buyVoucher(voucherId: Long, paymentMethod: String) {
        viewModelScope.launch {
            try {
                val txn = repository.buyVoucher(voucherId, paymentMethod)
                toastMessage.value = "Voucher purchased! Token: ${txn.transactionToken}"
            } catch (e: Exception) {
                toastMessage.value = "Purchase failed: ${e.message}"
            }
        }
    }

    fun verifyVoucher(voucherId: Long, approve: Boolean, reason: String = "") {
        viewModelScope.launch {
            val status = if (approve) "VERIFIED" else "REJECTED"
            repository.updateVoucherStatus(voucherId, status, reason)
            toastMessage.value = if (approve) "Voucher approved and live" else "Voucher rejected"
        }
    }

    // --- Notifications ---

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(user.id)
        }
    }

    // --- User Account & Auth ---

    fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        categories: String,
        brands: String,
        minPrice: Double,
        maxPrice: Double
    ) {
        viewModelScope.launch {
            val res = repository.registerUserAccount(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                preferredCategories = categories,
                preferredBrands = brands,
                minPrice = minPrice,
                maxPrice = maxPrice
            )
            res.fold(
                onSuccess = { user ->
                    toastMessage.value = "Account created! Welcome, ${user.fullName} (${user.userCode})"
                    showRegisterDialog.value = false
                    showAuthDialog.value = false
                },
                onFailure = { err ->
                    toastMessage.value = err.message ?: "Registration failed"
                }
            )
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val res = repository.loginUser(email, password)
            res.fold(
                onSuccess = { user ->
                    toastMessage.value = "Logged in as ${user.fullName}"
                    showAuthDialog.value = false
                },
                onFailure = { err ->
                    toastMessage.value = err.message ?: "Login failed"
                }
            )
        }
    }

    fun updateUserProfile(
        name: String,
        phone: String,
        categories: String,
        brands: String,
        minPrice: Double,
        maxPrice: Double
    ) {
        viewModelScope.launch {
            repository.updateUserProfile(name, phone, categories, brands, minPrice, maxPrice)
            toastMessage.value = "Profile updated successfully!"
            refreshRecommendations()
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        viewModelScope.launch {
            val res = repository.changePassword(oldPass, newPass)
            res.fold(
                onSuccess = { toastMessage.value = "Password updated successfully!" },
                onFailure = { err -> toastMessage.value = err.message ?: "Failed to update password" }
            )
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val res = repository.deleteUserAccount()
            res.fold(
                onSuccess = { toastMessage.value = "Account removed. Reverted to Demo Customer." },
                onFailure = { err -> toastMessage.value = err.message ?: "Failed to delete account" }
            )
        }
    }

    fun addReview(productId: Long, rating: Int, text: String) {
        viewModelScope.launch {
            repository.addReview(productId, rating, text)
            toastMessage.value = "Review submitted with AI sentiment analysis!"
            val updated = repository.getProductByIdSync(productId)
            if (updated != null) {
                _selectedProduct.value = updated
            }
            refreshRecommendations()
        }
    }

    fun updatePreferences(categories: String, minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            repository.updateUserPreferences(categories, minPrice, maxPrice)
            toastMessage.value = "AI Preferences updated!"
            refreshRecommendations()
        }
    }

    fun clearInteractionHistory() {
        viewModelScope.launch {
            repository.clearUserInteractionHistory()
            toastMessage.value = "Interaction history cleared"
            refreshRecommendations()
        }
    }

    fun switchDemoUser(role: String) {
        viewModelScope.launch {
            val email = if (role == "admin") "demo.admin@example.com" else "demo.customer@example.com"
            val user = repository.getUserByEmail(email)
            if (user != null) {
                repository.switchUser(user)
                toastMessage.value = "Switched to ${user.fullName} (${user.role})"
                if (user.role == "admin") {
                    selectTab(NavigationTab.ADMIN)
                } else {
                    selectTab(NavigationTab.HOME)
                }
            }
        }
    }

    fun retrainModel() {
        viewModelScope.launch {
            isRetraining.value = true
            retrainStatusMessage.value = "Recalculating collaborative similarity matrix and feature vectors..."
            kotlinx.coroutines.delay(1200)
            val newMetrics = repository.retrainRecommendationModel()
            isRetraining.value = false
            retrainStatusMessage.value = "Model retrained: ${newMetrics.modelVersion}. NDCG@5: ${newMetrics.ndcgAt5}"
            refreshRecommendations()
        }
    }

    fun recordExperimentClick(isVariantB: Boolean) {
        viewModelScope.launch {
            repository.recordExperimentClick(isVariantB)
        }
    }

    fun updateProductStock(product: ProductEntity, newStock: Int) {
        viewModelScope.launch {
            repository.updateProduct(product.copy(stock = newStock))
            toastMessage.value = "Updated stock for ${product.name}"
            refreshAdminStats()
        }
    }

    // --- AI Shopping Assistant Grounded Query Processing ---

    fun sendAssistantMessage(userQuery: String) {
        if (userQuery.isBlank()) return

        val userMsg = AssistantMessage(
            sender = MessageSender.USER,
            text = userQuery
        )
        _assistantMessages.value = _assistantMessages.value + userMsg
        isAssistantTyping.value = true

        viewModelScope.launch(Dispatchers.Default) {
            kotlinx.coroutines.delay(600)
            val reply = processGroundedAssistantQuery(userQuery)
            _assistantMessages.value = _assistantMessages.value + reply
            isAssistantTyping.value = false
        }
    }

    private fun processGroundedAssistantQuery(query: String): AssistantMessage {
        val qLower = query.lowercase(Locale.ROOT)
        val catalog = allProducts.value

        var maxBudget: Double? = null
        val underRegex = Regex("(?:under|below|less than|within|max)\\s*(?:₹|rs\\.?)?\\s*([0-9]+(?:,[0-9]+)*)")
        val match = underRegex.find(qLower)
        if (match != null) {
            val numStr = match.groupValues[1].replace(",", "")
            maxBudget = numStr.toDoubleOrNull()
        }

        val matchedProducts = catalog.filter { prod ->
            val matchesBudget = if (maxBudget != null) prod.finalPrice <= maxBudget else true
            val nameTokens = prod.name.lowercase(Locale.ROOT)
            val descTokens = prod.description.lowercase(Locale.ROOT)
            val tagTokens = prod.tags.lowercase(Locale.ROOT)
            val catTokens = prod.category.lowercase(Locale.ROOT)
            val brandTokens = prod.brand.lowercase(Locale.ROOT)

            var keywordMatch = false
            when {
                qLower.contains("laptop") -> keywordMatch = catTokens.contains("electronics") && (nameTokens.contains("macbook") || nameTokens.contains("laptop") || prod.subcategory.contains("laptops", ignoreCase = true))
                qLower.contains("headphone") || qLower.contains("earbud") || qLower.contains("audio") || qLower.contains("sound") -> keywordMatch = catTokens.contains("audio")
                qLower.contains("phone") || qLower.contains("smartphone") || qLower.contains("mobile") -> keywordMatch = catTokens.contains("smartphones")
                qLower.contains("game") || qLower.contains("gaming") || qLower.contains("ps5") || qLower.contains("switch") -> keywordMatch = catTokens.contains("gaming")
                qLower.contains("shoe") || qLower.contains("sneaker") || qLower.contains("jeans") || qLower.contains("fashion") -> keywordMatch = catTokens.contains("fashion")
                qLower.contains("toy") || qLower.contains("lego") || qLower.contains("board game") || qLower.contains("robot") || qLower.contains("10 year old") -> keywordMatch = catTokens.contains("toys")
                qLower.contains("beauty") || qLower.contains("serum") || qLower.contains("perfume") || qLower.contains("skincare") -> keywordMatch = catTokens.contains("beauty")
                qLower.contains("book") || qLower.contains("read") -> keywordMatch = catTokens.contains("books")
                qLower.contains("coffee") || qLower.contains("tea") -> keywordMatch = catTokens.contains("grocery")
                qLower.contains("dashcam") || qLower.contains("car") -> keywordMatch = catTokens.contains("automotive")
                qLower.contains("luggage") || qLower.contains("suitcase") -> keywordMatch = catTokens.contains("travel")
                qLower.contains("watch") -> keywordMatch = nameTokens.contains("watch")
                else -> {
                    val words = qLower.split(" ").filter { it.length > 3 }
                    keywordMatch = words.any { word ->
                        nameTokens.contains(word) || brandTokens.contains(word) || tagTokens.contains(word) || descTokens.contains(word)
                    }
                }
            }

            matchesBudget && keywordMatch && prod.stock > 0
        }
            .sortedByDescending { it.rating * 10.0 + it.popularityScore }
            .take(4)

        val replyText = when {
            matchedProducts.isNotEmpty() -> {
                val budgetNote = if (maxBudget != null) " matching your budget of under ₹${String.format("%,.0f", maxBudget)}" else ""
                "I found ${matchedProducts.size} grounded products across our multi-category catalog$budgetNote. You can compare real offers from Amazon, Flipkart, and Croma:"
            }
            maxBudget != null -> {
                "No products were found strictly under ₹${String.format("%,.0f", maxBudget)}. Here are available catalog alternatives:"
            }
            else -> {
                "No exact match was found for \"$query\" in our catalog. Here are top-rated products you can explore:"
            }
        }

        val fallbackList = if (matchedProducts.isEmpty()) {
            catalog.sortedByDescending { it.rating }.take(3)
        } else {
            matchedProducts
        }

        return AssistantMessage(
            sender = MessageSender.ASSISTANT,
            text = replyText,
            recommendedProducts = fallbackList
        )
    }
}

class SmartCartViewModelFactory(
    private val repository: SmartCartRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SmartCartViewModel(repository) as T
    }
}
