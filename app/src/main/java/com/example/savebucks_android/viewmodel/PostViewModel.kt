package com.example.savebucks_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savebucks_android.datamodel.PostUiModel
import com.example.savebucks_android.repo.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    private val repository = PostRepository()

    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts(refresh: Boolean = false) {
        if (refresh) {
            _uiState.update { it.copy(page = 1, posts = emptyList()) }
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getPosts(page = _uiState.value.page)
                .onSuccess { posts ->
                    _uiState.update { state ->
                        val updatedPosts = if (state.page == 1) posts else state.posts + posts
                        state.copy(
                            posts = updatedPosts,
                            isLoading = false,
                            error = null,
                            page = state.page + 1,
                            hasMorePosts = posts.isNotEmpty()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun onPostClicked(post: PostUiModel) {
        _uiState.update { it.copy(selectedPost = post) }
    }

    fun clearSelectedPost() {
        _uiState.update { it.copy(selectedPost = null) }
    }

    fun retryLoading() {
        _uiState.update { it.copy(error = null) }
        loadPosts()
    }
}

data class PostsUiState(
    val posts: List<PostUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMorePosts: Boolean = true,
    val selectedPost: PostUiModel? = null
)