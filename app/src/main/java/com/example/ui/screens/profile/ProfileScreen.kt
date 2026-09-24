package com.example.ui.screens.profile

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SmartCartViewModel,
    onNavigateToOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val aiMetrics by viewModel.aiMetrics.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val priceAlerts by viewModel.userPriceAlerts.collectAsState()
    val notifications by viewModel.userNotifications.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var preferredCats by remember(currentUser) {
        mutableStateOf(
            currentUser?.preferredCategories?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        )
    }

    var minPriceInput by remember(currentUser) {
        mutableStateOf(currentUser?.preferredMinPrice?.toInt()?.toString() ?: "0")
    }

    var maxPriceInput by remember(currentUser) {
        mutableStateOf(currentUser?.preferredMaxPrice?.toInt()?.toString() ?: "150000")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account & AI Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("profile_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. User Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (currentUser?.role == "admin") Amber500 else Indigo600,
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser?.fullName?.take(1) ?: "U",
                                    color = PureWhite,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.fullName ?: "Guest User",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = currentUser?.email ?: "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (currentUser?.role == "admin") Amber500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (currentUser?.role == "admin") "ADMINISTRATOR" else "VERIFIED CUSTOMER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (currentUser?.role == "admin") Amber600 else Emerald600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (!currentUser?.userCode.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = currentUser?.userCode ?: "",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Demo Persona Switcher
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = Indigo600, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Switch Demo Persona", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "Test personalized customer shopping vs. administrative operations",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.switchDemoUser("customer") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentUser?.role == "customer") Indigo600 else Slate400
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Customer Demo", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel.switchDemoUser("admin") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentUser?.role == "admin") Amber600 else Slate400
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Admin Console", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 3. AI Preference Vector Customization
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Purple500, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Preference Tuning", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Text(
                            text = "Customize your category affinities and price range. The hybrid engine re-weights recommendations instantly.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Favorite Categories:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = preferredCats.contains(cat)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        preferredCats = if (isSelected) preferredCats - cat else preferredCats + cat
                                    },
                                    label = { Text("${getCategoryEmoji(cat)} $cat", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = minPriceInput,
                                onValueChange = { minPriceInput = it },
                                label = { Text("Min Budget (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxPriceInput,
                                onValueChange = { maxPriceInput = it },
                                label = { Text("Max Budget (₹)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val minP = minPriceInput.toDoubleOrNull() ?: 0.0
                                val maxP = maxPriceInput.toDoubleOrNull() ?: 150000.0
                                viewModel.updatePreferences(
                                    categories = preferredCats.joinToString(","),
                                    minPrice = minP,
                                    maxPrice = maxP
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Recalibrate AI Recommendations")
                        }
                    }
                }
            }

            // 4. Active Price Drop Alerts
            if (priceAlerts.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Blue600, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Active Price Drop Alerts (${priceAlerts.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            priceAlerts.forEach { alert ->
                                val prod = allProducts.firstOrNull { it.id == alert.productId }
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prod?.name ?: "Product #${alert.productId}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "Target: ₹${String.format("%,.0f", alert.targetPrice)} • Current: ₹${String.format("%,.0f", prod?.finalPrice ?: alert.currentPrice)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deletePriceAlert(alert.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Delete alert", tint = Rose500, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Notifications List
            if (notifications.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Amber500, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Recent Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            notifications.take(4).forEach { notif ->
                                Surface(
                                    color = if (notif.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Blue600.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { viewModel.markNotificationRead(notif.id) }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            if (!notif.isRead) {
                                                Surface(
                                                    color = Blue600,
                                                    shape = CircleShape,
                                                    modifier = Modifier.size(6.dp)
                                                ) {}
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = notif.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Quick Navigation List (Orders, etc.)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Deals & Competitor Price Comparison", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            leadingContent = { Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Rose500) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.DEALS) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("P2P Voucher Marketplace", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            leadingContent = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Emerald500) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.VOUCHERS) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("My Orders & Tracking", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            leadingContent = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Indigo600) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.clickable(onClick = onNavigateToOrders)
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("AI Shopping Assistant", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                            leadingContent = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Purple500) },
                            trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.ASSISTANT) }
                        )
                        if (currentUser?.role == "admin") {
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text("Admin Dashboard & Model Ops", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Amber600) },
                                leadingContent = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Amber600) },
                                trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.ADMIN) }
                            )
                        }
                    }
                }
            }

            // 5. Privacy & Data Control
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Responsible AI & Data Privacy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "SmartCart AI processes telemetry on-device using local Room storage. You can purge your interaction events at any time.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearInteractionHistory() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose500),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear My Interaction History")
                        }
                    }
                }
            }
        }
    }
}
