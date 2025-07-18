package com.mealmatch.ui.feed

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
import kotlinx.coroutines.*
import com.mealmatch.data.local.TokenManager
import android.text.TextWatcher
import android.text.Editable


class HomeFragment : Fragment() {

    private lateinit var captionInput: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var postButton: Button
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var postErrorText: TextView
    private lateinit var postAdapter: PostAdapter
    private val posts = mutableListOf<Post>()

    private val api = ApiClient.postApiService
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var currentErrorType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        captionInput = view.findViewById(R.id.writeCaption)
        ratingBar = view.findViewById(R.id.writeRatingBar)
        postButton = view.findViewById(R.id.postButton)
        postRecyclerView = view.findViewById(R.id.homeRecyclerView)
        postErrorText = view.findViewById(R.id.postErrorText)

        postAdapter = PostAdapter(posts)
        postRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postRecyclerView.adapter = postAdapter

        postButton.setOnClickListener {
            val caption = captionInput.text.toString().trim()
            val rating = ratingBar.rating

            if (caption.isEmpty() && rating == 0f) {
                postErrorText.setText(R.string.both_required)
                postErrorText.visibility = View.VISIBLE
                currentErrorType = "both"
            }
            else if (rating == 0f) {
                postErrorText.setText(R.string.rating_required)
                postErrorText.visibility = View.VISIBLE
                currentErrorType = "rating"
            }
            else if (caption.isEmpty()) {
                postErrorText.setText(R.string.caption_required)
                postErrorText.visibility = View.VISIBLE
                currentErrorType = "caption"
            }
            else {
                postErrorText.visibility = View.GONE
                currentErrorType = null
                createPost(caption)
            }
        }

        captionInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (currentErrorType == "caption") {
                    postErrorText.visibility = View.GONE
                    currentErrorType = null
                }
                else if (currentErrorType == "both" && ratingBar.rating != 0f) {
                    postErrorText.visibility = View.GONE
                    currentErrorType = null
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        ratingBar.setOnRatingBarChangeListener { _, _, _ ->
            val caption = captionInput.text.toString().trim()

            if (currentErrorType == "rating") {
                postErrorText.visibility = View.GONE
                currentErrorType = null
            }
            else if (currentErrorType == "both" && caption.isNotEmpty()) {
                postErrorText.visibility = View.GONE
                currentErrorType = null
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
