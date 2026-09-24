package com.example.ui.screens.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.ProductCard
import com.example.ui.components.getCategoryEmoji
import com.example.viewmodel.SmartCartViewModel
import com.example.viewmodel.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val wishlistIds by viewModel.wishlistIds.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedBrand by viewModel.selectedBrand.collectAsState()
    val priceRange by viewModel.priceRange.collectAsState()
    val minRating by viewModel.minRatingFilter.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter and Sort Pipeline
    val filteredProducts = remember(
        allProducts,
        searchQuery,
        selectedCategory,
        selectedBrand,
        priceRange,
        minRating,
        sortOption
    ) {
        allProducts.filter { product ->
            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                product.name.lowercase().contains(q) ||
                product.brand.lowercase().contains(q) ||
                product.tags.lowercase().contains(q) ||
                product.category.lowercase().contains(q)
            }

            val matchesCat = if (selectedCategory == null) true else product.category.equals(selectedCategory, ignoreCase = true)
            val matchesBrand = if (selectedBrand == null) true else product.brand.equals(selectedBrand, ignoreCase = true)
            val matchesPrice = product.finalPrice in (priceRange.start.toDouble()..priceRange.endInclusive.toDouble())
            val matchesRating = product.rating >= minRating

            matchesQuery && matchesCat && matchesBrand && matchesPrice && matchesRating
        }.let { list ->
            when (sortOption) {
                SortOption.POPULARITY -> list.sortedByDescending { it.popularityScore }
                SortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.finalPrice }
                SortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.finalPrice }
                SortOption.RATING -> list.sortedByDescending { it.rating }
                SortOption.NEWEST -> list.sortedByDescending { it.createdAt }
                SortOption.DISCOUNT -> list.sortedByDescending { it.discountPercent }
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = 8.dp)
            ) {
                // Search bar input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search catalog, brand, specifications...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("catalog_search_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                // Category Chips Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.selectedCategory.value = null },
                            label = { Text("All (${allProducts.size})") }
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                viewModel.selectedCategory.value = if (selectedCategory == cat) null else cat
                            },
                            label = { Text("${getCategoryEmoji(cat)} $cat") }
                        )
                    }
                }

                // Filter & Sort Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProducts.size} Products found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Sort Button
                        Box {
                            OutlinedButton(
                                onClick = { showSortMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sort", fontSize = 12.sp)
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Most Popular") },
                                    onClick = { viewModel.sortOption.value = SortOption.POPULARITY; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Price: Low to High") },
                                    onClick = { viewModel.sortOption.value = SortOption.PRICE_LOW_TO_HIGH; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Price: High to Low") },
                                    onClick = { viewModel.sortOption.value = SortOption.PRICE_HIGH_TO_LOW; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Highest Rated") },
                                    onClick = { viewModel.sortOption.value = SortOption.RATING; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Newest Arrivals") },
                                    onClick = { viewModel.sortOption.value = SortOption.NEWEST; showSortMenu = false }
                                )
                            }
                        }

                        // Filter Button
                        FilledTonalButton(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Filters", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        modifier = modifier.testTag("catalog_screen")
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredProducts.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No products found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search keywords or resetting price filters",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.searchQuery.value = ""
                                    viewModel.selectedCategory.value = null
                                    viewModel.selectedBrand.value = null
                                    viewModel.priceRange.value = 0f..200000f
                                    viewModel.minRatingFilter.value = 0f
                                }
                            ) {
                                Text("Reset All Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        isWishlisted = wishlistIds.contains(product.id),
                        aiExplanation = if (product.isFeatured) "Featured in ${product.category}" else null,
                        onProductClick = { viewModel.selectProduct(product) },
                        onWishlistClick = { viewModel.toggleWishlist(product.id) },
                        onAddToCartClick = { viewModel.addToCart(product.id) }
                    )
                }
            }
        }
    }

    // Filter Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Refine Products",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Range Slider
                Text(
                    text = "Price Range: ₹${String.format("%,.0f", priceRange.start)} - ₹${String.format("%,.0f", priceRange.endInclusive)}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                RangeSlider(
                    value = priceRange,
                    onValueChange = { viewModel.priceRange.value = it },
                    valueRange = 0f..200000f,
                    steps = 20
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Minimum Rating Filter
                Text(
                    text = "Minimum Customer Rating: ${if (minRating > 0) "${minRating}★ and above" else "Any Rating"}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Slider(
                    value = minRating,
                    onValueChange = { viewModel.minRatingFilter.value = it },
                    valueRange = 0f..4.5f,
                    steps = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.priceRange.value = 0f..200000f
                            viewModel.minRatingFilter.value = 0f
                            showFilterSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }
}
