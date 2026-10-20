package com.example.ui.screens.cart

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: SmartCartViewModel,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val complementaryProducts by viewModel.cartComplementaryProducts.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()
    val appliedPromoCode by viewModel.appliedPromoCode.collectAsState()
    val appliedDiscount by viewModel.appliedDiscount.collectAsState()
    val activePromoCampaigns by viewModel.activePromoCampaigns.collectAsState()

    var couponInput by remember { mutableStateOf("") }

    val subtotal = remember(cartWithProducts) {
        cartWithProducts.sumOf { it.product.finalPrice * it.cartItem.quantity }
    }

    val couponDiscount = appliedDiscount
    val finalTotal = (subtotal - couponDiscount).coerceAtLeast(0.0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("My Cart", fontWeight = FontWeight.Bold)
                        if (cartWithProducts.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "${cartWithProducts.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (cartWithProducts.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = Rose500)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (cartWithProducts.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                            Column {
                                Text(
                                    text = "Estimated Total",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format("%,.0f", finalTotal)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = onProceedToCheckout,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("checkout_btn")
                            ) {
                                Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier.testTag("cart_screen")
    ) { innerPadding ->
        if (cartWithProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🛒", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Your Cart is Empty", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Discover personalized recommendations in our catalog",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(onClick = { viewModel.selectTab(NavigationTab.CATALOG) }) {
                        Text("Start Shopping")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cart Items List
                items(cartWithProducts) { cartItemWithProd ->
                    val prod = cartItemWithProd.product
                    val item = cartItemWithProd.cartItem

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Emoji / Thumbnail
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = getCategoryEmoji(prod.category), fontSize = 28.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = prod.brand,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${String.format("%,.0f", prod.finalPrice)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Quantity Adjusters
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(2.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.updateCartQuantity(item.id, item.quantity - 1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = {
                                        viewModel.updateCartQuantity(item.id, item.quantity + 1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = { viewModel.removeFromCart(item.id, prod.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Rose500, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // AI Coupon Code Box
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Have an AI Promo Code?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (appliedPromoCode.isNotBlank()) {
                                    TextButton(
                                        onClick = { viewModel.removePromoCode() },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Remove", color = Rose500, fontSize = 11.sp)
                                    }
                                }
                            }

                            if (appliedPromoCode.isNotBlank()) {
                                Surface(
                                    color = Emerald500.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald500, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Code '$appliedPromoCode' applied! Saved ₹${String.format("%,.0f", couponDiscount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Emerald500
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it },
                                        placeholder = { Text("e.g. SMART10") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Button(
                                        onClick = {
                                            if (couponInput.isNotBlank()) {
                                                viewModel.applyPromoCode(couponInput, subtotal)
                                                couponInput = ""
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Apply")
                                    }
                                }

                                // Quick tap coupons
                                if (activePromoCampaigns.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Shop Mind Promo Codes (Tap to Apply):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        activePromoCampaigns.take(3).forEach { camp ->
                                            AssistChip(
                                                onClick = { viewModel.applyPromoCode(camp.promoCode, subtotal) },
                                                label = { Text(camp.promoCode, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                                leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // "Complete Your Purchase" AI Complementary Add-ons
                if (complementaryProducts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Purple500, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Complete Your Purchase (AI Recommended Add-ons)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Purple500
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(complementaryProducts) { compItem ->
                                    ProductCard(
                                        product = compItem.product,
                                        isWishlisted = wishlistIds.contains(compItem.product.id),
                                        aiExplanation = compItem.explanation,
                                        onProductClick = { viewModel.selectProduct(compItem.product) },
                                        onWishlistClick = { viewModel.toggleWishlist(compItem.product.id) },
                                        onAddToCartClick = { viewModel.addToCart(compItem.product.id) },
                                        modifier = Modifier.width(160.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Price Summary Breakdown
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Order Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", subtotal)}", fontSize = 12.sp)
                            }
                            if (couponDiscount > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("AI Promo Discount", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.SemiBold)
                                    Text("-₹${String.format("%,.0f", couponDiscount)}", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Delivery Fee", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("FREE", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("₹${String.format("%,.0f", finalTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
