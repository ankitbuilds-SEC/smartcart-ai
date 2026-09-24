package com.example.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ProductEntity
import com.example.ui.components.AiBadge
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@Composable
fun HomeScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val recommendedForYou by viewModel.recommendedForYou.collectAsState()
    val trendingProducts by viewModel.trendingProducts.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val aiMetrics by viewModel.aiMetrics.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Bar / App Branding & AI Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Indigo600,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Logo",
                                tint = PureWhite,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SmartCart",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = " AI",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Model: ${aiMetrics.modelVersion.take(12)}",
                            fontSize = 11.sp,
                            color = Emerald600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Demo Switcher / User Chip
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable {
                        val nextRole = if (currentUser?.role == "admin") "customer" else "admin"
                        viewModel.switchDemoUser(nextRole)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (currentUser?.role == "admin") Amber500 else Emerald500,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentUser?.role == "admin") "Admin Mode" else "Customer Demo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Search Shortcut Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { viewModel.selectTab(NavigationTab.CATALOG) },
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search 100+ items, electronics, gaming...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Hero Banner (Interactive AI E-Commerce Showcase)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.hero_banner),
                    contentDescription = "SmartCart AI Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Slate950.copy(alpha = 0.88f),
                                    Slate900.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Banner text & Call to action
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                        .widthIn(max = 240.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    AiBadge(label = "AI Recommendation Engine")

                    Column {
                        Text(
                            text = "Next-Gen Intelligent E-Commerce",
                            color = PureWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Learns in real-time from your views, searches & purchases.",
                            color = Slate200,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.selectTab(NavigationTab.ASSISTANT) },
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Try AI Assistant", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3b. Marketplace Hub Row (Deals & Vouchers & AI)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Deals Hub Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(NavigationTab.DEALS) },
                    colors = CardDefaults.cardColors(containerColor = Rose500.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Rose500,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Top Deals", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Rose500)
                            Text("Competitor match", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Vouchers Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(NavigationTab.VOUCHERS) },
                    colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald500,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Vouchers", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Emerald500)
                            Text("P2P Escrow", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // 4. Quick Category Pills
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                PaddingHeader(
                    title = "Shop by Category",
                    action = "Explore All",
                    onActionClick = { viewModel.selectTab(NavigationTab.CATALOG) }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(categories) { cat ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier.clickable {
                                viewModel.selectedCategory.value = cat
                                viewModel.selectTab(NavigationTab.CATALOG)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getCategoryEmoji(cat),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. "Recommended For You" (Core AI Hybrid Recommendation Engine Section)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Recommended for You",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Purple500.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "HYBRID AI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Purple500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Refresh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.refreshRecommendations() }
                    )
                }

                Text(
                    text = "Personalized using collaborative filtering, content vectors & browsing behavior",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(recommendedForYou) { recItem ->
                        ProductCard(
                            product = recItem.product,
                            isWishlisted = wishlistIds.contains(recItem.product.id),
                            aiExplanation = recItem.explanation,
                            onProductClick = {
                                viewModel.recordExperimentClick(isVariantB = true)
                                viewModel.selectProduct(recItem.product)
                            },
                            onWishlistClick = { viewModel.toggleWishlist(recItem.product.id) },
                            onAddToCartClick = { viewModel.addToCart(recItem.product.id) },
                            modifier = Modifier.width(185.dp)
                        )
                    }
                }
            }
        }

        // 6. "Trending Products"
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                PaddingHeader(
                    title = "Trending Now 🔥",
                    action = "See More",
                    onActionClick = { viewModel.selectTab(NavigationTab.CATALOG) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(trendingProducts) { recItem ->
                        ProductCard(
                            product = recItem.product,
                            isWishlisted = wishlistIds.contains(recItem.product.id),
                            aiExplanation = recItem.explanation,
                            onProductClick = {
                                viewModel.recordExperimentClick(isVariantB = false)
                                viewModel.selectProduct(recItem.product)
                            },
                            onWishlistClick = { viewModel.toggleWishlist(recItem.product.id) },
                            onAddToCartClick = { viewModel.addToCart(recItem.product.id) },
                            modifier = Modifier.width(185.dp)
                        )
                    }
                }
            }
        }

        // 7. "Deals of the Day" (High Discounts)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            val deals = allProducts.filter { it.discountPercent >= 15 }.take(6)
            if (deals.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PaddingHeader(
                        title = "Top Discounts & Deals ⚡",
                        action = "All Deals",
                        onActionClick = { viewModel.selectTab(NavigationTab.CATALOG) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(deals) { dealProd ->
                            ProductCard(
                                product = dealProd,
                                isWishlisted = wishlistIds.contains(dealProd.id),
                                aiExplanation = "Save ${dealProd.discountPercent}% today",
                                onProductClick = { viewModel.selectProduct(dealProd) },
                                onWishlistClick = { viewModel.toggleWishlist(dealProd.id) },
                                onAddToCartClick = { viewModel.addToCart(dealProd.id) },
                                modifier = Modifier.width(185.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaddingHeader(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null && onActionClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onActionClick)
            ) {
                Text(
                    text = action,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
