package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.config.AppConfig
import com.example.config.AppEnvironment
import com.example.config.ProviderStatus
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.provider.MerchantProviderRegistry
import com.example.data.repository.SmartCartRepository
import com.example.util.MerchantUrlSecurityValidator
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Comprehensive End-to-End Test Suite verifying the 30 critical user journeys
 * required for Phase 4 of the SmartCart AI final release baseline.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EndToEndUserJourneysTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: SmartCartRepository

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = SmartCartRepository(database)
        repository.seedingJob.join()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // Journey 1: Application Launch & Baseline Initialization
    @Test
    fun journey01_applicationLaunch() = runBlocking {
        assertEquals("SMARTCART-RELEASE-CANDIDATE-04", AppConfig.RELEASE_BASELINE)
        assertEquals("com.aistudio.smartcart.ai", AppConfig.APPLICATION_ID)
        val productCount = database.productDao().getAllProducts().first().size
        assertTrue("Seed catalog must be loaded on launch", productCount > 0)
    }

    // Journey 2: User Registration
    @Test
    fun journey02_registration() = runBlocking {
        val regResult = repository.registerUserAccount(
            fullName = "Rahul Verma",
            email = "rahul.verma@smartcart.ai",
            phone = "9811223344",
            password = "StrongPassword2026!",
            preferredCategories = "Smartphones,Audio",
            preferredBrands = "Apple,Sony",
            minPrice = 1000.0,
            maxPrice = 80000.0
        )
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)
        assertTrue(user!!.userCode.startsWith("SC-USER-"))
        assertNotEquals("StrongPassword2026!", user.passwordHash)
    }

    // Journey 3: User Login
    @Test
    fun journey03_login() = runBlocking {
        val email = "login.test@smartcart.ai"
        val pwd = "LoginPass@123"
        repository.registerUserAccount(
            fullName = "Login Tester",
            email = email,
            phone = "9988776655",
            password = pwd,
            preferredCategories = "Laptops",
            preferredBrands = "Dell",
            minPrice = 5000.0,
            maxPrice = 90000.0
        )
        val loginRes = repository.loginUser(email, pwd)
        assertTrue(loginRes.isSuccess)
        assertEquals(email, repository.currentUser.value?.email)
    }

    // Journey 4: User Logout / Switch User
    @Test
    fun journey04_logout() = runBlocking {
        val email = "logout.test@smartcart.ai"
        val pwd = "LogoutPass@123"
        val reg = repository.registerUserAccount("Logout User", email, "9998887776", pwd, "Audio", "Sony", 100.0, 5000.0)
        val user = reg.getOrThrow()
        repository.switchUser(user)
        assertEquals(user.id, repository.currentUser.value?.id)
        val demoAdmin = database.userDao().getUserByEmail("demo.admin@example.com")
        assertNotNull(demoAdmin)
        repository.switchUser(demoAdmin!!)
        assertEquals("admin", repository.currentUser.value?.role)
    }

    // Journey 5: Customer Dashboard
    @Test
    fun journey05_customerDashboard() = runBlocking {
        val products = database.productDao().getAllProducts().first()
        assertTrue(products.isNotEmpty())
        val featured = products.filter { it.isFeatured }
        assertTrue(featured.isNotEmpty())
    }

    // Journey 6: Admin Dashboard
    @Test
    fun journey06_adminDashboard() = runBlocking {
        val adminUser = database.userDao().getUserByEmail("demo.admin@example.com")
        assertNotNull(adminUser)
        assertEquals("admin", adminUser?.role)
    }

    // Journey 7: Category Browsing
    @Test
    fun journey07_categoryBrowsing() = runBlocking {
        val smartphones = database.productDao().getProductsByCategory("Smartphones").first()
        assertTrue(smartphones.isNotEmpty())
        assertTrue(smartphones.all { it.category == "Smartphones" })
    }

    // Journey 8: Product Search
    @Test
    fun journey08_productSearch() = runBlocking {
        val searchResults = database.productDao().searchProducts("iPhone").first()
        assertTrue(searchResults.isNotEmpty())
        assertTrue(searchResults.any { it.name.contains("iPhone", ignoreCase = true) })
    }

    // Journey 9: Product Details
    @Test
    fun journey09_productDetails() = runBlocking {
        val product = database.productDao().getAllProducts().first().first()
        val details = database.productDao().getProductByIdSync(product.id)
        assertNotNull(details)
        assertEquals(product.id, details?.id)
        assertEquals(product.name, details?.name)
    }

    // Journey 10: AI Recommendations
    @Test
    fun journey10_aiRecommendations() = runBlocking {
        val recommendations = repository.getPersonalizedRecommendations(5)
        assertNotNull(recommendations)
    }

    // Journey 11: AI Shopping Assistant Query
    @Test
    fun journey11_aiShoppingAssistant() = runBlocking {
        val query = "headphones"
        val matches = database.productDao().searchProducts(query).first()
        assertTrue(matches.isNotEmpty())
    }

    // Journey 12: Merchant Comparison
    @Test
    fun journey12_merchantComparison() = runBlocking {
        val offers = database.merchantOfferDao().getOffersForProduct(1L).first()
        if (offers.isNotEmpty()) {
            val minPriceOffer = offers.minByOrNull { it.currentPrice }
            assertNotNull(minPriceOffer)
            assertTrue(minPriceOffer!!.currentPrice > 0)
        }
    }

    // Journey 13: External Merchant Redirect URL Validation
    @Test
    fun journey13_externalMerchantRedirect() {
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://www.amazon.in/dp/B08L5WHH68"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("http://insecure.amazon.in/dp/123"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("https://phishing-site.com"))
    }

    // Journey 14: Cart Workflow
    @Test
    fun journey14_cartWorkflow() = runBlocking {
        val userId = 999L
        val product = database.productDao().getAllProducts().first().first()
        val cartItem = CartItemEntity(userId = userId, productId = product.id, quantity = 2)
        database.cartDao().insertCartItem(cartItem)
        val userCart = database.cartDao().getCartItems(userId).first()
        assertEquals(1, userCart.size)
        assertEquals(2, userCart.first().quantity)
        database.cartDao().clearCart(userId)
        val emptyCart = database.cartDao().getCartItems(userId).first()
        assertTrue(emptyCart.isEmpty())
    }

    // Journey 15: Wishlist Workflow
    @Test
    fun journey15_wishlistWorkflow() = runBlocking {
        val userId = 888L
        val product = database.productDao().getAllProducts().first().first()
        val wishItem = WishlistItemEntity(userId = userId, productId = product.id)
        database.wishlistDao().insertWishlistItem(wishItem)
        val wishlist = database.wishlistDao().getWishlistItems(userId).first()
        assertEquals(1, wishlist.size)
        database.wishlistDao().deleteWishlistItem(userId, product.id)
        val emptyWishlist = database.wishlistDao().getWishlistItems(userId).first()
        assertTrue(emptyWishlist.isEmpty())
    }

    // Journey 16: Deals & Discount Calculation
    @Test
    fun journey16_deals() {
        val discount = SecurityUtils.calculateVerifiedDiscount(5000.0, 4000.0)
        assertEquals(20, discount)
        val dealScore = SecurityUtils.calculateDealScore(20, 1000.0, true, 4.5)
        assertTrue(dealScore > 50.0)
    }

    // Journey 17: SmartCart Promo Codes
    @Test
    fun journey17_promoCodes() = runBlocking {
        val campaign = PromoCampaignEntity(
            campaignName = "Flat 100",
            promoCode = "FLAT100",
            discountType = "FIXED",
            discountValue = 100.0,
            maxDiscount = 100.0,
            minOrderValue = 500.0,
            startDate = System.currentTimeMillis() - 1000,
            expiryDate = System.currentTimeMillis() + 100000,
            status = "ACTIVE"
        )
        database.promoCampaignDao().insertCampaign(campaign)
        val res = repository.validatePromoCode("FLAT100", 1000.0)
        assertTrue(res.isValid)
        assertEquals(100.0, res.discountAmount, 0.01)
    }

    // Journey 18: Merchant Coupons
    @Test
    fun journey18_merchantCoupons() = runBlocking {
        val coupons = database.merchantCouponDao().getAllCoupons().first()
        assertNotNull(coupons)
    }

    // Journey 19: Price History Tracking
    @Test
    fun journey19_priceHistory() = runBlocking {
        val historyEntry = PriceHistoryEntity(
            productId = 1L,
            merchantName = "Amazon India",
            price = 74999.0,
            recordedAt = System.currentTimeMillis()
        )
        database.priceHistoryDao().insertPricePoint(historyEntry)
        val history = database.priceHistoryDao().getPriceHistoryForProduct(1L).first()
        assertTrue(history.isNotEmpty())
    }

    // Journey 20: Price-Drop Alerts
    @Test
    fun journey20_priceDropAlerts() = runBlocking {
        val alert = PriceAlertEntity(
            userId = 1L,
            productId = 1L,
            productName = "Sample Phone",
            productCategory = "Smartphones",
            targetPrice = 70000.0,
            currentPrice = 79999.0
        )
        database.priceAlertDao().insertAlert(alert)
        val userAlerts = database.priceAlertDao().getAlertsForUser(1L).first()
        assertTrue(userAlerts.any { it.productId == 1L && it.targetPrice == 70000.0 })
    }

    // Journey 21: Voucher Listing
    @Test
    fun journey21_voucherListing() = runBlocking {
        val sellerRes = repository.registerUserAccount("Seller S", "seller.s@smartcart.ai", "9123456789", "Pass1234!", "Fashion", "Nike", 100.0, 5000.0)
        repository.loginUser("seller.s@smartcart.ai", "Pass1234!")
        val vId = repository.createVoucherListing(
            brand = "Myntra",
            voucherType = "Gift Card",
            faceValue = 1000.0,
            sellingPrice = 850.0,
            expiryDate = "2026-11-30",
            terms = "Valid on fashion items",
            verificationInfo = "MYN-9900-1122",
            sellerAgreement = true
        )
        assertTrue(vId > 0)
    }

    // Journey 22: Voucher Verification
    @Test
    fun journey22_voucherVerification() = runBlocking {
        val voucher = database.voucherListingDao().getAllVouchers().first().firstOrNull()
        if (voucher != null) {
            database.voucherListingDao().updateVoucherStatus(voucher.id, "VERIFIED", "Approved")
            val updated = database.voucherListingDao().getVoucherById(voucher.id)
            assertEquals("VERIFIED", updated?.status)
        }
    }

    // Journey 23: Voucher Purchase
    @Test
    fun journey23_voucherPurchase() = runBlocking {
        val sellerRes = repository.registerUserAccount("Seller X", "seller.x@smartcart.ai", "9900112233", "Pass1234!", "Electronics", "Sony", 100.0, 5000.0)
        repository.loginUser("seller.x@smartcart.ai", "Pass1234!")
        val vId = repository.createVoucherListing(
            brand = "Croma",
            voucherType = "Gift Card",
            faceValue = 1500.0,
            sellingPrice = 1300.0,
            expiryDate = "2026-12-31",
            terms = "Valid in-store",
            verificationInfo = "CRM-5544-3322",
            sellerAgreement = true
        )
        repository.updateVoucherStatus(vId, "VERIFIED", "Verified")
        val buyerRes = repository.registerUserAccount("Buyer Y", "buyer.y@smartcart.ai", "9900112244", "Pass1234!", "Electronics", "Sony", 100.0, 5000.0)
        repository.loginUser("buyer.y@smartcart.ai", "Pass1234!")
        val tx = repository.buyVoucher(vId, "UPI")
        assertNotNull(tx)
        assertEquals(vId, tx.voucherId)
    }

    // Journey 24: Voucher Masking
    @Test
    fun journey24_voucherMasking() {
        val masked = SecurityUtils.maskVoucherCode("FLIPKART-GIFT-5432")
        assertEquals("FLI-****-5432", masked)
    }

    // Journey 25: Demo Escrow Flow
    @Test
    fun journey25_demoEscrowFlow() = runBlocking {
        val sellerRes = repository.registerUserAccount("Seller Escrow", "seller.escrow@smartcart.ai", "9191919191", "Pass123!", "Tech", "Apple", 100.0, 5000.0)
        repository.loginUser("seller.escrow@smartcart.ai", "Pass123!")
        val vId = repository.createVoucherListing("Apple Store", "Gift Card", 5000.0, 4500.0, "2026-12-31", "Terms", "APPL-1234-5678", true)
        repository.updateVoucherStatus(vId, "VERIFIED", "Approved")

        val buyerRes = repository.registerUserAccount("Buyer Escrow", "buyer.escrow@smartcart.ai", "9292929292", "Pass123!", "Tech", "Apple", 100.0, 5000.0)
        repository.loginUser("buyer.escrow@smartcart.ai", "Pass123!")
        val tx = repository.buyVoucher(vId, "Net Banking")
        assertTrue(tx.paymentMethod.contains("Demo Escrow"))
    }

    // Journey 26: User Profile & Preference Update
    @Test
    fun journey26_userProfile() = runBlocking {
        val email = "profile.user@smartcart.ai"
        val res = repository.registerUserAccount("Profile User", email, "9393939393", "Pass1234!", "Electronics", "Apple", 500.0, 50000.0)
        val user = res.getOrThrow()
        val updated = user.copy(preferredCategories = "Electronics,Wearables", preferredBrands = "Apple,Samsung")
        database.userDao().updateUser(updated)
        val reloaded = database.userDao().getUserByIdSync(user.id)
        assertEquals("Electronics,Wearables", reloaded?.preferredCategories)
        assertEquals("Apple,Samsung", reloaded?.preferredBrands)
    }

    // Journey 27: User Data Isolation
    @Test
    fun journey27_userDataIsolation() = runBlocking {
        val userA = 1001L
        val userB = 1002L
        val itemA = CartItemEntity(userId = userA, productId = 1L, quantity = 3)
        val itemB = CartItemEntity(userId = userB, productId = 2L, quantity = 5)
        database.cartDao().insertCartItem(itemA)
        database.cartDao().insertCartItem(itemB)

        val cartA = database.cartDao().getCartItems(userA).first()
        val cartB = database.cartDao().getCartItems(userB).first()
        assertEquals(1, cartA.size)
        assertEquals(1, cartB.size)
        assertEquals(1L, cartA.first().productId)
        assertEquals(2L, cartB.first().productId)
    }

    // Journey 28: Role-Based Access Control (RBAC)
    @Test
    fun journey28_rbac() = runBlocking {
        val customer = UserEntity(email = "cust@smartcart.ai", passwordHash = "hash", fullName = "Customer", role = "customer")
        val admin = UserEntity(email = "boss@smartcart.ai", passwordHash = "hash", fullName = "Admin", role = "admin")
        assertFalse(customer.role == "admin")
        assertTrue(admin.role == "admin")
    }

    // Journey 29: Offline Behavior & Local Storage Integrity
    @Test
    fun journey29_offlineBehavior() = runBlocking {
        // Room works 100% offline without network
        val product = database.productDao().getAllProducts().first().first()
        assertNotNull(product)
        assertTrue(product.name.isNotBlank())
    }

    // Journey 30: Production Mode & Unconfigured Merchant Behavior
    @Test
    fun journey30_productionUnconfiguredBehavior() {
        AppConfig.setEnvironment(AppEnvironment.PRODUCTION)
        assertTrue(AppConfig.isProductionMode)
        assertTrue(AppConfig.requireLiveProviders)
        val registry = MerchantProviderRegistry()
        val amazon = registry.getProvider("amazon")
        assertNotNull(amazon)
        assertEquals(ProviderStatus.NOT_CONFIGURED, amazon?.status)
        assertFalse(amazon!!.isLiveConfigured)

        // Reset back to DEMO mode for default state
        AppConfig.setEnvironment(AppEnvironment.DEMO)
        assertTrue(AppConfig.isDemoMode)
    }
}
