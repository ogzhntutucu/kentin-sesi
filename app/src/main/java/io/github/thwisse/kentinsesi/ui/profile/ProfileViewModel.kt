package io.github.thwisse.kentinsesi.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.thwisse.kentinsesi.data.model.Post
import io.github.thwisse.kentinsesi.data.repository.AuthRepository
import io.github.thwisse.kentinsesi.data.repository.PostRepository
import io.github.thwisse.kentinsesi.util.Resource
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userPosts = MutableLiveData<Resource<List<Post>>>()
    val userPosts: LiveData<Resource<List<Post>>> = _userPosts

    // İstatistikler için LiveData
    private val _totalPostsCount = MutableLiveData<Int>()
    val totalPostsCount: LiveData<Int> = _totalPostsCount

    private val _resolvedPostsCount = MutableLiveData<Int>()
    val resolvedPostsCount: LiveData<Int> = _resolvedPostsCount

    // Kullanıcı bilgisi
    val currentUser = authRepository.currentUser

    init {
        loadUserPosts()
    }

    fun loadUserPosts() {
        val userId = currentUser?.uid ?: return

        viewModelScope.launch {
            _userPosts.value = Resource.Loading()
            val result = postRepository.getUserPosts(userId)

            if (result is Resource.Success) {
                val posts = result.data ?: emptyList()

                // İstatistikleri hesapla
                _totalPostsCount.value = posts.size
                _resolvedPostsCount.value = posts.count { it.status == "resolved" }
            }

            _userPosts.value = result
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}