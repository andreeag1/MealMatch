package com.mealmatch.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mealmatch.data.local.TokenManager
import com.mealmatch.databinding.FragmentFriendsTabBinding

class FriendsTabFragment : Fragment() {

    private var _binding: FragmentFriendsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by activityViewModels()
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        friendsAdapter = FriendsAdapter(listOf(), allowSelection = false, allowRemoval = true) { friend ->
            showRemoveFriendConfirmationDialog(friend.username)
        }
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.friendsRecyclerView.adapter = friendsAdapter
    }

    private fun setupClickListeners() {
        binding.buttonAddFriend.setOnClickListener {
            val username = binding.editTextAddFriend.text.toString().trim()
            val token = TokenManager.getToken(requireContext())

            if (username.isNotEmpty() && token != null) {
                viewModel.sendFriendRequest("Bearer $token", username)
                binding.editTextAddFriend.text?.clear()
                Toast.makeText(context, "Sending request...", Toast.LENGTH_SHORT).show()
            } else if (username.isEmpty()){
                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.friends.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                }
                is ApiResult.Success -> {
                    friendsAdapter.updateFriends(result.data)
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRemoveFriendConfirmationDialog(username: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove $username from your friends?")
            .setPositiveButton("Remove") { _, _ ->
                val token = TokenManager.getToken(requireContext())
                if (token != null) {
                    viewModel.removeFriend("Bearer $token", username)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}