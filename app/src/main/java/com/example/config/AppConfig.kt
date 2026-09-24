package com.example.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Release Environment Classification
 */
enum class AppEnvironment {
    DEMO,
    PRODUCTION
}

/**
 * Comprehensive Merchant Provider Status Model
 * Required for Phase 4 of SmartCart AI Release-03
 */
enum class ProviderStatus(val displayName: String, val badgeColorHex: Long) {
    LIVE("Live API Connected", 0xFF10B981),
    CONFIGURED("Credentials Configured", 0xFF059669),
    AVAILABLE("Deep Link Available", 0xFF3B82F6),
    DEMO("Demo / Sandbox Mode", 0xFF6366F1),
    NOT_CONFIGURED("Not Configured (Demo)", 0xFFF59E0B),
    DISABLED("Provider Disabled", 0xFF6B7280),
    ERROR("Connection Error", 0xFFEF4444)
}

/**
 * Centralized Application, Environment, and Secret Configuration System
 * Governs DEMO mode vs PRODUCTION mode behaviors.
 */
object AppConfig {
    const val RELEASE_BASELINE = "SMARTCART-RELEASE-CANDIDATE-04"
    const val APPLICATION_ID = "com.aistudio.smartcart.ai"
    const val VERSION_NAME = "1.0"
    const val VERSION_CODE = 1
    const val MIN_SDK = 24
    const val TARGET_SDK = 36
    const val COMPILE_SDK = 36
    const val DATABASE_VERSION = 1

    private val _environment = MutableStateFlow(AppEnvironment.DEMO)
    val environmentState: StateFlow<AppEnvironment> = _environment.asStateFlow()

    var currentEnvironment: AppEnvironment
        get() = _environment.value
        set(value) {
            _environment.value = value
        }

    val isDemoMode: Boolean
        get() = currentEnvironment == AppEnvironment.DEMO

    val isProductionMode: Boolean
        get() = currentEnvironment == AppEnvironment.PRODUCTION

    /**
     * Whether simulated or sample data can be returned in UI
     */
    val allowDemoData: Boolean
        get() = isDemoMode

    /**
     * In production mode, unconfigured providers must return honest empty/unconfigured states
     */
    val requireLiveProviders: Boolean
        get() = isProductionMode

    /**
     * Display label for UI headers and disclaimer cards
     */
    val environmentBannerText: String
        get() = if (isDemoMode) {
            "DEMO MODE — Sample catalog & simulated offers for evaluation"
        } else {
            "PRODUCTION MODE — Live feeds and authorized provider connections"
        }

    fun setEnvironment(env: AppEnvironment) {
        _environment.value = env
    }

    fun toggleEnvironment() {
        _environment.value = if (isDemoMode) AppEnvironment.PRODUCTION else AppEnvironment.DEMO
    }

    fun getReleaseBaselineInfo(): Map<String, String> {
        return mapOf(
            "Release Checkpoint" to RELEASE_BASELINE,
            "Environment" to currentEnvironment.name,
            "Application ID" to APPLICATION_ID,
            "Version" to "$VERSION_NAME ($VERSION_CODE)",
            "Min SDK" to MIN_SDK.toString(),
            "Target SDK" to TARGET_SDK.toString(),
            "Compile SDK" to COMPILE_SDK.toString(),
            "Database Version" to DATABASE_VERSION.toString()
        )
    }
}
