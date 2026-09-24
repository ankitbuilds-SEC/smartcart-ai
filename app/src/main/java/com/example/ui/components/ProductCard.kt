package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.theme.*

@Composable
fun ProductCard(
    product: ProductEntity,
    isWishlisted: Boolean = false,
    aiExplanation: String? = null,
    onProductClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onProductClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual Product Header / Hero Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                getCategoryGradientStart(product.category),
                                getCategoryGradientEnd(product.category)
                            )
                        )
                    )
            ) {
                // Category & Brand Tag
                Surface(
                    color = Slate900.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.brand,
                        color = PureWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Wishlist Toggle Button
                IconButton(
                    onClick = onWishlistClick,
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .background(Slate900.copy(alpha = 0.4f), CircleShape)
                        .testTag("wishlist_btn_${product.id}")
                ) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) Rose500 else PureWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Center Icon Silhouette / Category Graphic
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = getCategoryEmoji(product.category),
                        fontSize = 38.sp
                    )
                    Text(
                        text = product.category,
                        color = PureWhite.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Discount Pill if available
                if (product.discountPercent > 0) {
                    Surface(
                        color = Rose500,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Rating row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Amber500,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${product.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "(${product.reviewCount})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (product.stock in 1..10) {
                        Text(
                            text = "Only ${product.stock} left",
                            color = Rose500,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Product Name
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Explainable AI Pill
                if (!aiExplanation.isNullOrBlank()) {
                    Surface(
                        color = Purple500.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Match",
                                tint = Purple500,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = aiExplanation,
                                fontSize = 10.sp,
                                color = Purple500,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Price and Add-to-cart row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "₹${String.format("%,.0f", product.finalPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (product.discountPercent > 0) {
                            Text(
                                text = "₹${String.format("%,.0f", product.price)}",
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Add Button
                    FilledIconButton(
                        onClick = onAddToCartClick,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = PureWhite
                        ),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("add_cart_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Add to Cart",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "electronics" -> "💻"
        "smartphones" -> "📱"
        "audio & wearables", "audio" -> "🎧"
        "gaming" -> "🎮"
        "smart home" -> "🏠"
        "fashion" -> "👟"
        "fitness & sports", "sports" -> "⚡"
        "books" -> "📚"
        "accessories" -> "🔌"
        else -> "✨"
    }
}

fun getCategoryGradientStart(category: String): Color {
    return when (category.lowercase()) {
        "electronics" -> Color(0xFF1E3A8A)
        "smartphones" -> Color(0xFF4338CA)
        "audio & wearables", "audio" -> Color(0xFF581C87)
        "gaming" -> Color(0xFF701A75)
        "smart home" -> Color(0xFF065F46)
        "fashion" -> Color(0xFF831843)
        "fitness & sports", "sports" -> Color(0xFF9A3412)
        "books" -> Color(0xFF14532D)
        "accessories" -> Color(0xFF0F766E)
        else -> Slate800
    }
}

fun getCategoryGradientEnd(category: String): Color {
    return when (category.lowercase()) {
        "electronics" -> Color(0xFF3B82F6)
        "smartphones" -> Color(0xFF6366F1)
        "audio & wearables", "audio" -> Color(0xFF9333EA)
        "gaming" -> Color(0xFFC026D3)
        "smart home" -> Color(0xFF10B981)
        "fashion" -> Color(0xFFE11D48)
        "fitness & sports", "sports" -> Color(0xFFEA580C)
        "books" -> Color(0xFF16A34A)
        "accessories" -> Color(0xFF14B8A6)
        else -> Slate700
    }
}
