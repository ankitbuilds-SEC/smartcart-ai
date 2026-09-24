package com.example.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.ui.theme.*
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: SmartCartViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val orderSuccess by viewModel.orderSuccess.collectAsState()

    var step by remember { mutableIntStateOf(1) } // 1: Address, 2: Payment, 3: Review
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "Ankit Sharma") }
    var addressLine by remember { mutableStateOf("42, Silicon Valley Avenue, Koramangala") }
    var city by remember { mutableStateOf("Bengaluru") }
    var pinCode by remember { mutableStateOf("560034") }
    var selectedPayment by remember { mutableStateOf("Simulated UPI (Google Pay)") }
    var isProcessing by remember { mutableStateOf(false) }

    val subtotal = remember(cartWithProducts) {
        cartWithProducts.sumOf { it.product.finalPrice * it.cartItem.quantity }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orderSuccess != null) "Order Confirmed!" else "Secure Checkout", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (orderSuccess != null) {
                                viewModel.orderSuccess.value = null
                                onDismiss()
                            } else if (step > 1) {
                                step--
                            } else {
                                onDismiss()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.testTag("checkout_screen")
    ) { innerPadding ->
        if (orderSuccess != null) {
            val order = orderSuccess!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Emerald500.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(44.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Order Placed Successfully!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Order ID: ${order.orderNumber}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Tracking: ${order.trackingNumber}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Delivery Address:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(order.shippingAddress, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Amount Paid:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${String.format("%,.0f", order.finalAmount)} via ${order.paymentMethod}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.orderSuccess.value = null
                                onDismiss()
                                viewModel.selectTab(NavigationTab.PROFILE)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Orders History")
                        }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step Indicator
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepPill(number = 1, label = "Address", isActive = step == 1, isCompleted = step > 1)
                        Box(modifier = Modifier.width(30.dp).height(2.dp).background(if (step > 1) Emerald500 else Slate400))
                        StepPill(number = 2, label = "Payment", isActive = step == 2, isCompleted = step > 2)
                        Box(modifier = Modifier.width(30.dp).height(2.dp).background(if (step > 2) Emerald500 else Slate400))
                        StepPill(number = 3, label = "Review", isActive = step == 3, isCompleted = false)
                    }
                }

                // Step 1: Address
                if (step == 1) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Shipping Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = addressLine,
                                    onValueChange = { addressLine = it },
                                    label = { Text("Street Address") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = pinCode,
                                        onValueChange = { pinCode = it },
                                        label = { Text("PIN Code") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { step = 2 },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Continue to Payment")
                                }
                            }
                        }
                    }
                }

                // Step 2: Payment Method
                if (step == 2) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Demonstration Sandbox - No actual charge will occur", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(14.dp))

                                val paymentOptions = listOf(
                                    "Simulated UPI (Google Pay / PhonePe)",
                                    "Simulated Credit / Debit Card",
                                    "Simulated Net Banking",
                                    "Cash on Delivery"
                                )

                                paymentOptions.forEach { opt ->
                                    Surface(
                                        color = if (selectedPayment == opt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedPayment = opt }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedPayment == opt,
                                                onClick = { selectedPayment = opt }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = opt, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = { step = 3 },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Review Order")
                                }
                            }
                        }
                    }
                }

                // Step 3: Review & Place Order
                if (step == 3) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Final Order Review", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Deliver to: $fullName", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("$addressLine, $city - $pinCode", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Payment: $selectedPayment", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Items (${cartWithProducts.size}):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                cartWithProducts.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${item.product.name} (x${item.cartItem.quantity})", fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("₹${String.format("%,.0f", item.product.finalPrice * item.cartItem.quantity)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Payable:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("₹${String.format("%,.0f", subtotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = {
                                        isProcessing = true
                                        val fullAddr = "$fullName, $addressLine, $city - $pinCode"
                                        viewModel.placeOrder(fullAddr, selectedPayment, "")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("confirm_order_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(color = PureWhite, modifier = Modifier.size(22.dp))
                                    } else {
                                        Text("Pay ₹${String.format("%,.0f", subtotal)} & Place Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepPill(number: Int, label: String, isActive: Boolean, isCompleted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = when {
                isCompleted -> Emerald500
                isActive -> Indigo600
                else -> Slate400
            },
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                } else {
                    Text("$number", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
