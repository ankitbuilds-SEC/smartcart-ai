package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.assistant.AiAssistantScreen
import com.example.ui.screens.cart.CartScreen
import com.example.ui.screens.catalog.CatalogScreen
import com.example.ui.screens.checkout.CheckoutScreen
import com.example.ui.screens.deals.DealsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.screens.product.ProductDetailScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.vouchers.VoucherMarketplaceScreen
import com.example.ui.theme.PureWhite
import com.example.viewmodel.NavigationTab
import com.example.viewmodel.SmartCartViewModel

@Composable
fun SmartCartApp(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCheckout by remember { mutableStateOf(false) }
    var showOrdersScreen by remember { mutableStateOf(false) }

    // Toast message handler
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            snackbarHostState.showSnackbar(toastMessage!!)
            viewModel.toastMessage.value = null
        }
    }

    val totalCartCount = remember(cartWithProducts) {
        cartWithProducts.sumOf { it.cartItem.quantity }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (selectedProduct == null && !showCheckout && !showOrdersScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavigationTab.HOME,
                        onClick = { viewModel.selectTab(NavigationTab.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_home")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.DEALS,
                        onClick = { viewModel.selectTab(NavigationTab.DEALS) },
                        icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Deals") },
                        label = { Text("Deals", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_deals")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.CATALOG,
                        onClick = { viewModel.selectTab(NavigationTab.CATALOG) },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                        label = { Text("Explore", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_catalog")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.VOUCHERS,
                        onClick = { viewModel.selectTab(NavigationTab.VOUCHERS) },
                        icon = { Icon(Icons.Default.CardGiftcard, contentDescription = "Vouchers") },
                        label = { Text("Vouchers", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_vouchers")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.CART,
                        onClick = { viewModel.selectTab(NavigationTab.CART) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalCartCount > 0) {
                                        Badge { Text("$totalCartCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text("Cart", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        modifier = Modifier.testTag("nav_cart")
                    )

                    if (currentUser?.role == "admin") {
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.ADMIN,
                            onClick = { viewModel.selectTab(NavigationTab.ADMIN) },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                            label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("nav_admin")
                        )
                    } else {
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.PROFILE,
                            onClick = { viewModel.selectTab(NavigationTab.PROFILE) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                            label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            modifier = Modifier.testTag("nav_profile")
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tab View
            when (currentTab) {
                NavigationTab.HOME -> HomeScreen(viewModel = viewModel)
                NavigationTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                NavigationTab.DEALS -> DealsScreen(viewModel = viewModel)
                NavigationTab.VOUCHERS -> VoucherMarketplaceScreen(viewModel = viewModel)
                NavigationTab.ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
                NavigationTab.CART -> CartScreen(
                    viewModel = viewModel,
                    onProceedToCheckout = { showCheckout = true }
                )
                NavigationTab.ORDERS -> OrdersScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.selectTab(NavigationTab.PROFILE) }
                )
                NavigationTab.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToOrders = { showOrdersScreen = true }
                )
                NavigationTab.ADMIN -> AdminDashboardScreen(viewModel = viewModel)
            }

            // Overlays: Product Detail View
            AnimatedVisibility(
                visible = selectedProduct != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (selectedProduct != null) {
                    ProductDetailScreen(
                        product = selectedProduct!!,
                        viewModel = viewModel,
                        onDismiss = { viewModel.selectProduct(null) }
                    )
                }
            }

            // Overlays: Checkout Wizard
            AnimatedVisibility(
                visible = showCheckout,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                CheckoutScreen(
                    viewModel = viewModel,
                    onDismiss = { showCheckout = false }
                )
            }

            // Overlays: User Orders History
            AnimatedVisibility(
                visible = showOrdersScreen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                OrdersScreen(
                    viewModel = viewModel,
                    onBack = { showOrdersScreen = false }
                )
            }
        }
    }
}
