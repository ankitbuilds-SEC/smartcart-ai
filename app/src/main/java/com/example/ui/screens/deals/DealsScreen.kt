package com.example.ui.screens.deals

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MerchantCouponEntity
import com.example.data.model.MerchantOfferEntity
import com.example.data.model.ProductEntity
import com.example.data.model.PromoCampaignEntity
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.util.MerchantUrlSecurityValidator
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val topDeals by viewModel.topDeals.collectAsState()
    val highDiscountProducts by viewModel.highestDiscountProducts.collectAsState()
    val merchantCoupons by viewModel.allMerchantCoupons.collectAsState()
    val promoCampaigns by viewModel.activePromoCampaigns.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var showCopiedToast by remember { mutableStateOf<String?>(null) }
    var pendingDealRedirect by remember { mutableStateOf<Pair<MerchantOfferEntity, ProductEntity>?>(null) }
    var pendingCouponRedirect by remember { mutableStateOf<MerchantCouponEntity?>(null) }

    val filteredProducts = remember(highDiscountProducts, selectedCategoryFilter) {
        if (selectedCategoryFilter == null || selectedCategoryFilter == "All") {
            highDiscountProducts
        } else {
            highDiscountProducts.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Rose500.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Rose500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Deals & Price Drops",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Verified price comparison across top merchants",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("deals_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Data Attribution Notice Banner
            item {
                Surface(
                    color = Blue600.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Prices verified across authorized merchants (Amazon, Flipkart, Croma, Myntra). Demo catalog feeds labeled accordingly.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 2. Category Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All Categories") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(getCategoryEmoji(cat))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(cat)
                                }
                            }
                        )
                    }
                }
            }

            // 3. Featured Promo Codes (SmartCart Store Promos)
            if (promoCampaigns.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Shop Mind Store Promos",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Apply at Checkout",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(promoCampaigns) { promo ->
                                PromoCard(
                                    campaign = promo,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(promo.promoCode))
                                        viewModel.toastMessage.value = "Copied code '${promo.promoCode}'"
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Today's Top Discounts (>15% OFF)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Rose500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Highest Discounts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${filteredProducts.size} items",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            ProductCard(
                                product = prod,
                                isWishlisted = wishlistIds.contains(prod.id),
                                aiExplanation = "${prod.discountPercent}% OFF • Lowest in 30 days",
                                onProductClick = { viewModel.selectProduct(prod) },
                                onWishlistClick = { viewModel.toggleWishlist(prod.id) },
                                onAddToCartClick = { viewModel.addToCart(prod.id) },
                                modifier = Modifier.width(170.dp)
                            )
                        }
                    }
                }
            }

            // 5. Merchant Comparison Deals (External Offers)
            if (topDeals.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Top Merchant Price Drops",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Real-time competitor offers across Amazon, Flipkart, Croma & Myntra",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                    }
                }

                items(topDeals.take(8)) { offer ->
                    val prod = allProducts.firstOrNull { it.id == offer.productId }
                    if (prod != null) {
                        MerchantDealRow(
                            offer = offer,
                            product = prod,
                            onCompareClick = { viewModel.selectProduct(prod) },
                            onBuyNowClick = {
                                pendingDealRedirect = Pair(offer, prod)
                            }
                        )
                    }
                }
            }

            // 6. Verified Merchant Coupons (Copy to external website)
            if (merchantCoupons.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Loyalty, contentDescription = null, tint = Amber500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Merchant Coupon Codes",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Copy codes to redeem directly on external merchant checkout pages",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            merchantCoupons.forEach { coupon ->
                                MerchantCouponCard(
                                    coupon = coupon,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(coupon.code))
                                        viewModel.toastMessage.value = "Copied ${coupon.merchantName} code '${coupon.code}'"
                                    },
                                    onVisit = {
                                        pendingCouponRedirect = coupon
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (pendingDealRedirect != null) {
            val (offer, prod) = pendingDealRedirect!!
            val isAllowed = MerchantUrlSecurityValidator.isAllowedRedirect(offer.externalUrl)
            AlertDialog(
                onDismissRequest = { pendingDealRedirect = null },
                icon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Indigo600) },
                title = { Text("External Merchant Redirect") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "You are being redirected to the merchant website to complete your purchase.",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Merchant: ${offer.merchantName}\nProduct: ${prod.name}\nPrice: ₹${String.format("%,.0f", offer.currentPrice)}\nDestination: ${offer.externalUrl}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isAllowed) {
                            Text(
                                text = "⚠️ Security Warning: Redirection blocked. Destination is not on the approved merchant allowlist or lacks HTTPS.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "🔒 Shop Mind AI does not process external merchant payments, claim delivery statuses, or fabricate external order IDs. You will complete your transaction directly on ${offer.merchantName}.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isAllowed) {
                                viewModel.recordExternalClick(offer, prod, "deals_page")
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.externalUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                                viewModel.toastMessage.value = "You are being redirected to the merchant website to complete your purchase."
                                pendingDealRedirect = null
                            }
                        },
                        enabled = isAllowed
                    ) {
                        Text(if (isAllowed) "Continue to ${offer.merchantName}" else "Redirect Blocked")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDealRedirect = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (pendingCouponRedirect != null) {
            val coupon = pendingCouponRedirect!!
            val isAllowed = MerchantUrlSecurityValidator.isAllowedRedirect(coupon.externalUrl)
            AlertDialog(
                onDismissRequest = { pendingCouponRedirect = null },
                icon = { Icon(Icons.Default.Loyalty, contentDescription = null, tint = Amber500) },
                title = { Text("Visit Merchant Store") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "You are being redirected to the merchant website to complete your purchase.",
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Merchant: ${coupon.merchantName}\nCoupon Code: ${coupon.code} (${coupon.discountDescription})\nSource: ${coupon.source} • Expiry: ${coupon.expiryDate}\nDestination: ${coupon.externalUrl}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isAllowed) {
                            Text(
                                text = "⚠️ Security Warning: Redirection blocked. Destination is not on the approved merchant allowlist or lacks HTTPS.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isAllowed) {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(coupon.externalUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                                viewModel.toastMessage.value = "You are being redirected to the merchant website to complete your purchase."
                                pendingCouponRedirect = null
                            }
                        },
                        enabled = isAllowed
                    ) {
                        Text(if (isAllowed) "Continue to ${coupon.merchantName}" else "Redirect Blocked")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingCouponRedirect = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PromoCard(
    campaign: PromoCampaignEntity,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.08f)),
        modifier = modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Emerald500,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = campaign.promoCode,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Emerald500,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = campaign.campaignName,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (campaign.discountType == "PERCENTAGE") "${campaign.discountValue.toInt()}% off up to ₹${campaign.maxDiscount.toInt()}" else "Flat ₹${campaign.discountValue.toInt()} off",
                fontSize = 12.sp,
                color = Emerald500,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Min order: ₹${campaign.minOrderValue.toInt()}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MerchantDealRow(
    offer: MerchantOfferEntity,
    product: ProductEntity,
    onCompareClick: () -> Unit,
    onBuyNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Emoji Avatar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Slate100,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = getCategoryEmoji(product.category), fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product & Merchant details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (offer.merchantName) {
                            "Amazon" -> Amber500.copy(alpha = 0.15f)
                            "Flipkart" -> Blue600.copy(alpha = 0.15f)
                            "Myntra" -> Rose500.copy(alpha = 0.15f)
                            else -> Purple500.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = offer.merchantName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (offer.merchantName) {
                                "Amazon" -> Color(0xFFD97706)
                                "Flipkart" -> Blue600
                                "Myntra" -> Rose500
                                else -> Purple500
                            },
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = Emerald500.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${offer.discountPercent}% OFF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "₹${String.format("%,.0f", offer.currentPrice)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${String.format("%,.0f", offer.originalPrice)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                if (offer.couponCode.isNotBlank()) {
                    Text(
                        text = "🏷️ Code: ${offer.couponCode} (${offer.couponDiscount})",
                        fontSize = 10.sp,
                        color = Emerald500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onBuyNowClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Buy Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onCompareClick,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Compare", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun MerchantCouponCard(
    coupon: MerchantCouponEntity,
    onCopy: () -> Unit,
    onVisit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = coupon.merchantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• Expires ${coupon.expiryDate}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = coupon.discountDescription,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Text(
                    text = coupon.conditions,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextButton(
                    onClick = onVisit,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Go to site ↗", fontSize = 10.sp)
                }
            }
        }
    }
}
