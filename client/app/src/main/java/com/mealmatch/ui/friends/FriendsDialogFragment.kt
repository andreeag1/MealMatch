package com.mealmatch.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.databinding.DialogFragmentFriendsBinding

class FriendsDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by activityViewModels()
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }


    override fun onResume() {
        super.onResume()
        // This ensures the friends list is fetched every time the pop-up opens.
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            viewModel.fetchFriends("Bearer $token")
        } else {
            // You could show a toast here if the token is missing for some reason
            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter(
            friends = listOf(),
            allowSelection = false,
            allowRemoval = true
        ) { friend ->
            showRemoveFriendConfirmationDialog(friend.username)
        }
        binding.recyclerViewFriendsList.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFriendsList.adapter = friendsAdapter
    }

    private fun setupClickListeners() {

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.buttonAddFriend.setOnClickListener {
            val username = binding.editTextAddFriend.text.toString().trim()
            val token = TokenManager.getToken(requireContext())

            if (username.isNotEmpty() && token != null) {
                // We just call the viewModel function. The update will happen
                // when the friends list is re-fetched.
                viewModel.sendFriendRequest("Bearer $token", username)
                binding.editTextAddFriend.text?.clear()
                Toast.makeText(context, "Sending request...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.recyclerViewFriendsList.visibility = View.INVISIBLE
                }
                is ApiResult.Success -> {
                    binding.recyclerViewFriendsList.visibility = View.VISIBLE
                    friendsAdapter.updateFriends(result.data)
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRemoveFriendConfirmationDialog(username: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove $username from your friends?")
            .setPositiveButton("Remove") { _, _ ->
                val token = TokenManager.getToken(requireContext()) ?: return@setPositiveButton
                viewModel.removeFriend("Bearer $token", username)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
