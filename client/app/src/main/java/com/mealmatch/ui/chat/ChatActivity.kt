package com.mealmatch.ui.chat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.auth0.android.jwt.JWT
import com.mealmatch.data.local.TokenManager
import com.mealmatch.data.model.MessageResponse
import com.mealmatch.data.model.UserInfo
import com.mealmatch.databinding.ActivityChatBinding
import com.mealmatch.ui.match.MatchActivity
import com.mealmatch.ui.friends.ApiResult

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: MessageAdapter
    private var groupId: String? = null
    private var currentUserId: String? = null

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"

        fun newIntent(context: Context, groupId: String, groupName: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_GROUP_ID, groupId)
                putExtra(EXTRA_GROUP_NAME, groupName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupId = intent.getStringExtra(EXTRA_GROUP_ID)
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME)
        val token = TokenManager.getToken(this)

        if (groupId == null || token == null) {
            Toast.makeText(this, "Error: Group or User not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Decode the JWT to get the current user's ID
        val jwt = JWT(token)
        currentUserId = jwt.getClaim("id").asString()

        setupToolbar(groupName)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.fetchMessages("Bearer $token", groupId!!)
    }

    override fun onStart() {
        super.onStart()
        val token = TokenManager.getToken(this)
        if (token != null && groupId != null) {
            viewModel.connectWebSocket(token, groupId!!)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnectWebSocket()
    }

    private fun setupToolbar(groupName: String?) {
        binding.toolbar.title = groupName ?: "Chat"
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(currentUserId!!)
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonSend.setOnClickListener {
            val messageContent = binding.editTextMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                viewModel.sendMessage(groupId!!, messageContent)
                binding.editTextMessage.text.clear()
            }
        }

        binding.buttonStartMatch.setOnClickListener {
            val token = TokenManager.getToken(this)
            if (token != null && groupId != null) {
                viewModel.startNewMatchSession("Bearer $token", groupId!!)
            }
        }

        binding.buttonLeaderboard.setOnClickListener {
            // TODO: Handle leaderboard click
            Toast.makeText(this, "Leaderboard clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { result ->
            when(result) {
                is ApiResult.Loading -> { /* Show progress bar */ }
                is ApiResult.Success -> {
                    messageAdapter.setMessages(result.data)
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error fetching messages: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.createSessionResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(this, "Starting new session...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    val session = result.data
                    Toast.makeText(this, "New session started!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this, MatchActivity::class.java).apply {
                        putExtra("SESSION_ID", session._id)
                        putExtra("GROUP_ID", session.group)
                    }
                    startActivity(intent)
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.newMessage.observe(this) { message ->
            // Convert WebSocket message to MessageResponse
            val messageResponse = MessageResponse(
                _id = "",
                group = groupId!!,
                user = UserInfo(_id = message.userId, username = message.username),
                content = message.content,
                createdAt = message.createdAt
            )
            messageAdapter.addMessage(messageResponse)
            binding.recyclerViewMessages.scrollToPosition(messageAdapter.itemCount - 1)
        }
    }
}