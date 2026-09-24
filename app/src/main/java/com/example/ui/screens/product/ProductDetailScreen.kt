package com.example.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import com.example.data.model.MerchantOfferEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.AiBadge
import com.example.ui.components.ProductCard
import com.example.ui.components.SentimentBar
import com.example.ui.components.getCategoryEmoji
import com.example.ui.components.getCategoryGradientEnd
import com.example.ui.components.getCategoryGradientStart
import com.example.ui.theme.*
import com.example.util.MerchantUrlSecurityValidator
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: ProductEntity,
    viewModel: SmartCartViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val similarProducts by viewModel.similarProducts.collectAsState()
    val frequentlyBought by viewModel.frequentlyBoughtTogether.collectAsState()
    val merchantOffers by viewModel.selectedProductOffers.collectAsState()
    val priceHistory by viewModel.selectedProductPriceHistory.collectAsState()

    // Price Alert Dialog state
    var showPriceAlertDialog by remember { mutableStateOf(false) }
    var targetPriceInput by remember { mutableStateOf("") }
    var pendingRedirectOffer by remember { mutableStateOf<MerchantOfferEntity?>(null) }

    // Load reviews for this product
    val reviews by viewModel.repository.getReviewsForProduct(product.id).collectAsState(initial = emptyList())

    // Review submission state
    var showReviewDialog by remember { mutableStateOf(false) }
    var newRating by remember { mutableIntStateOf(5) }
    var newReviewText by remember { mutableStateOf("") }

    val isWishlisted = wishlistIds.contains(product.id)

    // Calculate aggregated sentiment
    val positiveCount = reviews.count { it.sentiment == "Positive" }
    val negativeCount = reviews.count { it.sentiment == "Negative" }
    val neutralCount = reviews.count { it.sentiment == "Neutral" }
    val totalCount = reviews.size.coerceAtLeast(1)

    val posPercent = ((positiveCount.toDouble() / totalCount) * 100).toInt()
    val negPercent = ((negativeCount.toDouble() / totalCount) * 100).toInt()
    val neutPercent = (100 - posPercent - negPercent).coerceAtLeast(0)

    // Price History metrics calculated from actual stored observations
    val hasPriceHistory = priceHistory.isNotEmpty()
    val lowestRecordedPrice = remember(priceHistory, product) {
        val minHist = priceHistory.minOfOrNull { it.price } ?: product.finalPrice
        minOf(minHist, product.finalPrice)
    }
    val highestRecordedPrice = remember(priceHistory, product) {
        val maxHist = priceHistory.maxOfOrNull { it.price } ?: product.price
        maxOf(maxHist, product.price)
    }
    val averageRecordedPrice = remember(priceHistory, product) {
        if (priceHistory.isEmpty()) product.finalPrice else priceHistory.map { it.price }.average()
    }
    val priceChangePercent = remember(averageRecordedPrice, product) {
        if (averageRecordedPrice <= 0.0) 0 else (((product.finalPrice - averageRecordedPrice) / averageRecordedPrice) * 100).toInt()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.brand, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("detail_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) Rose500 else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Sticky Action Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Price",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format("%,.0f", product.finalPrice)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.addToCart(product.id) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("detail_add_cart_btn")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.addToCart(product.id)
                            onDismiss()
                            viewModel.selectTab(NavigationTab.CART)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        modifier = Modifier
                            .weight(1.1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_btn")
                    ) {
                        Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        modifier = modifier.testTag("product_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Large Hero Graphic
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    getCategoryGradientStart(product.category),
                                    getCategoryGradientEnd(product.category)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = getCategoryEmoji(product.category), fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.category,
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Stock Pill
                    Surface(
                        color = if (product.stock > 0) Emerald500 else Rose500,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = if (product.stock > 0) "In Stock: ${product.stock} units" else "Out of Stock",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 2. Title, Ratings & Price Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.brand.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Amber500, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${product.rating}★",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${product.reviewCount} reviews)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = product.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Price Section
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${String.format("%,.0f", product.finalPrice)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.discountPercent > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "₹${String.format("%,.0f", product.price)}",
                                fontSize = 15.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Rose500,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${product.discountPercent}% OFF",
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2b. Live Merchant Offers & Price Comparison
            if (merchantOffers.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CompareArrows, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Compare Other Retailers",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Surface(
                                    color = Emerald500.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Best Price Match",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald500,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            merchantOffers.forEach { offer ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = offer.merchantName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                if (offer.discountPercent > 0) {
                                                    Surface(
                                                        color = Emerald500.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "${offer.discountPercent}% OFF",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Emerald500,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "₹${String.format("%,.0f", offer.currentPrice)} • ${offer.deliveryInfo}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (offer.couponCode.isNotBlank()) {
                                                Text(
                                                    text = "🏷️ Coupon: ${offer.couponCode}",
                                                    fontSize = 10.sp,
                                                    color = Blue600,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                pendingRedirectOffer = offer
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("View Deal", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Prices updated hourly. Attribution: Live merchant feeds and demo catalog integrations.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }

            // 2c. 30-Day Price Tracker & Alert Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue600.copy(alpha = 0.07f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Blue600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "30-Day Price History",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Blue600
                                )
                            }
                            TextButton(
                                onClick = { showPriceAlertDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp), tint = Blue600)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue600)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!hasPriceHistory) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Insufficient historical data",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "No stored observations yet for this product. Observations are tracked hourly.",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Lowest", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${String.format("%,.0f", lowestRecordedPrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Emerald500)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Average", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${String.format("%,.0f", averageRecordedPrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Indigo600)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Current", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹${String.format("%,.0f", product.finalPrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Trend", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = if (priceChangePercent <= 0) "$priceChangePercent%" else "+$priceChangePercent%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (priceChangePercent <= 0) Emerald500 else Rose500
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. AI Explainability Card ("Why SmartCart AI Recommends This")
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Purple500.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Purple500, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Why SmartCart AI Recommends This",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Purple500
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Match reasons
                        AiReasonRow(label = "Category Affinity", value = "High match for ${product.category} (35% weight)")
                        AiReasonRow(label = "Brand Popularity", value = "${product.brand} is trending with 4.8★ average sentiment")
                        AiReasonRow(label = "Price Alignment", value = "Fits comfortably in user preferred budget tier")
                        if (product.tags.isNotEmpty()) {
                            AiReasonRow(label = "Feature Vector Tags", value = product.tags.split(",").take(4).joinToString(", "))
                        }
                    }
                }
            }

            // 4. Product Description & Specifications
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product Overview",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = product.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        if (product.specifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Specifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            product.specifications.split("|").forEach { spec ->
                                val parts = spec.split(":")
                                if (parts.size == 2) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = parts[0].trim(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = parts[1].trim(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Customer Reviews & Sentiment Analysis
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer Reviews",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            TextButton(onClick = { showReviewDialog = true }) {
                                Text("Write Review", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sentiment Breakdown
                        SentimentBar(
                            positivePercent = posPercent,
                            neutralPercent = neutPercent,
                            negativePercent = negPercent,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reviews List
                        reviews.take(5).forEach { rev ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = rev.userName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Surface(
                                            color = when (rev.sentiment) {
                                                "Positive" -> Emerald500
                                                "Negative" -> Rose500
                                                else -> Amber500
                                            },
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = rev.sentiment,
                                                color = PureWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "★".repeat(rev.rating),
                                        color = Amber500,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rev.reviewText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Frequently Bought Together (Collaborative Filtering Carousel)
            if (frequentlyBought.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Frequently Bought Together 📦",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            text = "Collaborative filtering from customer order co-occurrences",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(frequentlyBought) { item ->
                                ProductCard(
                                    product = item.product,
                                    isWishlisted = wishlistIds.contains(item.product.id),
                                    aiExplanation = item.explanation,
                                    onProductClick = { viewModel.selectProduct(item.product) },
                                    onWishlistClick = { viewModel.toggleWishlist(item.product.id) },
                                    onAddToCartClick = { viewModel.addToCart(item.product.id) },
                                    modifier = Modifier.width(170.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 7. Similar Products (Content-Based Similarity Carousel)
            if (similarProducts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Similar Products You May Like",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            text = "Content-based cosine similarity on category, brand, and specifications",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarProducts) { item ->
                                ProductCard(
                                    product = item.product,
                                    isWishlisted = wishlistIds.contains(item.product.id),
                                    aiExplanation = item.explanation,
                                    onProductClick = { viewModel.selectProduct(item.product) },
                                    onWishlistClick = { viewModel.toggleWishlist(item.product.id) },
                                    onAddToCartClick = { viewModel.addToCart(item.product.id) },
                                    modifier = Modifier.width(170.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Write Review Dialog with Real-time Sentiment Analysis
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Write a Review", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select Rating:", fontSize = 13.sp)
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { newRating = star },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= newRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star Stars",
                                    tint = Amber500
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newReviewText,
                        onValueChange = { newReviewText = it },
                        label = { Text("Share your experience...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newReviewText.isNotBlank()) {
                            viewModel.addReview(product.id, newRating, newReviewText)
                            newReviewText = ""
                            showReviewDialog = false
                        }
                    }
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set Price Drop Alert Dialog
    if (showPriceAlertDialog) {
        AlertDialog(
            onDismissRequest = { showPriceAlertDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Blue600, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Price Drop Alert", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Current price is ₹${String.format("%,.0f", product.finalPrice)}. Enter your target price to receive in-app notifications when it drops below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = targetPriceInput,
                        onValueChange = { targetPriceInput = it },
                        label = { Text("Target Price (₹)") },
                        placeholder = { Text("e.g. ${(product.finalPrice * 0.9).toInt()}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetPriceInput.toDoubleOrNull()
                        if (target != null && target > 0) {
                            viewModel.createPriceAlert(product, target)
                            showPriceAlertDialog = false
                            targetPriceInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    Text("Set Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPriceAlertDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (pendingRedirectOffer != null) {
        val offer = pendingRedirectOffer!!
        val isAllowed = MerchantUrlSecurityValidator.isAllowedRedirect(offer.externalUrl)
        AlertDialog(
            onDismissRequest = { pendingRedirectOffer = null },
            icon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Blue600) },
            title = { Text("External Merchant Redirect") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "You are being redirected to the merchant website to complete your purchase.",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Merchant: ${offer.merchantName}\nPrice: ₹${String.format("%,.0f", offer.currentPrice)}\nDestination: ${offer.externalUrl}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isAllowed) {
                        Text(
                            text = "⚠️ Security Warning: Redirection blocked. Destination is not on the approved merchant allowlist or does not enforce HTTPS.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "🔒 SmartCart AI does not process external merchant payments, claim delivery statuses, or fabricate external order IDs. You will complete your transaction directly on ${offer.merchantName}.",
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
                            viewModel.recordExternalClick(offer, product, "product_detail")
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(offer.externalUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                            viewModel.toastMessage.value = "You are being redirected to the merchant website to complete your purchase."
                            pendingRedirectOffer = null
                        }
                    },
                    enabled = isAllowed
                ) {
                    Text(if (isAllowed) "Continue to ${offer.merchantName}" else "Redirect Blocked")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRedirectOffer = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AiReasonRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "• $label:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
