package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import com.example.config.AppConfig
import com.example.config.AppEnvironment
import com.example.data.model.ProductEntity
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val adminStats by viewModel.adminStats.collectAsState()
    val aiMetrics by viewModel.aiMetrics.collectAsState()
    val activeExperiment by viewModel.activeExperiment.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val isRetraining by viewModel.isRetraining.collectAsState()
    val retrainStatusMessage by viewModel.retrainStatusMessage.collectAsState()
    val pendingVouchers by viewModel.pendingVouchers.collectAsState()
    val merchantProviders = remember { viewModel.repository.merchantRegistry.allSupportedProviders }
    val currentEnv by AppConfig.environmentState.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: Intelligence & Retraining, 1: A/B Testing, 2: Inventory, 3: Audit Logs, 4: Feeds & Vouchers

    // Dialog state for adding a product
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Dialog state for editing stock
    var productToEditStock by remember { mutableStateOf<ProductEntity?>(null) }
    var newStockInput by remember { mutableStateOf("") }

    val totalRevenue = (adminStats["totalRevenue"] as? Double) ?: 426800.0
    val totalOrders = (adminStats["totalOrders"] as? Int) ?: 18
    val totalProducts = (adminStats["totalProducts"] as? Int) ?: 100
    val totalCustomers = (adminStats["totalCustomers"] as? Int) ?: 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Amber500,
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Admin Operations & AI Hub", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Live Telemetry & Model Ops", fontSize = 11.sp, color = Emerald600, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.selectTab(NavigationTab.HOME) }) {
                        Text("Exit Admin", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("admin_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Executive Summary Metric Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Gross Revenue",
                        value = "₹${String.format("%,.0f", totalRevenue)}",
                        icon = Icons.Default.CurrencyRupee,
                        tint = Emerald600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Total Orders",
                        value = "$totalOrders",
                        icon = Icons.Default.ShoppingBag,
                        tint = Indigo600,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Catalog Size",
                        value = "$totalProducts SKUs",
                        icon = Icons.Default.Inventory2,
                        tint = Amber600,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "AI Model Health",
                        value = "${String.format("%.1f", aiMetrics.ndcgAt5 * 100)}% NDCG",
                        icon = Icons.Default.AutoAwesome,
                        tint = Purple500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Tab Navigation
            item {
                TabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedAdminTab == 0,
                        onClick = { selectedAdminTab = 0 },
                        text = { Text("Model Ops", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 1,
                        onClick = { selectedAdminTab = 1 },
                        text = { Text("A/B Testing", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 2,
                        onClick = { selectedAdminTab = 2 },
                        text = { Text("Inventory (${lowStockProducts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 3,
                        onClick = { selectedAdminTab = 3 },
                        text = { Text("Audit Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 4,
                        onClick = { selectedAdminTab = 4 },
                        text = { Text("Feeds & Vouchers (${pendingVouchers.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // 3. TAB CONTENT

            // TAB 0: Recommendation Model Ops & Retraining
            if (selectedAdminTab == 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Recommendation Engine Ops", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Version: ${aiMetrics.modelVersion}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Surface(
                                    color = Emerald500.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LIVE SERVING",
                                        color = Emerald600,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Model Metrics Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MetricItem(label = "Precision@5", value = "${String.format("%.1f", aiMetrics.precisionAt5 * 100)}%")
                                MetricItem(label = "Recall@5", value = "${String.format("%.1f", aiMetrics.recallAt5 * 100)}%")
                                MetricItem(label = "NDCG@5", value = "${String.format("%.1f", aiMetrics.ndcgAt5 * 100)}%")
                                MetricItem(label = "Coverage", value = "${String.format("%.1f", aiMetrics.catalogCoveragePercent)}%")
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Training Dataset: ${aiMetrics.totalTrainingSamples} interaction events, purchase records, and content vectors.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (retrainStatusMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Purple500.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = retrainStatusMessage!!,
                                        fontSize = 11.sp,
                                        color = Purple500,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.retrainModel() },
                                enabled = !isRetraining,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("retrain_model_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple500),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isRetraining) {
                                    CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retraining Hybrid Model...", fontSize = 13.sp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retrain Recommendation Model", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: A/B Testing Intelligence
            if (selectedAdminTab == 1) {
                item {
                    val exp = activeExperiment
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = exp?.experimentName ?: "Hybrid AI vs Popularity Baseline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Split: 50% Control (Popularity Only) vs 50% Treatment (Hybrid AI Pipeline)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Variant A (Control)
                            val clicksA = exp?.clicksA ?: 320
                            val convA = exp?.conversionsA ?: 18
                            val ctrA = (clicksA.toDouble() / (exp?.impressionsA ?: 1000).coerceAtLeast(1)) * 100
                            val convRateA = (convA.toDouble() / clicksA.coerceAtLeast(1)) * 100

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Variant A: Control (Popularity Baseline)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Impressions: ${exp?.impressionsA ?: 1000}", fontSize = 11.sp)
                                        Text("Clicks: $clicksA (CTR: ${String.format("%.1f", ctrA)}%)", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Conversions: $convA (${String.format("%.1f", convRateA)}%)", fontSize = 11.sp)
                                        Text("Revenue: ₹${String.format("%,.0f", exp?.revenueA ?: 84200.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Variant B (Hybrid AI)
                            val clicksB = exp?.clicksB ?: 580
                            val convB = exp?.conversionsB ?: 46
                            val ctrB = (clicksB.toDouble() / (exp?.impressionsB ?: 1000).coerceAtLeast(1)) * 100
                            val convRateB = (convB.toDouble() / clicksB.coerceAtLeast(1)) * 100

                            Surface(
                                color = Purple500.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Variant B: Treatment (Hybrid AI Engine)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Purple500)
                                        Surface(color = Emerald500, shape = RoundedCornerShape(4.dp)) {
                                            Text("+155% LIFT", color = PureWhite, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Impressions: ${exp?.impressionsB ?: 1000}", fontSize = 11.sp)
                                        Text("Clicks: $clicksB (CTR: ${String.format("%.1f", ctrB)}%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Conversions: $convB (${String.format("%.1f", convRateB)}%)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Revenue: ₹${String.format("%,.0f", exp?.revenueB ?: 342600.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: Inventory & Low Stock Alerts
            if (selectedAdminTab == 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Low Stock Items (<15 units)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Button(
                            onClick = { showAddProductDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Product", fontSize = 12.sp)
                        }
                    }
                }

                items(lowStockProducts) { prod ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = getCategoryEmoji(prod.category), fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("SKU: #${prod.id} • ${prod.category} • ₹${String.format("%,.0f", prod.finalPrice)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Surface(
                                color = if (prod.stock <= 5) Rose500.copy(alpha = 0.2f) else Amber500.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${prod.stock} left",
                                    color = if (prod.stock <= 5) Rose500 else Amber600,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(onClick = {
                                productToEditStock = prod
                                newStockInput = prod.stock.toString()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Stock", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // TAB 3: Operational Audit Logs
            if (selectedAdminTab == 3) {
                items(auditLogs) { log ->
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("By: ${log.actorEmail}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // TAB 4: Merchant Integrations & Voucher Moderation
            if (selectedAdminTab == 4) {
                item {
                    Text(
                        text = "Merchant Providers & Feed Integration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Isolated merchant providers. Live connections require authorized credentials.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Environment Mode Configuration Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentEnv == AppEnvironment.PRODUCTION) Emerald500.copy(alpha = 0.1f) else Blue600.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (currentEnv == AppEnvironment.PRODUCTION) Icons.Default.VerifiedUser else Icons.Default.Science,
                                        contentDescription = null,
                                        tint = if (currentEnv == AppEnvironment.PRODUCTION) Emerald600 else Blue600,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Environment: ${currentEnv.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppConfig.environmentBannerText,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { AppConfig.toggleEnvironment() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = if (currentEnv == AppEnvironment.DEMO) "Switch to PROD" else "Switch to DEMO",
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                items(merchantProviders) { provider ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
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
                                        text = provider.merchantName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = provider.integrationType,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Domain: ${provider.domain} • ID: ${provider.providerId}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = provider.statusMessage,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = Color(provider.status.badgeColorHex).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = provider.status.name,
                                    color = Color(provider.status.badgeColorHex),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Vouchers (${pendingVouchers.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (pendingVouchers.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No pending vouchers in review queue.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(pendingVouchers) { voucher ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${voucher.brand} (${voucher.voucherType})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Surface(
                                        color = Indigo600.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "₹${String.format("%,.0f", voucher.faceValue)} for ₹${String.format("%,.0f", voucher.sellingPrice)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Indigo600,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Seller: ${voucher.sellerName} (${voucher.sellerEmail})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Masked Token: ${voucher.verificationInfo} • Expires: ${voucher.expiryDate}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.verifyVoucher(voucher.id, false, "Declined during admin compliance review.") },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Reject", fontSize = 11.sp, color = Rose500)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.verifyVoucher(voucher.id, true) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Approve", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Stock Edit Dialog
    if (productToEditStock != null) {
        val prod = productToEditStock!!
        AlertDialog(
            onDismissRequest = { productToEditStock = null },
            title = { Text("Update Inventory: ${prod.name}") },
            text = {
                Column {
                    Text("Enter new stock level:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newStockInput,
                        onValueChange = { newStockInput = it },
                        label = { Text("Stock Units") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stockNum = newStockInput.toIntOrNull() ?: prod.stock
                        viewModel.updateProductStock(prod, stockNum)
                        productToEditStock = null
                    }
                ) {
                    Text("Save Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToEditStock = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
