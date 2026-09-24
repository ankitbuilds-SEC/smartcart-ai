package com.example.util

import java.net.URI
import java.util.Locale

/**
 * Merchant URL Security Validator
 * Validates that external redirect URLs strictly enforce HTTPS and are restricted to approved merchant domains.
 */
object MerchantUrlSecurityValidator {

    val ALLOWED_MERCHANT_DOMAINS = setOf(
        "amazon.in",
        "amazon.com",
        "flipkart.com",
        "croma.com",
        "myntra.com",
        "tatacliq.com",
        "ajio.com",
        "meesho.com",
        "reliancedigital.in",
        "reliancedigital.com",
        "apple.com"
    )

    /**
     * Validates whether a given URL is permitted for external merchant purchase redirects.
     * Criteria:
     * 1. Must use HTTPS scheme.
     * 2. Host must match or be a subdomain of an approved merchant domain.
     * 3. No arbitrary schemes (javascript:, file:, data:, etc.) allowed.
     */
    fun isAllowedRedirect(url: String): Boolean {
        val trimmed = url.trim()
        if (!trimmed.startsWith("https://", ignoreCase = true)) {
            return false
        }
        return try {
            val uri = URI(trimmed)
            val scheme = uri.scheme?.lowercase(Locale.ROOT)
            if (scheme != "https") return false

            val host = uri.host?.lowercase(Locale.ROOT) ?: return false

            ALLOWED_MERCHANT_DOMAINS.any { domain ->
                host == domain || host.endsWith(".$domain")
            }
        } catch (e: Exception) {
            false
        }
    }

    fun getDomain(url: String): String {
        return try {
            val uri = URI(url.trim())
            uri.host?.lowercase(Locale.ROOT) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
