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
import androidx.appcompat.app.AlertDialog
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.mealmatch.data.model.Media
import com.mealmatch.data.network.FirebaseMediaManager

class HomeFragment : Fragment() {

    private lateinit var captionInput: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var postButton: Button
    private lateinit var postRecyclerView: RecyclerView
    private lateinit var postErrorText: TextView
    private lateinit var clearButton: Button
    private lateinit var postAdapter: PostAdapter
    private lateinit var currentUsername: String
    private val posts = mutableListOf<Post>()
    private val allPosts = mutableListOf<Post>()

    private enum class FilterType { ALL, FRIENDS, ME }
    private var currentFilter = FilterType.ALL

    private lateinit var filterAllButton: Button
    private lateinit var filterFriendsButton: Button
    private lateinit var filterMeButton: Button

    private lateinit var toggleWritePostButton: Button
    private lateinit var writePostCard: androidx.cardview.widget.CardView
    private var isWritePostVisible = false

    private lateinit var mediaRecyclerView: RecyclerView
    private lateinit var buttonAddMedia: Button
    private lateinit var mediaAdapter: MediaAdapter
    private val selectedMedia = mutableListOf<Media>()

    private val api = ApiClient.postApiService
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var currentErrorType: String? = null

    private val friendRepository = com.mealmatch.data.network.repository.FriendRepository()
    private val friendsList = mutableListOf<String>()

    private var editingPost: Post? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (!uris.isNullOrEmpty()) {
            uploadMultipleMediaFiles(uris)
        }
    }

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
        clearButton = view.findViewById(R.id.clearButton)
        mediaRecyclerView = view.findViewById(R.id.mediaRecyclerView)
        buttonAddMedia = view.findViewById(R.id.mediaButton)
        filterAllButton = view.findViewById(R.id.filterAllButton)
        filterFriendsButton = view.findViewById(R.id.filterFriendsButton)
        filterMeButton = view.findViewById(R.id.filterMeButton)
        toggleWritePostButton = view.findViewById(R.id.toggleWritePostButton)
        writePostCard = view.findViewById(R.id.writePostCard)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentUsername = prefs.getString("username", null) ?: ""

        postAdapter = PostAdapter(posts, currentUsername,
            { post -> deletePost(post) },
            { post -> enterEditMode(post) }
        )
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

                if (editingPost != null) {
                    updatePost(editingPost!!, caption, rating, selectedMedia.toList())
                } else {
                    createPost(caption)
                }
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

        clearButton.setOnClickListener {
            captionInput.text.clear()
            ratingBar.rating = 0f
            resetEditMode()
            postErrorText.visibility = View.GONE

            val mediaToDelete = selectedMedia.toList()
            selectedMedia.clear()
            mediaAdapter.notifyDataSetChanged()
            updateMediaRecyclerViewVisibility()

            for (media in mediaToDelete) {
                FirebaseMediaManager.deleteMedia(
                    storagePath = media.storagePath,
                    onSuccess = {},
                    onFailure = { exception ->
                        showToast("Error deleting media: ${exception.message}")
                    }
                )
            }
        }

        mediaAdapter = MediaAdapter(
            selectedMedia,
            { position -> removeMediaAt(position) },
            true,
            R.layout.item_media
        )

        mediaRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        mediaRecyclerView.adapter = mediaAdapter

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.media_item_spacing)
        mediaRecyclerView.addItemDecoration(GridSpacing(3, spacingInPixels))

        buttonAddMedia.setOnClickListener {
            getContent.launch("*/*")
        }

        filterAllButton.setOnClickListener {
            currentFilter = FilterType.ALL
            updateFilterButtonColors()
            applyFilter()
        }

        filterFriendsButton.setOnClickListener {
            currentFilter = FilterType.FRIENDS
            updateFilterButtonColors()
            applyFilter()
        }

        filterMeButton.setOnClickListener {
            currentFilter = FilterType.ME
            updateFilterButtonColors()
            applyFilter()
        }

        toggleWritePostButton.setOnClickListener {
            toggleWritePostSection()
        }

        loadPosts()
        loadFriends()
        resetFilter()
        return view
    }

    private fun resetFilter() {
        currentFilter = FilterType.ALL
        updateFilterButtonColors()
        applyFilter()
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
                        allPosts.clear()
                        allPosts.addAll(fetchedPosts)
                        applyFilter()
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
                    media = selectedMedia.toList(),
                    user = currentUser
                )
                val response = api.createPost("Bearer ${getToken()}", newPost)
                if (response.isSuccessful && response.body()?.success == true) {
                    withContext(Dispatchers.Main) {
                        captionInput.text.clear()
                        ratingBar.rating = 0f
                        selectedMedia.clear()
                        mediaAdapter.notifyDataSetChanged()
                        updateMediaRecyclerViewVisibility()
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

    private fun enterEditMode(post: Post) {
        editingPost = post
        captionInput.setText(post.caption)
        ratingBar.rating = post.rating
        selectedMedia.clear()
        selectedMedia.addAll(post.media)
        mediaAdapter.notifyDataSetChanged()
        updateMediaRecyclerViewVisibility()
        writePostCard.visibility = View.VISIBLE
        toggleWritePostButton.text = "Hide Review"
        isWritePostVisible = true
        postButton.text = "Save Changes"
    }

    private fun updatePost(post: Post, caption: String, rating: Float, media: List<Media>) {
        scope.launch {
            try {
                val updatedPost = post.copy(
                    caption = caption,
                    rating = rating,
                    media = media
                )
                val response = api.updatePost("Bearer ${getToken()}", post._id!!, updatedPost)
                if (response.isSuccessful && response.body()?.success == true) {
                    withContext(Dispatchers.Main) {
                        showToast("Post updated")
                        resetEditMode()
                        loadPosts()
                    }
                } else {
                    showToast("Failed to update post")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun resetEditMode() {
        editingPost = null
        postButton.text = "Create Post"
        captionInput.text.clear()
        ratingBar.rating = 0f
        selectedMedia.clear()
        mediaAdapter.notifyDataSetChanged()
        updateMediaRecyclerViewVisibility()
    }

    private fun deletePost(post: Post) {
        showDeleteConfirmationDialog(post)
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                performDeletePost(post)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performDeletePost(post: Post) {
        scope.launch {
            try {
                val response = api.deletePost("Bearer ${getToken()}", post._id!!)
                if (response.isSuccessful && response.body()?.success == true) {
                    withContext(Dispatchers.Main) {
                        allPosts.removeAll { it._id == post._id }
                        posts.removeAll { it._id == post._id }
                        applyFilter()
                        showToast("Post deleted")
                    }
                } else {
                    showToast("Failed to delete post")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private fun uploadMultipleMediaFiles(uris: List<Uri>) {
        val userId = currentUsername
        uris.forEach { uri ->
            val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            val type = if (mimeType.startsWith("video")) "video" else "image"
            val storagePath = "posts/$userId/${System.currentTimeMillis()}_${uri.lastPathSegment}"

            FirebaseMediaManager.uploadMedia(
                uri = uri,
                storagePath = storagePath,
                onSuccess = { downloadUrl ->
                    val media = Media(url = downloadUrl, type = type, storagePath = storagePath)
                    selectedMedia.add(media)
                    mediaAdapter.notifyItemInserted(selectedMedia.size - 1)
                    updateMediaRecyclerViewVisibility()
                    showToast("Media uploaded successfully")
                },
                onFailure = { exception ->
                    showToast("Error uploading media: ${exception.message}")
                }
            )
        }
    }

    private fun removeMediaAt(position: Int) {
        if (position < 0 || position >= selectedMedia.size) return

        val media = selectedMedia[position]
        FirebaseMediaManager.deleteMedia(
            storagePath = media.storagePath,
            onSuccess = {
                if (position < selectedMedia.size) {
                    selectedMedia.removeAt(position)
                    mediaAdapter.notifyItemRemoved(position)
                    if (selectedMedia.isEmpty()) {
                        mediaAdapter.notifyDataSetChanged()
                    }
                    updateMediaRecyclerViewVisibility()
                }
            },
            onFailure = { exception ->
                showToast("Error deleting media: ${exception.message}")
            }
        )
    }

    private fun updateMediaRecyclerViewVisibility() {
        if (selectedMedia.isNotEmpty()) {
            mediaRecyclerView.visibility = View.VISIBLE
        } else {
            mediaRecyclerView.visibility = View.GONE
        }
    }

    private fun updateFilterButtonColors() {
        val activeColor = ContextCompat.getColor(requireContext(), R.color.brand_primary)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.quantum_grey800)
        
        filterAllButton.setBackgroundColor(
            if (currentFilter == FilterType.ALL) activeColor else inactiveColor
        )
        filterFriendsButton.setBackgroundColor(
            if (currentFilter == FilterType.FRIENDS) activeColor else inactiveColor
        )
        filterMeButton.setBackgroundColor(
            if (currentFilter == FilterType.ME) activeColor else inactiveColor
        )
    }

    private fun applyFilter() {
        val filteredPosts = when (currentFilter) {
            FilterType.ALL -> allPosts
            FilterType.FRIENDS -> allPosts.filter { post -> 
                post.user?.username in friendsList 
            }
            FilterType.ME -> allPosts.filter { it.user?.username == currentUsername }
        }
        
        posts.clear()
        posts.addAll(filteredPosts)
        postAdapter.notifyDataSetChanged()
    }

    private fun loadFriends() {
        scope.launch {
            try {
                val response = friendRepository.getFriends("Bearer ${getToken()}")
                if (response.isSuccessful && response.body()?.friends != null) {
                    withContext(Dispatchers.Main) {
                        friendsList.clear()
                        friendsList.addAll(response.body()!!.friends.map { it.username })
                        applyFilter()
                    }
                }
            } catch (e: Exception) {
                showToast("Error loading friends: ${e.message}")
            }
        }
    }

    private fun toggleWritePostSection() {
        if (isWritePostVisible) {
            writePostCard.visibility = View.GONE
            toggleWritePostButton.text = "Write a Review!"
            isWritePostVisible = false
        } else {
            writePostCard.visibility = View.VISIBLE
            toggleWritePostButton.text = "Hide"
            isWritePostVisible = true
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
