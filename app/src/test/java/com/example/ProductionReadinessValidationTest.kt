package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.config.AppConfig
import com.example.config.AppEnvironment
import com.example.config.ProviderStatus
import com.example.data.database.AppDatabase
import com.example.data.model.MerchantOfferEntity
import com.example.data.model.PromoCampaignEntity
import com.example.data.model.toNormalized
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProductionReadinessValidationTest {

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

    // --- 1. Password Hashing & Authentication Security ---

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "P@ssw0rdSecure2026!"
        val hash = SecurityUtils.hashPassword(rawPassword)

        assertNotEquals(rawPassword, hash)
        assertEquals(64, hash.length) // SHA-256 hex string length
        assertTrue(SecurityUtils.verifyPassword(rawPassword, hash))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", hash))
    }

    @Test
    fun testUserRegistrationAndLoginFlow() = runBlocking {
        val email = "alice.test@smartcart.ai"
        val password = "SecurePassword123"
        val fullName = "Alice Test"

        // 1. Register
        val regResult = repository.registerUserAccount(
            fullName = fullName,
            email = email,
            phone = "9876543210",
            password = password,
            preferredCategories = "Electronics,Fashion",
            preferredBrands = "Apple,Nike",
            minPrice = 500.0,
            maxPrice = 50000.0
        )
        assertTrue(regResult.isSuccess)
        val user = regResult.getOrNull()
        assertNotNull(user)
        assertEquals(email, user?.email)
        assertEquals(fullName, user?.fullName)
        assertNotEquals(password, user?.passwordHash) // Plain text must NEVER be stored

        // 2. Login with correct password
        val loginResult = repository.loginUser(email, password)
        assertTrue(loginResult.isSuccess)
        assertNotNull(repository.currentUser.value)
        assertEquals(user?.id, repository.currentUser.value?.id)

        // 3. Login with incorrect password
        val loginFail = repository.loginUser(email, "InvalidPassword")
        assertFalse(loginFail.isSuccess)

        // 4. Duplicate registration rejected
        val dupResult = repository.registerUserAccount(
            fullName = "Alice Duplicate",
            email = email,
            phone = "9876543210",
            password = password,
            preferredCategories = "Electronics",
            preferredBrands = "Apple",
            minPrice = 500.0,
            maxPrice = 50000.0
        )
        assertTrue(dupResult.isFailure)
    }

    // --- 2. Merchant URL Allowlisting & HTTPS Validation ---

    @Test
    fun testMerchantUrlSecurityValidation() {
        // Approved HTTPS domains
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://www.amazon.in/dp/B08L5WHH68"))
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://amazon.com/product/123"))
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://dl.flipkart.com/dl/p/item"))
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://www.croma.com/apple-iphone/p/123"))
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://myntra.com/shoes/nike"))
        assertTrue(MerchantUrlSecurityValidator.isAllowedRedirect("https://www.reliancedigital.com/tv/p/456"))

        // Insecure HTTP MUST be rejected
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("http://www.amazon.in/dp/B08L5WHH68"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("http://www.flipkart.com/p"))

        // Unapproved and phishing domains MUST be rejected
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("https://phishing-site.com/amazon"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("https://evil-hacker.org"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("javascript:alert(1)"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect(""))
    }

    // --- 3. Verified Discount & Deal Scoring Logic ---

    @Test
    fun testVerifiedDiscountCalculation() {
        // Regular 20% discount
        assertEquals(20, SecurityUtils.calculateVerifiedDiscount(1000.0, 800.0))
        // Zero or negative discount (selling price >= original price)
        assertEquals(0, SecurityUtils.calculateVerifiedDiscount(1000.0, 1000.0))
        assertEquals(0, SecurityUtils.calculateVerifiedDiscount(1000.0, 1200.0))
        // Invalid original price <= 0
        assertEquals(0, SecurityUtils.calculateVerifiedDiscount(0.0, 100.0))
        assertEquals(0, SecurityUtils.calculateVerifiedDiscount(-100.0, 50.0))
    }

    @Test
    fun testDealScoringCalculation() {
        val highConfidenceDeal = SecurityUtils.calculateDealScore(
            discountPercent = 40,
            priceDropAmount = 2000.0,
            isLowestIn30Days = true,
            merchantRating = 4.8
        )
        val lowConfidenceDeal = SecurityUtils.calculateDealScore(
            discountPercent = 5,
            priceDropAmount = 50.0,
            isLowestIn30Days = false,
            merchantRating = 2.5
        )
        assertTrue(highConfidenceDeal > lowConfidenceDeal)
        assertTrue(highConfidenceDeal <= 100.0)
        assertTrue(lowConfidenceDeal >= 0.0)
    }

    // --- 4. Voucher Code Masking & Peer-to-Peer Lifecycle ---

    @Test
    fun testVoucherCodeMasking() {
        assertEquals("AMZ-****-1234", SecurityUtils.maskVoucherCode("AMZN-ABCD-1234"))
        assertEquals("****", SecurityUtils.maskVoucherCode("ABC"))
        assertEquals("****", SecurityUtils.maskVoucherCode("ABCD"))
        assertEquals("****", SecurityUtils.maskVoucherCode(""))
    }

    @Test
    fun testVoucherListingVerificationAndPurchaseWithDemoEscrow() = runBlocking {
        // 1. Register Seller & Buyer
        val sellerRes = repository.registerUserAccount(
            fullName = "Seller Bob",
            email = "bob@smartcart.ai",
            phone = "9876543211",
            password = "BobPass123!",
            preferredCategories = "Electronics",
            preferredBrands = "Amazon",
            minPrice = 100.0,
            maxPrice = 10000.0
        )
        val buyerRes = repository.registerUserAccount(
            fullName = "Buyer Charlie",
            email = "charlie@smartcart.ai",
            phone = "9876543212",
            password = "CharliePass123!",
            preferredCategories = "Electronics",
            preferredBrands = "Amazon",
            minPrice = 100.0,
            maxPrice = 10000.0
        )
        val seller = sellerRes.getOrThrow()
        val buyer = buyerRes.getOrThrow()

        // 2. Seller logs in and lists voucher
        repository.loginUser(seller.email, "BobPass123!")
        val voucherId = repository.createVoucherListing(
            brand = "Amazon Pay",
            voucherType = "Gift Card",
            faceValue = 2000.0,
            sellingPrice = 1700.0,
            expiryDate = "2026-12-31",
            terms = "Valid across Amazon India on all items.",
            verificationInfo = "AMZN-CLAIM-9988",
            sellerAgreement = true
        )
        assertTrue(voucherId > 0)

        // 3. Newly submitted voucher is PENDING_VERIFICATION and not yet publicly verified
        val pendingVouchers = repository.getPendingVerificationVouchers().first()
        assertTrue(pendingVouchers.any { it.id == voucherId })
        val verifiedBeforeApproval = repository.getVerifiedVouchers().first()
        assertFalse(verifiedBeforeApproval.any { it.id == voucherId })

        // 4. Admin approves / verifies voucher
        repository.updateVoucherStatus(voucherId, "VERIFIED", "Approved by compliance team")
        val verifiedAfterApproval = repository.getVerifiedVouchers().first()
        assertTrue(verifiedAfterApproval.any { it.id == voucherId })

        // 5. Buyer logs in and purchases voucher
        repository.loginUser(buyer.email, "CharliePass123!")
        val transaction = repository.buyVoucher(voucherId, "UPI / Card")
        assertNotNull(transaction)
        assertEquals(buyer.id, transaction.buyerUserId)
        assertEquals(seller.id, transaction.sellerUserId)
        assertEquals(1700.0, transaction.amountPaid, 0.01)
        assertTrue(transaction.paymentMethod.contains("Demo Escrow"))

        // 6. Voucher is now SOLD and removed from verified active marketplace
        val verifiedAfterSale = repository.getVerifiedVouchers().first()
        assertFalse(verifiedAfterSale.any { it.id == voucherId })

        // 7. Double-purchase prevention: Attempting to buy again must fail
        try {
            repository.buyVoucher(voucherId, "UPI / Card")
            fail("Expected IllegalStateException: Voucher is no longer available")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("no longer available") == true)
        }
    }

    // --- 5. Promo Code Engine Validation ---

    @Test
    fun testPromoCodeEngineValidation() = runBlocking {
        // Insert test campaign
        val campaign = PromoCampaignEntity(
            campaignName = "Smart Welcome 10",
            promoCode = "SMART10",
            discountType = "PERCENTAGE",
            discountValue = 10.0,
            maxDiscount = 500.0,
            minOrderValue = 2000.0,
            startDate = System.currentTimeMillis() - 10000,
            expiryDate = System.currentTimeMillis() + 86400000,
            status = "ACTIVE"
        )
        repository.createPromoCampaign(campaign)

        // Test below minimum order
        val belowMin = repository.validatePromoCode("SMART10", 1500.0)
        assertFalse(belowMin.isValid)
        assertTrue(belowMin.message.contains("Minimum cart value"))

        // Test valid application within limits (₹3000 -> 10% = ₹300 <= ₹500 max)
        val valid = repository.validatePromoCode("SMART10", 3000.0)
        assertTrue(valid.isValid)
        assertEquals(300.0, valid.discountAmount, 0.01)

        // Test capped discount (₹8000 -> 10% = ₹800, capped at ₹500 max)
        val capped = repository.validatePromoCode("SMART10", 8000.0)
        assertTrue(capped.isValid)
        assertEquals(500.0, capped.discountAmount, 0.01)

        // Test invalid code
        val invalid = repository.validatePromoCode("INVALID_CODE", 5000.0)
        assertFalse(invalid.isValid)
    }

    // --- 6. Merchant Provider Registry Isolation ---

    @Test
    fun testMerchantProviderRegistryIsolation() {
        val registry = MerchantProviderRegistry()
        assertEquals(5, registry.providers.size)

        val amazon = registry.getProvider("amazon")
        assertNotNull(amazon)
        assertEquals("Amazon India", amazon?.merchantName)
        assertFalse(amazon!!.isLiveConfigured) // Sandbox mode must be labeled as unconfigured

        val flipkart = registry.getProvider("flipkart")
        assertNotNull(flipkart)
        assertEquals("Flipkart", flipkart?.merchantName)
        assertFalse(flipkart!!.isLiveConfigured)

        val croma = registry.getProvider("croma")
        assertNotNull(croma)
        assertEquals("Croma", croma?.merchantName)
    }

    // --- 7. Release-03: Environment Mode Validation (Demo vs Production) ---

    @Test
    fun testAppEnvironmentConfigurationAndToggling() {
        // Default environment is DEMO
        AppConfig.setEnvironment(AppEnvironment.DEMO)
        assertTrue(AppConfig.isDemoMode)
        assertFalse(AppConfig.isProductionMode)
        assertTrue(AppConfig.allowDemoData)
        assertFalse(AppConfig.requireLiveProviders)
        assertTrue(AppConfig.environmentBannerText.contains("DEMO MODE"))

        // Toggle to PRODUCTION
        AppConfig.toggleEnvironment()
        assertTrue(AppConfig.isProductionMode)
        assertFalse(AppConfig.isDemoMode)
        assertFalse(AppConfig.allowDemoData)
        assertTrue(AppConfig.requireLiveProviders)
        assertTrue(AppConfig.environmentBannerText.contains("PRODUCTION MODE"))

        // Reset back to DEMO
        AppConfig.setEnvironment(AppEnvironment.DEMO)
        assertTrue(AppConfig.isDemoMode)
    }

    // --- 8. Release-03: All 8 Audited Merchant Providers ---

    @Test
    fun testAllEightAuditedMerchantProvidersInRegistry() {
        val registry = MerchantProviderRegistry()
        assertEquals(8, registry.allSupportedProviders.size)

        val expectedMerchants = listOf(
            "amazon" to "Amazon India",
            "flipkart" to "Flipkart",
            "myntra" to "Myntra",
            "croma" to "Croma",
            "reliance" to "Reliance Digital",
            "apple" to "Apple Store",
            "tatacliq" to "Tata CliQ",
            "ajio" to "Ajio"
        )

        for ((query, expectedName) in expectedMerchants) {
            val provider = registry.getProvider(query)
            assertNotNull("Provider for '$query' must not be null", provider)
            assertEquals(expectedName, provider?.merchantName)
            assertEquals(ProviderStatus.NOT_CONFIGURED, provider?.status)
            assertTrue(provider!!.integrationType.isNotBlank())
            assertTrue(provider.domain.isNotBlank())
        }
    }

    // --- 9. Release-03: Merchant Offer Normalization ---

    @Test
    fun testNormalizedMerchantOfferMapping() {
        val rawEntity = MerchantOfferEntity(
            id = 1,
            merchantId = 101,
            productId = 501,
            merchantName = "Amazon India",
            currentPrice = 79999.0,
            originalPrice = 89999.0,
            discountPercent = 11,
            couponCode = "SAVE1000",
            couponDiscount = "₹1,000 off with HDFC Card",
            externalUrl = "https://www.amazon.in/dp/B08L5WHH68",
            affiliateUrl = "https://www.amazon.in/dp/B08L5WHH68?tag=smartcart-21",
            availability = "In Stock",
            deliveryInfo = "Prime 1-day delivery",
            isLive = false,
            lastUpdated = 1700000000000L
        )

        val normalized = rawEntity.toNormalized("Apple iPhone 15")
        assertEquals(501L, normalized.productId)
        assertEquals(101L, normalized.merchantId)
        assertEquals("Amazon India", normalized.merchantName)
        assertEquals("Apple iPhone 15", normalized.title)
        assertEquals(79999.0, normalized.price, 0.01)
        assertEquals(89999.0, normalized.originalPrice)
        assertEquals(11, normalized.discount)
        assertEquals("INR", normalized.currency)
        assertEquals("In Stock", normalized.availability)
        assertEquals("Prime 1-day delivery", normalized.deliveryEstimate)
        assertEquals("https://www.amazon.in/dp/B08L5WHH68", normalized.productUrl)
        assertEquals("https://www.amazon.in/dp/B08L5WHH68?tag=smartcart-21", normalized.affiliateUrl)
        assertFalse(normalized.isVerified) // isLive == false
        assertEquals("Demo Catalog Integration", normalized.source)
    }

    // --- 10. Release-03: All Audited Merchant Domains in Security Allowlist ---

    @Test
    fun testAllAuditedMerchantDomainsAllowedInSecurityValidator() {
        val auditedDomains = listOf(
            "https://www.amazon.in/dp/123",
            "https://amazon.com/dp/123",
            "https://dl.flipkart.com/dl/p/123",
            "https://flipkart.com/p/123",
            "https://www.croma.com/p/123",
            "https://myntra.com/p/123",
            "https://www.tatacliq.com/p/123",
            "https://www.ajio.com/p/123",
            "https://reliancedigital.in/p/123",
            "https://www.reliancedigital.com/p/123",
            "https://www.apple.com/in/shop/buy-iphone"
        )

        for (url in auditedDomains) {
            assertTrue("Domain in '$url' must be allowed", MerchantUrlSecurityValidator.isAllowedRedirect(url))
        }

        // Must reject insecure HTTP and phishing
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("http://www.tatacliq.com/p/123"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("https://tatacliq.fake-scam.com/p/123"))
        assertFalse(MerchantUrlSecurityValidator.isAllowedRedirect("https://ajio.phishing.org/offer"))
    }
}
