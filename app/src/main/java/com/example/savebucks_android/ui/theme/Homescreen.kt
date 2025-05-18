@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.savebucks_android.ui.theme

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.core.text.HtmlCompat
import com.example.savebucks_android.datamodel.PostUiModel
import com.example.savebucks_android.viewmodel.PostViewModel
import com.example.savebucks_android.viewmodel.PostsUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SaveBucksApp() {
    val viewModel: PostViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.selectedPost != null) {
            PostDetailScreen(
                post = uiState.selectedPost!!,
                onBackPressed = { viewModel.clearSelectedPost() }
            )
        } else {
            PostListScreen(
                uiState = uiState,
                onPostClick = { viewModel.onPostClicked(it) },
                onLoadMore = { viewModel.loadPosts() },
                onRefresh = { viewModel.loadPosts(refresh = true) },
                onRetry = { viewModel.retryLoading() }
            )
        }
    }
}

@Composable
fun PostListScreen(
    uiState: PostsUiState,
    onPostClick: (PostUiModel) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onRetry: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPosts = uiState.posts.filter {
        it.title.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("SaveBucks") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // ✅ Add Search Bar Here
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp)) // ✅ Only this makes it rounded
        )


        if (uiState.error != null && uiState.posts.isEmpty()) {
            ErrorView(error = uiState.error, onRetry = onRetry)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredPosts) { post ->
                    PostItem(post = post, onClick = { onPostClick(post) })
                }

                item {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.error != null) {
                        ErrorItem(error = uiState.error, onRetry = onRetry)
                    } else if (uiState.hasMorePosts) {
                        LoadMoreButton(onClick = onLoadMore)
                    }
                }
            }
        }
    }
}


@Composable
fun PostItem(post: PostUiModel, onClick: () -> Unit) {
    val context = LocalContext.current  // ✅ Move this to the top

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = HtmlCompat.fromHtml(post.excerpt, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}

@Composable
fun PostDetailScreen(post: PostUiModel, onBackPressed: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val fullUrl = "https://savebucks.us/${post.slug}"

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(post.title) },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Text("←", fontSize = 24.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Show image if available
            if (post.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Deal Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Deal Date
            Text(
                text = formatDate(post.date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // View Deal Button (opens deal page)
            Button(
                onClick = { uriHandler.openUri(fullUrl) },
                modifier = Modifier.align(CenterHorizontally)
            ) {
                Text("View Full Deal on Website")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Deal Content
            Text(
                text = HtmlCompat.fromHtml(post.excerpt, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = Red,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun ErrorItem(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = "Error loading more posts",
            style = MaterialTheme.typography.bodyLarge,
            color = Red
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun LoadMoreButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onClick) {
            Text("Load More")
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}