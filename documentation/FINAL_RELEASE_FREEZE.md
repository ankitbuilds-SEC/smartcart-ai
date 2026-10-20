# Shop Mind AI — Final Release Freeze & Architecture Baseline

**Baseline Checkpoint:** `SHOPMIND-RELEASE-CANDIDATE-04`  
**Application ID:** `com.aistudio.smartcart.ai`  
**Version:** `1.0` (`versionCode = 1`)  
**Target SDK:** `36` | **Min SDK:** `24` | **Compile SDK:** `36` (minorApiLevel = 1)  
**Database Schema Version:** `1` (Room SQLite)  
**Status:** SOURCE CODE FROZEN — READY FOR FINAL SUBMISSION & DEMO  
**Date:** September 2026  

---

## 1. Executive Freeze Summary

The source code of **Shop Mind AI** is officially frozen at checkpoint **`SHOPMIND-RELEASE-CANDIDATE-04`**. All architectural tiers, business logic pipelines, UI screens, database schemas, cryptographic safeguards, and testing harnesses have been thoroughly verified.

No untested dependencies, unstable prototypes, or fabricated live APIs are present in the frozen release. The project strictly separates **DEMO mode** (sandboxed evaluation with labeled mock offers) from **PRODUCTION mode** (honest unconfigured provider states with zero fabricated pricing or inventory).

---

## 2. Technical Baseline & Build Specifications

| Parameter | Value / Tooling | Notes |
|---|---|---|
| **Project Baseline** | `SHOPMIND-RELEASE-CANDIDATE-04` | Successor to `SMARTCART-HARDENED-02` |
| **Application ID** | `com.aistudio.smartcart.ai` | Unique production application identifier |
| **Version Name / Code** | `1.0` / `1` | Semantic versioning 1.0 |
| **Language & Toolchain** | Kotlin `2.0.21` + Compose Compiler | AGP 8.x, Java 11 bytecode target |
| **UI Framework** | Jetpack Compose BOM `2024.10.00` | Material Design 3 (M3) design system |
| **Local Persistence** | Room Database `2.6.1` (KSP) | 8 Indexed Entities, Foreign Keys, DAOs |
| **Asynchronous Engine** | Kotlin Coroutines `1.8.1` + Flow | StateFlow / SharedFlow reactive streams |
| **Network & Serialization** | Retrofit `2.11.0` + Moshi `1.15.2` | Moshi Kotlin Codegen (KSP) |
| **Image Loading** | Coil Compose `2.7.0` | Async image caching and crossfade |
| **AI Integration** | Google Gemini API via Firebase AI BOM | Client-side recommendation heuristics + AI assistant |
| **Testing Harness** | Robolectric `4.14.1` + JUnit 4 + Test BOM | JVM local execution without emulator requirement |
| **Network Security** | `android:usesCleartextTraffic="false"` | Strict HTTPS only |

---

## 3. Environment Architecture: DEMO vs PRODUCTION

Controlled centrally via `com.example.config.AppConfig`:

```text
                     ┌──────────────────────────────┐
                     │          AppConfig           │
                     │  (Centralized StateFlow)     │
                     └──────────────┬───────────────┘
                                    │
          ┌─────────────────────────┴─────────────────────────┐
          ▼                                                   ▼
     [DEMO MODE]                                      [PRODUCTION MODE]
 - Seeded Catalog Products                         - Disables simulated merchant offers
 - Sample Multi-Merchant Offers                    - Only queries live configured feeds
 - Demo Personalization Baseline                   - Returns honest unconfigured message
 - Demo Voucher Marketplace                        - Zero fabricated prices/inventory
 - Explicit "Demo Escrow"                          - Strict merchant credentials check
 - Prominent UI Sandbox Banner                     - Production database isolation
```

### Truth in Advertising Guarantees:
1. **No Fake Production Integrations:** If merchant API keys or partner affiliate tokens are not configured in environment secrets, Shop Mind AI returns:  
   `"Live merchant data is not currently configured."`
2. **Normalized Merchant Model:** All offers conform to `NormalizedMerchantOffer`. Unavailable data points (e.g. delivery date, seller rating) remain `null` rather than being invented.
3. **Sandbox Escrow:** All peer-to-peer voucher escrow operations are explicitly marked with `Demo Escrow (Sandbox Mode)`.

---

## 4. Architectural Package Layout

```text
com.example/
├── MainActivity.kt               # Single Activity host with Edge-to-Edge & Navigation
├── config/
│   └── AppConfig.kt              # Baseline, environment StateFlow, ProviderStatus enum
├── data/
│   ├── database/
│   │   └── AppDatabase.kt        # Room database definition with database version 1
│   ├── dao/
│   │   ├── CartDao.kt            # Cart items with user isolation
│   │   ├── MerchantDao.kt        # Merchants, offers, and price comparisons
│   │   ├── OrderDao.kt           # Order history and transactions
│   │   ├── PriceHistoryDao.kt    # Historical price snapshots and price-drop alerts
│   │   ├── ProductDao.kt         # Product catalog, categories, search queries
│   │   ├── PromoDao.kt           # Platform coupons and merchant deals
│   │   ├── UserDao.kt            # User accounts, password hashes, credentials
│   │   ├── VoucherDao.kt         # P2P vouchers with atomic status updates
│   │   └── WishlistDao.kt        # User wishlists with user isolation
│   ├── model/
│   │   ├── NormalizedMerchantOffer.kt # Standardized merchant offer contract
│   │   └── RoomEntities.kt       # 8 Entity classes with foreign keys & indices
│   ├── provider/
│   │   ├── MerchantProvider.kt   # Merchant provider contract & factory
│   │   └── MerchantProviderRegistry.kt # Registry for all 8 Indian & global providers
│   └── repository/
│       └── SmartCartRepository.kt# Single source of truth, reactive flows, mutexes
├── ai/
│   └── RecommendationEngine.kt   # Content-based, category heuristic & collaborative filtering
├── ui/
│   ├── components/               # Reusable M3 cards, headers, buttons, banners
│   ├── screens/                  # 14 distinct feature screens
│   ├── navigation/               # Type-safe navigation routes & bottom bar
│   └── theme/                    # ColorScheme, Typography, Shapes
├── util/
│   ├── MerchantUrlSecurityValidator.kt # Strict HTTPS and domain whitelist validator
│   └── SecurityUtils.kt          # SHA-256 password hashing & verification
└── viewmodel/
    └── SmartCartViewModel.kt     # MVVM ViewModel holding UI states and user flows
```

---

## 5. Major Module Breakdown

### Module 1: User Management, Authentication & RBAC
- Sequential user identifier generation: `SC-USER-000001`, `SC-USER-000002`...
- Cryptographic password hashing using SHA-256 with salting (`SecurityUtils.hashPassword`). Plain text passwords are never stored.
- User data isolation across cart, wishlist, orders, and price-drop alerts.
- Role-based authorization: **Customer** (shopping, vouchers, cart, orders) vs **Admin** (catalog management, provider status, transaction oversight).

### Module 2: Multi-Category Product Discovery & Search
- 10+ categories: Electronics, Smartphones, Audio, Laptops, Wearables, Fashion, Home, Sports, Accessories.
- Fast local indexing via Room SQLite with search by keyword, category, brand, and price bracket.

### Module 3: Multi-Merchant Engine (8 Providers)
Supported merchants:
1. **Amazon India** (`amazon.in`)
2. **Flipkart** (`flipkart.com`)
3. **Croma** (`croma.com`)
4. **Reliance Digital** (`reliancedigital.in`)
5. **Apple Store** (`apple.com`)
6. **Myntra** (`myntra.com`)
7. **Tata CliQ** (`tatacliq.com`)
8. **Ajio** (`ajio.com`)
- Strict HTTPS and registered merchant domain enforcement (`MerchantUrlSecurityValidator`).
- Safely rejects spoofed URLs, javascript schemes, or unverified redirect parameters.

### Module 4: Intelligent AI Recommendation Engine
- **Content-Based Filtering:** Matches user preferred categories and brands.
- **Collaborative Heuristics:** Recommends trending items and complementary accessories based on user cart contents.
- **Gemini Shopping Assistant:** Natural language query processor recommending specific catalog products for conversational shopper intent.

### Module 5: P2P Voucher Marketplace & Demo Escrow
- Users can list surplus gift cards and brand vouchers for sale.
- Double-spending protection using Kotlin Coroutine Mutex lock (`voucherPurchaseMutex`).
- Atomic status updates: `AVAILABLE` -> `IN_ESCROW` -> `SOLD`.
- Explicitly labeled **Demo Escrow (Sandbox Mode)**.
- Secure code masking: Voucher codes are masked (`••••••••`) until release.

### Module 6: Dynamic Pricing & Deal Optimization
- Price drop tracking with historical price point curves.
- Automatic application of eligible platform promo codes and merchant coupons.
- Net savings calculations displayed before checkout.

---

## 6. Known Limitations

1. **Merchant API Access:** Production merchant price feeds depend on commercial API contracts (Amazon PA-API, Flipkart Affiliate Partner). In the absence of live production credentials, the system displays honest unconfigured notices rather than simulating live data.
2. **Demo Escrow:** The escrow system is built for academic demonstration and simulation; it does not integrate with banking APIs (e.g. Razorpay/Stripe escrow) as live financial transactions require commercial licensing.
3. **Gemini Live Assistance:** When offline or without an API key, the shopping assistant relies on local semantic keyword heuristics.

---

## 7. Build Commands & Verification Status

```bash
# Automated Unit & Robolectric Tests (All passing)
gradle :app:testDebugUnitTest

# Platform Code Compilation
compile_applet

# Assembly of Release APK
gradle :app:assembleRelease
```

**Final Test Status:** `PASSING (100% test pass rate)`  
**Baseline Verification:** `SHOPMIND-RELEASE-CANDIDATE-04 VERIFIED`
