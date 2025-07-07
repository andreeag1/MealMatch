package com.mealmatch.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.Post
import com.mealmatch.data.model.PostUser
import com.mealmatch.data.network.ApiClient
import com.mealmatch.ui.feed.PostAdapter
import kotlinx.coroutines.*
import com.mealmatch.data.local.TokenManager


class HomeFragment : Fragment() {

    private lateinit var captionInput: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var postButton: Button
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<Post>()

    private val api = ApiClient.postApiService
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        captionInput = view.findViewById(R.id.writeCaption)
        ratingBar = view.findViewById(R.id.writeRatingBar)
        postButton = view.findViewById(R.id.postButton)
        postRecyclerView = view.findViewById(R.id.homeRecyclerView)

        postAdapter = PostAdapter(posts)
        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.adapter = postAdapter

        postButton.setOnClickListener {
            val caption = captionInput.text.toString().trim()
            if (caption.isNotEmpty()) {
                createPost(caption)
            } else {
                showToast("Please enter a caption")
            }
        }

        loadPosts()
        return view
    }

    private fun getToken(): String {
        val token = TokenManager.getToken(requireContext())
        return token ?: throw IllegalStateException("Token not found")
    }

    private fun getCurrentUser(): PostUser {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)
            ?: throw IllegalStateException("User must be logged in")
        return PostUser(username = username)
    }

    private fun loadPosts() {
        scope.launch {
            try {
                val response = api.getPosts("Bearer ${getToken()}")
                if (response.isSuccessful && response.body()?.success == true) {
                    val fetchedPosts = response.body()?.data ?: emptyList()
                    withContext(Dispatchers.Main) {
                        posts.clear()
                        posts.addAll(fetchedPosts)
                        postAdapter.notifyDataSetChanged()
                    }
                } else {
                    showToast("Failed to load posts")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun createPost(caption: String) {
        scope.launch {
            try {
                val currentUser = getCurrentUser()
                val ratingValue = ratingBar.rating
                val newPost = Post(
                    _id = null,
                    caption = caption,
                    rating = ratingValue,
                    imageUrl = null,
                    user = currentUser
                )
                val response = api.createPost("Bearer ${getToken()}", newPost)
                if (response.isSuccessful && response.body()?.success == true) {
                    withContext(Dispatchers.Main) {
                        captionInput.text.clear()
                        ratingBar.rating = 0f
                        loadPosts()
                        showToast("Post created")
                    }
                } else {
                    showToast("Failed to create post")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
