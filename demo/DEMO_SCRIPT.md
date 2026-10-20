# Shop Mind AI — Comprehensive Demonstration & Presentation Script

**Project Baseline:** `SHOPMIND-RELEASE-CANDIDATE-04`  
**Application ID:** `com.aistudio.smartcart.ai`  
**Presenter:** Project Team  
**Audience:** External Examiner, Project Guide, Department Evaluation Committee  
**Total Estimated Time:** 12–15 Minutes  

---

## Pre-Demonstration Setup & Environment Readiness Checklist

1. **Device / Emulator:** Android Streaming Emulator or physical device running Android 7.0+ (API 24 to 36).
2. **Network Connection:** Active Internet connection (for Coil image loading, external merchant domain resolution, and Gemini shopping queries).
3. **Build Status:** Verified passing `gradle :app:testDebugUnitTest` and `compile_applet`.
4. **Initial App State:** Database automatically seeds default catalog items, mock multi-merchant comparison points, and sample P2P vouchers.

---

## Act 1: Initial Launch & System Introduction (Demo vs Production Architecture)

### 🎙️ Spoken Script:
> "Respected members of the committee, welcome to the demonstration of **Shop Mind AI**, an intelligent, multi-merchant e-commerce and secondary voucher ecosystem built natively for Android using Jetpack Compose, Kotlin Coroutines, and Room local persistence.
> 
> Before showing any feature, I would like to draw your attention to our **Environment Configuration Banner** at the top of the screen. Notice that it currently displays **'DEMO MODE — Sample catalog & simulated offers for evaluation'**.
> 
> In enterprise engineering, honest truth-in-advertising is paramount. When we toggle the environment to **PRODUCTION MODE**, notice how the system dynamically changes: all simulated merchant offers are suppressed. For merchants without live authorized API credentials, Shop Mind AI honestly reports that live merchant data is not yet configured, rather than fabricating fake prices, inventory, or delivery dates.
> 
> For the purposes of this walkthrough, we will demonstrate the end-to-end user journeys in **DEMO MODE**."

### 📱 Action on Screen:
1. Point to the top environment banner in the Home or Settings screen.
2. Toggle the Environment switch to **PRODUCTION** mode.
3. Open a product details page: observe that merchant comparison displays the unconfigured provider badge (`NOT_CONFIGURED: Live merchant data is not currently configured`).
4. Toggle back to **DEMO MODE** to re-enable evaluation sample feeds.

---

## Act 2: User Onboarding, Registration & Secure Authentication

### 🎙️ Spoken Script:
> "Now let us register a new shopper account to demonstrate our authentication architecture, cryptographic password hashing, and user data isolation.
> 
> When entering a password, Shop Mind AI never stores plain text credentials. The app immediately salts and hashes the password using SHA-256 in `SecurityUtils.kt`. Additionally, every registered user is allocated a unique, sequential enterprise identity, such as `SC-USER-000001`.
> 
> Notice also during registration that the user selects their preferred shopping categories—such as Smartphones and Audio—and preferred brands. This preference vector directly bootstraps our on-device AI recommendation engine."

### 📱 Action on Screen:
1. Navigate to the **Profile / Auth** screen.
2. Tap **Create Account**.
3. Register with:
   - Full Name: `Prof. Sharma`
   - Email: `sharma.eval@smartcart.ai`
   - Password: `Password@2026`
   - Select Preferred Categories: `Smartphones`, `Laptops`, `Audio`
   - Preferred Brands: `Apple`, `Sony`
4. Tap **Register**.
5. Observe successful login and point out the generated ID badge: `SC-USER-XXXXXX`.

---

## Act 3: Intelligent Product Discovery & Multi-Category Browsing

### 🎙️ Spoken Script:
> "Now let us explore the catalog discovery pipeline. The application organizes products across 10 distinct categories.
> 
> As we scroll through the catalog, notice the fluid 60 FPS scrolling provided by Jetpack Compose `LazyColumn` and asynchronous image caching with crossfade transitions powered by Coil.
> 
> Let's perform a live keyword search for 'Headphones'. Room SQLite executes an indexed full-text query, returning instant results with zero UI hitching."

### 📱 Action on Screen:
1. On the **Home** screen, tap across category chips: `Electronics`, `Audio`, `Laptops`, `Wearables`.
2. Enter `"Sony"` or `"AirPods"` in the top search bar.
3. Observe instant filtered results with matching badges and prices.

---

## Act 4: Gemini-Powered Shopping Assistant & Recommendation Engine

### 🎙️ Spoken Script:
> "Now we navigate to the **AI Assistant** tab. Shop Mind AI features a hybrid recommendation engine.
> 
> On the Home tab, the app calculates affinity scores using our content-based algorithm: matching the user's saved preferences with product category and brand tags.
> 
> Here in the Assistant tab, shoppers can converse in natural human language. Let us ask: *'I need noise-cancelling headphones for frequent travel under 30000'*.
> 
> The shopping assistant evaluates the prompt, filters catalog specifications, and provides contextual reasoning along with clickable product recommendation cards."

### 📱 Action on Screen:
1. Tap the **AI Assistant** icon in the bottom navigation bar.
2. Type or select the quick query: *"Recommend noise-cancelling headphones for long flights"*.
3. Tap **Send**.
4. View the AI response card featuring the recommended audio device with direct link to details.

---

## Act 5: Real-Time Multi-Merchant Price Comparison & Secure Redirection

### 🎙️ Spoken Script:
> "Let us tap on the flagship **iPhone 15 Pro** to open the Product Details view.
> 
> Instead of locking the shopper into a single vendor, Shop Mind AI aggregates offers across **8 supported merchant providers**: Amazon India, Flipkart, Croma, Reliance Digital, Apple Store, Myntra, Tata CliQ, and Ajio.
> 
> Each offer is normalized into our domain structure `NormalizedMerchantOffer`. The lowest price is dynamically highlighted with a 'Best Price' green badge.
> 
> When the user decides to buy from Amazon or Croma, our `MerchantUrlSecurityValidator` strictly validates that the URL uses HTTPS and belongs exclusively to the verified merchant domain before launching Android's external browser Intent. Phishing domains and insecure HTTP links are blocked automatically."

### 📱 Action on Screen:
1. Tap the product card for **iPhone 15 Pro** or **Sony WH-1000XM5**.
2. Scroll to the **Merchant Comparison** section.
3. Point out the multiple merchant cards (e.g. Amazon India vs Croma vs Reliance Digital).
4. Tap **Buy on Amazon**.
5. Observe system browser intent launching safely with the validated merchant link.

---

## Act 6: Dynamic Price History & Price-Drop Alerts

### 🎙️ Spoken Script:
> "Still on the product details page, let us inspect the **Price History** card.
> 
> Shop Mind AI tracks time-series pricing data for every SKU. Users can observe historical price trends over 30 and 90 days to determine whether a current deal is genuinely attractive or artificially inflated.
> 
> Furthermore, users can tap **'Notify on Price Drop'** to set an automated threshold alert. When the price dips below their chosen limit, Room flags a dynamic alert for the user's active session."

### 📱 Action on Screen:
1. Scroll to the **Price History Graph / Chart** section.
2. Highlight the lowest historical point vs current price.
3. Tap **Set Price Drop Alert**.
4. Confirm notification configuration dialog.

---

## Act 7: Cart Management, Promo Codes & Merchant Coupons

### 🎙️ Spoken Script:
> "Let us add this product to our Cart and navigate to the Cart screen.
> 
> Notice how the checkout drawer calculates:
> 1. Catalog Gross Total
> 2. Applied SmartCart Platform Promo Code (e.g. `SMART10` for 10% off)
> 3. Partner Merchant Instant Discount
> 4. Final Net Payable Amount
> 
> Users can apply and validate custom discount codes in real time, with instant calculation updates."

### 📱 Action on Screen:
1. Tap **Add to Cart**.
2. Tap the **Cart** icon in the bottom navigation bar.
3. Enter promo code `SMART10` or `FESTIVE2026`.
4. Tap **Apply**; observe the discount line item reflecting immediate net savings.

---

## Act 8: P2P Voucher Marketplace, Concurrency Mutex & Demo Escrow

### 🎙️ Spoken Script:
> "Now let us present one of our key original contributions: the **Peer-to-Peer Voucher Marketplace with Demo Escrow**.
> 
> Often, consumers possess unspent brand vouchers (e.g., a ₹2,000 Myntra or Croma gift card) that they cannot liquidate. Here, users can list vouchers for other shoppers to buy at a discount.
> 
> To protect both buyers and sellers, we engineered two safeguards:
> 1. **Concurrency Protection:** In `SmartCartRepository`, voucher purchases are wrapped inside a Kotlin Coroutine `Mutex` (`voucherPurchaseMutex.withLock`). If two shoppers tap 'Buy' at the exact same millisecond, the lock ensures the voucher transitions atomically to `IN_ESCROW`. The second attempt is safely rejected, preventing double-spending.
> 2. **Demo Escrow:** Once purchased, buyer funds are held in sandbox escrow. The buyer receives the masked voucher code, verifies it with the merchant, and confirms release. Only then does the status transition to `SOLD`."

### 📱 Action on Screen:
1. Tap the **Vouchers** tab.
2. Point out available vouchers with discount percentages (e.g. "₹1,000 Amazon Gift Card for ₹850").
3. Tap **Buy Voucher**.
4. Observe the status transition to **'Held in Demo Escrow'**.
5. Tap **Verify & Release Escrow**.
6. Observe the unmasked redemption code displayed securely.

---

## Act 9: Role-Based Access Control (Admin Dashboard)

### 🎙️ Spoken Script:
> "Finally, let us demonstrate Role-Based Access Control (RBAC).
> 
> We will switch to the Admin profile (`admin@smartcart.ai`). Notice that the UI dynamically unlocks the **Admin Management Console**.
> 
> Administrators have elevated privileges:
> - Auditing the connection health and credentials of all 8 merchant providers.
> - Monitoring active P2P escrow transactions.
> - Adding new catalog products and broadcast promotional campaigns.
> Regular shoppers cannot access these administrative endpoints."

### 📱 Action on Screen:
1. Navigate to **Profile** and switch account to Admin.
2. Tap the unlocked **Admin Console**.
3. Show the provider health list (Amazon, Flipkart, Croma, etc. with status badges).
4. Review the Escrow transactions overview table.

---

## Act 10: Technical Verification & Conclusion

### 🎙️ Spoken Script:
> "To conclude, Shop Mind AI delivers a fully hardened, test-proven Android application with:
> - Clean MVVM architecture and 100% Jetpack Compose UI.
> - Full offline-first Room persistence with 8 indexed entity tables.
> - Comprehensive test suite passing with 100% success rate on Robolectric JVM.
> - Strict environment separation between Demo and Production modes.
> 
> We are now ready to take questions from the examination committee. Thank you!"

### 📱 Action on Screen:
1. Show the `FINAL_RELEASE_FREEZE.md` or test terminal showing `BUILD SUCCESSFUL`.
2. Invite questions from the examiners.
