package com.example.ui.screens.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.example.ui.components.getCategoryEmoji
import com.example.ui.theme.*
import com.example.viewmodel.AssistantMessage
import com.example.viewmodel.MessageSender
import com.example.viewmodel.SmartCartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    viewModel: SmartCartViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.assistantMessages.collectAsState()
    val isTyping by viewModel.isAssistantTyping.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val promptSuggestions = listOf(
        "Suggest a laptop under ₹60,000",
        "Best noise-cancelling headphones",
        "Top gaming accessories",
        "Find products with 4.8+ ratings",
        "Running shoes under ₹15,000"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Purple500,
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PureWhite, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Shop Mind AI Concierge", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Catalog-Grounded Intelligence", fontSize = 11.sp, color = Emerald600, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick Suggestion Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(promptSuggestions) { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                viewModel.sendAssistantMessage(suggestion)
                            }
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Chat Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about any product or budget...", fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assistant_input_field"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendAssistantMessage(inputText)
                                inputText = ""
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Indigo600),
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("assistant_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PureWhite, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        modifier = modifier.testTag("ai_assistant_screen")
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatMessageItem(
                    message = msg,
                    onProductClick = { prod -> viewModel.selectProduct(prod) },
                    onAddToCart = { prod -> viewModel.addToCart(prod.id) }
                )
            }

            if (isTyping) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Purple500,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing product features and budget...",
                            fontSize = 11.sp,
                            color = Purple500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: AssistantMessage,
    onProductClick: (ProductEntity) -> Unit,
    onAddToCart: (ProductEntity) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            if (!isUser) {
                Surface(
                    color = Purple500.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Purple500, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                Surface(
                    color = if (isUser) Indigo600 else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    shadowElevation = 1.dp
                ) {
                    Text(
                        text = message.text,
                        color = if (isUser) PureWhite else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                // If assistant returned grounded product recommendations
                if (!isUser && message.recommendedProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.recommendedProducts.forEach { prod ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onProductClick(prod) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = getCategoryEmoji(prod.category), fontSize = 22.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                        Text(text = "${prod.brand} • ${prod.rating}★", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "₹${String.format("%,.0f", prod.finalPrice)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    }

                                    FilledIconButton(
                                        onClick = { onAddToCart(prod) },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Indigo600)
                                    ) {
                                        Icon(Icons.Default.ShoppingBag, contentDescription = "Add", tint = PureWhite, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Slate700,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PureWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
