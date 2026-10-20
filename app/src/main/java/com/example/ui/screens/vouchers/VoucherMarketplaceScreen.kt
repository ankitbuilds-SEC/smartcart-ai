package com.example.ui.screens.vouchers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VoucherListingEntity
import com.example.data.model.VoucherTransactionEntity
import com.example.ui.theme.*
import com.example.viewmodel.SmartCartViewModel

enum class VoucherMarketTab {
    BROWSE,
    SELL,
    MY_VOUCHERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherMarketplaceScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val verifiedVouchers by viewModel.verifiedVouchers.collectAsState()
    val mySellerVouchers by viewModel.mySellerVouchers.collectAsState()
    val myPurchases by viewModel.myPurchasedVouchers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTab by remember { mutableStateOf(VoucherMarketTab.BROWSE) }
    var selectedVoucherToBuy by remember { mutableStateOf<VoucherListingEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald500.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = Emerald500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Voucher Marketplace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Verified peer-to-peer gift cards & credits",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("voucher_marketplace_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Escrow Guarantee Banner
            Surface(
                color = Emerald500.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Demo Escrow Protection (Sandbox Mode): In live production, funds are held by an authorized escrow gateway until voucher code is verified by buyer.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            // Tabs row
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == VoucherMarketTab.BROWSE,
                    onClick = { selectedTab = VoucherMarketTab.BROWSE },
                    text = { Text("Browse (${verifiedVouchers.size})", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == VoucherMarketTab.SELL,
                    onClick = { selectedTab = VoucherMarketTab.SELL },
                    text = { Text("List / Sell", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == VoucherMarketTab.MY_VOUCHERS,
                    onClick = { selectedTab = VoucherMarketTab.MY_VOUCHERS },
                    text = { Text("My Vouchers", fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (selectedTab) {
                VoucherMarketTab.BROWSE -> {
                    BrowseVouchersTab(
                        vouchers = verifiedVouchers,
                        onBuyClick = { voucher -> selectedVoucherToBuy = voucher }
                    )
                }
                VoucherMarketTab.SELL -> {
                    SellVoucherTab(
                        isLoggedIn = currentUser != null,
                        onSubmit = { brand, type, faceValue, price, expiry, terms, verification, agree ->
                            viewModel.createVoucherListing(
                                brand = brand,
                                voucherType = type,
                                faceValue = faceValue,
                                sellingPrice = price,
                                expiryDate = expiry,
                                terms = terms,
                                verificationInfo = verification,
                                agreementAccepted = agree
                            )
                            selectedTab = VoucherMarketTab.MY_VOUCHERS
                        }
                    )
                }
                VoucherMarketTab.MY_VOUCHERS -> {
                    MyVouchersTab(
                        sellerListings = mySellerVouchers,
                        purchasedVouchers = myPurchases
                    )
                }
            }
        }
    }

    // Buy Voucher Bottom Sheet / Dialog
    if (selectedVoucherToBuy != null) {
        BuyVoucherDialog(
            voucher = selectedVoucherToBuy!!,
            onDismiss = { selectedVoucherToBuy = null },
            onConfirm = { paymentMethod ->
                viewModel.buyVoucher(selectedVoucherToBuy!!.id, paymentMethod)
                selectedVoucherToBuy = null
                selectedTab = VoucherMarketTab.MY_VOUCHERS
            }
        )
    }
}

@Composable
fun BrowseVouchersTab(
    vouchers: List<VoucherListingEntity>,
    onBuyClick: (VoucherListingEntity) -> Unit
) {
    if (vouchers.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No verified vouchers currently available",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Check back soon or list your unused vouchers under 'List / Sell'!",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(vouchers) { voucher ->
                VoucherCard(
                    voucher = voucher,
                    onBuyClick = { onBuyClick(voucher) }
                )
            }
        }
    }
}

@Composable
fun VoucherCard(
    voucher: VoucherListingEntity,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (voucher.brand) {
                            "Amazon" -> Amber500.copy(alpha = 0.15f)
                            "Flipkart" -> Blue600.copy(alpha = 0.15f)
                            "Myntra" -> Rose500.copy(alpha = 0.15f)
                            "Swiggy" -> Color(0xFFFF5722).copy(alpha = 0.15f)
                            "Uber" -> Slate900.copy(alpha = 0.15f)
                            else -> Purple500.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = voucher.brand,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (voucher.brand) {
                                "Amazon" -> Color(0xFFD97706)
                                "Flipkart" -> Blue600
                                "Myntra" -> Rose500
                                "Swiggy" -> Color(0xFFFF5722)
                                "Uber" -> Slate900
                                else -> Purple500
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = voucher.voucherType,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = Emerald500.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${voucher.discountPercent}% OFF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Emerald500,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Face Value: ₹${String.format("%,.0f", voucher.faceValue)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Text(
                        text = "₹${String.format("%,.0f", voucher.sellingPrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Expires: ${voucher.expiryDate}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Instant Buy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Blue600, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified Seller: ${voucher.sellerName}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Escrow Guaranteed",
                    fontSize = 10.sp,
                    color = Emerald500,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SellVoucherTab(
    isLoggedIn: Boolean,
    onSubmit: (String, String, Double, Double, String, String, String, Boolean) -> Unit
) {
    var brand by remember { mutableStateOf("Amazon") }
    var voucherType by remember { mutableStateOf("Gift Card") }
    var faceValueStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("2025-12-31") }
    var terms by remember { mutableStateOf("Valid across all catalog products. Cannot be exchanged for cash.") }
    var verificationInfo by remember { mutableStateOf("") }
    var agreementAccepted by remember { mutableStateOf(false) }

    val faceValue = faceValueStr.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceStr.toDoubleOrNull() ?: 0.0
    val calculatedDiscount = if (faceValue > 0 && sellingPrice > 0 && sellingPrice < faceValue) {
        (((faceValue - sellingPrice) / faceValue) * 100).toInt()
    } else 0

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "List an Unused Gift Card or Voucher",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        Text(
            text = "Turn unwanted gift cards into instant cash. All listings undergo strict compliance verification to ensure platform trust.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Brand selection
        Column {
            Text("Select Brand", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val brands = listOf("Amazon", "Flipkart", "Myntra", "Swiggy", "Uber", "Zomato", "BookMyShow", "Croma")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                brands.take(4).forEach { b ->
                    FilterChip(
                        selected = brand == b,
                        onClick = { brand = b },
                        label = { Text(b, fontSize = 11.sp) }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                brands.drop(4).forEach { b ->
                    FilterChip(
                        selected = brand == b,
                        onClick = { brand = b },
                        label = { Text(b, fontSize = 11.sp) }
                    )
                }
            }
        }

        // Voucher Type
        Column {
            Text("Voucher Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Gift Card", "Promo Code", "Store Credit").forEach { type ->
                    FilterChip(
                        selected = voucherType == type,
                        onClick = { voucherType = type },
                        label = { Text(type, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Face Value & Selling Price
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = faceValueStr,
                onValueChange = { faceValueStr = it },
                label = { Text("Face Value (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = sellingPriceStr,
                onValueChange = { sellingPriceStr = it },
                label = { Text("Selling Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        if (calculatedDiscount > 0) {
            Surface(
                color = Emerald500.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Emerald500, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buyer discount: $calculatedDiscount% OFF face value. Competitive pricing ensures fast sales!",
                        fontSize = 11.sp,
                        color = Emerald500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        OutlinedTextField(
            value = expiryDate,
            onValueChange = { expiryDate = it },
            label = { Text("Expiry Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = verificationInfo,
            onValueChange = { verificationInfo = it },
            label = { Text("Voucher Code / PIN (Confidential)") },
            placeholder = { Text("e.g., GC-9482-XXXX-XXXX (Protected until sale)") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Encrypted. Never shared until payment is confirmed.") }
        )

        OutlinedTextField(
            value = terms,
            onValueChange = { terms = it },
            label = { Text("Terms & Conditions") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // Mandatory Agreement Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { agreementAccepted = !agreementAccepted }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreementAccepted,
                onCheckedChange = { agreementAccepted = it }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "I verify that this voucher is authentic, legally acquired, unredeemed, and adheres to Shop Mind AI anti-fraud policies.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Button(
            onClick = {
                if (faceValue > 0 && sellingPrice > 0 && verificationInfo.isNotBlank() && agreementAccepted) {
                    onSubmit(brand, voucherType, faceValue, sellingPrice, expiryDate, terms, verificationInfo, agreementAccepted)
                }
            },
            enabled = faceValue > 0 && sellingPrice > 0 && verificationInfo.isNotBlank() && agreementAccepted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit for Verification & Escrow Listing", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MyVouchersTab(
    sellerListings: List<VoucherListingEntity>,
    purchasedVouchers: List<VoucherTransactionEntity>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. My Listings
        item {
            Text(
                text = "My Listings (${sellerListings.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (sellerListings.isEmpty()) {
            item {
                Text(
                    text = "You haven't listed any vouchers yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sellerListings) { voucher ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${voucher.brand} ₹${voucher.faceValue.toInt()} Voucher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Surface(
                                color = when (voucher.status) {
                                    "VERIFIED" -> Emerald500.copy(alpha = 0.15f)
                                    "PENDING_VERIFICATION" -> Amber500.copy(alpha = 0.15f)
                                    "SOLD" -> Blue600.copy(alpha = 0.15f)
                                    else -> Rose500.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = voucher.status.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (voucher.status) {
                                        "VERIFIED" -> Emerald500
                                        "PENDING_VERIFICATION" -> Color(0xFFD97706)
                                        "SOLD" -> Blue600
                                        else -> Rose500
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Selling for ₹${voucher.sellingPrice.toInt()} • Expiry: ${voucher.expiryDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (voucher.status == "PENDING_VERIFICATION") {
                            Text(
                                text = "🛡️ Under review by Shop Mind AI compliance admin",
                                fontSize = 10.sp,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. My Purchased Vouchers
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "My Purchased Vouchers (${purchasedVouchers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (purchasedVouchers.isEmpty()) {
            item {
                Text(
                    text = "No purchased vouchers. Buy verified gift cards at a discount in the Browse tab!",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(purchasedVouchers) { txn ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald500.copy(alpha = 0.06f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${txn.brand} Voucher",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Paid ₹${txn.amountPaid.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Emerald500
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Token: ${txn.transactionToken}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Payment: ${txn.paymentMethod} • Status: ${txn.paymentStatus}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyVoucherDialog(
    voucher: VoucherListingEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf("UPI / Google Pay") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Emerald500, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Demo Escrow Purchase (Sandbox)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "You are purchasing a ${voucher.brand} ${voucher.voucherType} (Face value: ₹${voucher.faceValue.toInt()}) for ₹${voucher.sellingPrice.toInt()}.",
                    fontSize = 13.sp
                )

                Surface(
                    color = Emerald500.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🛡️ Demo Escrow: In sandbox mode. Demonstration transaction held in simulated escrow until code redemption is verified.",
                        fontSize = 11.sp,
                        color = Emerald500,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text("Select Payment Method:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                listOf("UPI / Google Pay", "Credit / Debit Card", "Net Banking").forEach { method ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = method }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedPaymentMethod == method,
                            onClick = { selectedPaymentMethod = method }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPaymentMethod) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
            ) {
                Text("Simulate Escrow Payment (₹${voucher.sellingPrice.toInt()})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
