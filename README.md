# SmartCart AI

> **Intelligent Multi-Merchant E-Commerce Platform with AI Recommendations, Dynamic Price Intelligence, and Peer-to-Peer Voucher Escrow**

[![Android Baseline](https://img.shields.io/badge/Baseline-SMARTCART--RELEASE--CANDIDATE--04-blue.svg)](FINAL_RELEASE_FREEZE.md)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20%28API%2024--36%29-green.svg)](app/build.gradle.kts)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20%28M3%29-purple.svg)](app/build.gradle.kts)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20UDF%20%2B%20Room-orange.svg)](PROJECT_REPORT.md)
[![Tests](https://img.shields.io/badge/Tests-Robolectric%20JVM%20%28Passing%29-brightgreen.svg)](app/src/test/java/com/example)

---

## 1. Project Overview

**SmartCart AI** is an enterprise-grade native Android e-commerce application developed as a B.Tech Computer Science Major Project. In contrast to conventional marketplace applications that lock users into single-vendor silos with margin-biased recommendations, SmartCart AI operates as an unbiased, consumer-first shopping companion.

SmartCart AI unifies:
1. **Multi-Merchant Price Comparison** across 8 leading Indian and global retail destinations.
2. **Hybrid AI Personalization** combining local on-device heuristics with conversational shopping assistance powered by Google Gemini.
3. **Dynamic Price Analytics** with historical tracking and automated price-drop alerts.
4. **Peer-to-Peer (P2P) Voucher Exchange** protected by concurrency mutex locks and a sandboxed escrow workflow.
5. **Deterministic Environment Separation** strictly distinguishing **DEMO** mode (sandboxed evaluation) from **PRODUCTION** mode (honest unconfigured provider states with zero fabricated data).

---

## 2. Major Features & System Capabilities

### 🛍️ Intelligent Product Discovery & Multi-Category Catalog
- Fast browsing across 10+ retail categories (Smartphones, Audio, Laptops, Wearables, Fashion, Home, Sports, Accessories).
- SQLite-indexed full-text keyword search and category attribute filtering.
- 60 FPS declarative UI rendering built with Jetpack Compose Material Design 3.

### 🤖 Hybrid AI Recommendation System & Shopping Assistant
- **Content-Based Affinity Scoring:** Matches user-saved category and brand preferences with catalog product tags without sending private data off-device.
- **Collaborative Association Heuristics:** Evaluates complementary accessory associations based on cart contents.
- **Gemini AI Conversational Assistant:** Natural language query processor recommending specific catalog products for complex shopper queries.

### 🌐 Multi-Merchant Comparison Engine (8 Providers)
Supported merchants:
- **Amazon India** (`amazon.in`)
- **Flipkart** (`flipkart.com`)
- **Croma** (`croma.com`)
- **Reliance Digital** (`reliancedigital.in`)
- **Apple Store India** (`apple.com`)
- **Myntra** (`myntra.com`)
- **Tata CliQ** (`tatacliq.com`)
- **Ajio** (`ajio.com`)

> **Important Disclosure:** The merchant provider architecture models live API endpoints and affiliate feeds. In this release, unconfigured merchant providers honestly display `NOT_CONFIGURED: Live merchant data is not currently configured.` Zero prices, discounts, or delivery dates are fabricated in production mode.

### 🛡️ Strict URL Security & Allowlisting
- `MerchantUrlSecurityValidator` strictly enforces `https` schemes and verifies that redirect URLs match audited merchant domains before launching external intents, preventing open-redirect and phishing attacks.

### 📉 Price History Tracking & Dynamic Alerts
- Records product price points over time to expose artificial discounts.
- User-configurable threshold alerts triggered when product prices drop below a specified limit.

### 🎟️ P2P Voucher Marketplace with Concurrency Protection & Demo Escrow
- Allows consumers to liquidate unspent brand gift vouchers.
- **Concurrency Lock (`Mutex`):** `SmartCartRepository` uses `voucherPurchaseMutex.withLock` to guarantee atomic state transitions and eliminate race conditions / double-spending.
- **Demo Escrow (Sandbox Mode):** 
  > **Note on Escrow:** This application implements **"Demo Escrow (Sandbox Mode)"** for academic and demonstration purposes. It demonstrates state-machine integrity (`AVAILABLE` → `IN_ESCROW` → `SOLD`) and code masking (`FLI-****-5432`) without executing live financial transactions through banking clearinghouses.

### 🔐 Authentication, RBAC & Data Isolation
- User accounts receive a sequential, enterprise-formatted identifier: `SC-USER-000001`, `SC-USER-000002`...
- Cryptographic password hashing using SHA-256 (`SecurityUtils.hashPassword`). Plain text passwords are never stored.
- Multi-user isolation across Cart, Wishlist, Orders, and Price Alerts on shared devices.
- Role-Based Access Control (RBAC): Customer shopping interface vs Admin management console.

---

## 3. Demo Mode vs Production Mode Architecture

Environment configuration is governed centrally by `com.example.config.AppConfig`:

| Dimension | DEMO Mode | PRODUCTION Mode |
|---|---|---|
| **Catalog Feed** | Seeded sample catalog | Seeded / live catalog |
| **Merchant Offers** | Evaluative mock offers | Disables simulated offers |
| **Unconfigured Providers** | Returns labeled sample data | Displays `"Live merchant data is not currently configured"` |
| **Price & Stock Data** | Sample data for demonstration | Zero fabricated prices or inventory |
| **P2P Escrow** | Labeled "Demo Escrow (Sandbox Mode)" | Production escrow integration |
| **UI Banner** | Visible top sandbox disclaimer | Production environment status |

---

## 4. Technology Stack & Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                      PRESENTATION TIER                      │
│            Jetpack Compose (M3) + Navigation Compose        │
└──────────────────────────────┬──────────────────────────────┘
                               │ Unidirectional Data Flow (StateFlow)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       VIEWMODEL TIER                        │
│            SmartCartViewModel (Coroutines / Flows)          │
└──────────────────────────────┬──────────────────────────────┘
                               │ Repository Pattern
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                      REPOSITORY TIER                        │
│         SmartCartRepository (Mutex Concurrency Locks)       │
├──────────────────────────────┬──────────────────────────────┤
│         LOCAL PERSISTENCE    │     EXTERNAL INTEGRATION     │
│  ┌────────────────────────┐  │  ┌────────────────────────┐  │
│  │   Room SQLite 2.6.1    │  │  │Merchant Provider Layer │  │
│  │   - 8 Indexed Entities │  │  │(Amazon, Flipkart, etc.)│  │
│  │   - 9 DAOs             │  │  ├────────────────────────┤  │
│  └────────────────────────┘  │  │  Google Gemini AI SDK  │  │
│                              │  └────────────────────────┘  │
└──────────────────────────────┴──────────────────────────────┘
```

- **Language:** Kotlin 2.0.21
- **UI Framework:** Jetpack Compose BOM 2024.10.00 (Material Design 3)
- **Persistence:** Room 2.6.1 with KSP (Kotlin Symbol Processing)
- **Concurrency:** Kotlin Coroutines 1.8.1 & Reactive Flow
- **Image Loading:** Coil Compose 2.7.0
- **Testing:** JUnit 4 + Robolectric 4.14.1 (Headless local JVM testing)

---

## 5. Repository Structure

```text
smartcart-ai/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ai/               # Hybrid recommendation engine & Gemini
│   │   │   │   ├── config/           # AppConfig, AppEnvironment, ProviderStatus
│   │   │   │   ├── data/             # Database, DAOs, Entities, Providers, Repository
│   │   │   │   ├── ui/               # Compose screens, components, theme, navigation
│   │   │   │   ├── util/             # SecurityUtils, MerchantUrlSecurityValidator
│   │   │   │   └── viewmodel/        # SmartCartViewModel
│   │   │   └── res/                  # Drawables, mipmaps, strings, XML resources
│   │   └── test/java/com/example/    # Robolectric & JUnit 4 automated tests
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml            # Version catalog
│   └── wrapper/                      # Gradle wrapper configuration
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── FINAL_RELEASE_FREEZE.md           # Formal release freeze baseline
├── PROJECT_REPORT.md                 # Academic B.Tech Major Project Report
├── VIVA_PREPARATION.md               # 30+ Viva questions & system defense guide
├── DEMO_SCRIPT.md                    # 10-Act evaluation presentation walkthrough
└── README.md
```

---

## 6. Build & Installation Instructions

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line Android SDK
- JDK 17 (or JDK 11+)
- Android SDK Platform 36 (Android 15)

### Running Automated Tests
```bash
# Execute all JUnit and Robolectric JVM test suites
./gradlew testDebugUnitTest
```

### Compiling Debug Build
```bash
./gradlew assembleDebug
```

### Building Release Artifacts
```bash
# Release APK
STORE_PASSWORD=android KEY_PASSWORD=android ./gradlew assembleRelease

# Release Android App Bundle (AAB)
STORE_PASSWORD=android KEY_PASSWORD=android ./gradlew bundleRelease
```

---

## 7. Limitations & Future Scope

### Current Limitations
1. **Merchant Feeds:** Production data aggregation requires active merchant affiliate API partnership keys (e.g. Amazon PA-API, Flipkart Affiliate). Without credentials, providers safely show `NOT_CONFIGURED`.
2. **Demo Escrow:** Escrow is implemented as a demonstration state machine and is not connected to banking escrow accounts.
3. **Gemini Live Assistance:** Conversational AI requires a Google Gemini API key configured in `.env` or the Secrets panel. Offline mode uses keyword semantics.

### Future Scope
- **On-Device SLM (Gemini Nano):** Transitioning conversational reasoning to run completely on-device via Android AICore.
- **UPI Escrow Integration:** Connecting peer-to-peer voucher liquidation with RBI-regulated automated escrow accounts.
- **Barcode & QR Scanning:** Direct camera OCR verification for in-store voucher redemption.

---

## 8. License & Academic Attribution

This project is submitted in partial fulfillment of the requirements for the award of the degree of **Bachelor of Technology in Computer Science and Engineering**.

Developed by **Ankit Kumar** ([ankitkumaranalytics](https://github.com/ankitkumaranalytics)).
