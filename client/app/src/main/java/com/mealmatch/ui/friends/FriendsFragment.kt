package com.mealmatch.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mealmatch.databinding.FragmentFriendsBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import android.widget.Button
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.ui.chat.ChatActivity

data class Friend(val id: String, val name: String, val avatarUrl: String)
data class GroupChat(val id: String, val name: String, val members: List<Friend>)


class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by viewModels()

    private val initialFriends = listOf(
        Friend("1", "Bob", "https://placehold.co/80x80/FFD700/FFFFFF?text=AP"),
        Friend("2", "Rob", "https://placehold.co/80x80/ADD8E6/000080?text=JL"),
        Friend("3", "Dude", "https://placehold.co/80x80/90EE90/006400?text=CK")
    )

    // Adapters for the RecyclerViews
    private lateinit var selectableFriendsAdapter: FriendsAdapters
    private lateinit var groupChatsAdapter: GroupChatsAdapter
    private lateinit var friendsListAdapter: FriendsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(view)
        setupClickListeners(view)
        observeViewModel()
        fetchGroups()
    }

    private fun fetchGroups() {
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            viewModel.getUserGroups("Bearer $token")
        } else {
            Toast.makeText(context, "Not authenticated. Please log in.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerViews(view: View) {
        // View for showing friends when creating a group
        val rvSelectableFriends = view.findViewById<RecyclerView>(R.id.recyclerViewAddFriends)
        selectableFriendsAdapter = FriendsAdapters(initialFriends)
        rvSelectableFriends.layoutManager = LinearLayoutManager(context)
        rvSelectableFriends.adapter = selectableFriendsAdapter

        // View that shows current groups
        val rvGroupChats = view.findViewById<RecyclerView>(R.id.recyclerViewGroupChats)
        groupChatsAdapter = GroupChatsAdapter(mutableListOf()) { groupChat ->
            val intent = ChatActivity.newIntent(requireContext(), groupChat.id, groupChat.name)
            startActivity(intent)
        }
        rvGroupChats.layoutManager = LinearLayoutManager(context)
        rvGroupChats.adapter = groupChatsAdapter

        // View that shows friend list
        val rvFriendsList = view.findViewById<RecyclerView>(R.id.recyclerViewFriendsList)
        friendsListAdapter = FriendsListAdapter(initialFriends)
        rvFriendsList.layoutManager = LinearLayoutManager(context)
        rvFriendsList.adapter = friendsListAdapter
    }

    private fun setupClickListeners(view: View) {
        val groupNameEditText = view.findViewById<TextInputEditText>(R.id.editTextGroupName)
        val createGroupButton = view.findViewById<Button>(R.id.buttonCreateGroup)

        createGroupButton.setOnClickListener {
            val groupName = groupNameEditText.text.toString().trim()
            val selectedFriends = selectableFriendsAdapter.getSelectedFriends()

            if (groupName.isEmpty()) {
                groupNameEditText.error = "Group name cannot be empty"
                return@setOnClickListener
            }
            if (selectedFriends.isEmpty()) {
                Toast.makeText(context, "Please select at least one friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val memberUsernames = selectedFriends.map { it.name }
            val token = TokenManager.getToken(requireContext())

            if (token == null) {
                Toast.makeText(context, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val authToken = "Bearer $token"

            viewModel.createGroup(authToken, groupName, memberUsernames)
        }
    }

    private fun observeViewModel() {
        // Observer for the create group action
        viewModel.createGroupResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> Toast.makeText(context, "Creating group...", Toast.LENGTH_SHORT).show()
                is ApiResult.Success -> {
                    Toast.makeText(context, "Group created successfully!", Toast.LENGTH_LONG).show()
                    binding.editTextGroupName.text = null
                    selectableFriendsAdapter.clearSelection()
                    fetchGroups()
                }
                is ApiResult.Error -> Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observer for the list of user's groups
        viewModel.userGroupsResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(context, "Creating group...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    // Convert the backend response model to the UI model
                    val groupChatsForUi = result.data.map { groupResponse ->
                        val membersForUi = groupResponse.members.map { member ->
                            Friend(id = member._id, name = member.username, avatarUrl = "")
                        }
                        GroupChat(id = groupResponse._id, name = groupResponse.name, members = membersForUi)
                    }
                    // Update the adapter with the new list
                    groupChatsAdapter.updateGroups(groupChatsForUi)
                }
                is ApiResult.Error -> Toast.makeText(context, "Error fetching groups: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
