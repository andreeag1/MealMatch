package com.mealmatch.ui.friends

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import com.mealmatch.databinding.FragmentFriendsBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import android.widget.Toast
import android.widget.Button
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.ui.chat.ChatActivity
import com.mealmatch.data.model.FriendModel as Friend


class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by activityViewModels()

    private lateinit var groupChatsAdapter: GroupChatsAdapter
    private lateinit var groupCreationFriendsAdapter: FriendsAdapter
//    private lateinit var friendsListAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Add this entire function to your FriendsFragment.kt file
    override fun onResume() {
        super.onResume()
        // This will be called every time the fragment becomes active,
        // including when you close the "Add Friends Pop-up".
        // It ensures your friend and group lists are always up-to-date.
        fetchFriendsAndGroups()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(view)
        setupClickListeners(view)
        observeViewModel()
        fetchFriendsAndGroups()
    }

    private fun fetchFriendsAndGroups() {
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            val authToken = "Bearer $token"
            viewModel.getUserGroups(authToken)
            viewModel.fetchFriends(authToken)
        } else {
            Toast.makeText(context, "Not authenticated. Please log in.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerViews(view: View) {
        val rvSelectableFriends = view.findViewById<RecyclerView>(R.id.recyclerViewAddFriends)
        groupCreationFriendsAdapter = FriendsAdapter(
            friends = listOf(),
            allowSelection = true,
            allowRemoval = false
        )
        rvSelectableFriends.layoutManager = LinearLayoutManager(context)
        rvSelectableFriends.adapter = groupCreationFriendsAdapter

        val rvGroupChats = view.findViewById<RecyclerView>(R.id.recyclerViewGroupChats)
        groupChatsAdapter = GroupChatsAdapter(mutableListOf()) { groupChat ->
            val intent = ChatActivity.newIntent(requireContext(), groupChat.id, groupChat.name)
            startActivity(intent)
        }
        rvGroupChats.layoutManager = LinearLayoutManager(context)
        rvGroupChats.adapter = groupChatsAdapter

//        val rvFriendsList = view.findViewById<RecyclerView>(R.id.recyclerViewFriendsList)
//        friendsListAdapter = FriendsAdapter(
//            friends = listOf(),
//            allowSelection = false,
//            allowRemoval = true
//        ) { friend ->
//            val token = TokenManager.getToken(requireContext()) ?: return@FriendsAdapter
//            viewModel.removeFriend("Bearer $token", friend.username)
//        }
//        rvFriendsList.layoutManager = LinearLayoutManager(context)
//        rvFriendsList.adapter = friendsListAdapter
    }

    private fun setupClickListeners(view: View) {
        val groupNameEditText = view.findViewById<TextInputEditText>(R.id.editTextGroupName)
        val createGroupButton = view.findViewById<Button>(R.id.buttonCreateGroup)

        binding.buttonManageFriends.setOnClickListener {
            FriendsDialogFragment().show(childFragmentManager, "FriendsDialogFragment")
        }

        createGroupButton.setOnClickListener {
            val groupName = groupNameEditText.text.toString().trim()
            val selectedFriends = groupCreationFriendsAdapter.getSelectedFriends()

            if (groupName.isEmpty()) {
                groupNameEditText.error = "Group name cannot be empty"
                return@setOnClickListener
            }
            if (selectedFriends.isEmpty()) {
                Toast.makeText(context, "Please select at least one friend", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val memberUsernames = selectedFriends.map { it.username }
            val token = TokenManager.getToken(requireContext())

            if (token == null) {
                Toast.makeText(context, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val authToken = "Bearer $token"
            viewModel.createGroup(authToken, groupName, memberUsernames)
        }

//        val addFriendButton = view.findViewById<Button>(R.id.buttonAddFriend)
//        val friendUsernameInput = view.findViewById<TextInputEditText>(R.id.editTextAddFriend)
//
//        addFriendButton.setOnClickListener {
//            val username = friendUsernameInput.text.toString().trim()
//            val token = TokenManager.getToken(requireContext())
//
//            if (username.isNotEmpty() && token != null) {
//                viewModel.addFriend("Bearer $token", username)
//                friendUsernameInput.text?.clear()
//                Toast.makeText(context, "Adding $username...", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Please enter a username", Toast.LENGTH_SHORT).show()
//            }
//        }

    }

    private fun observeViewModel() {
        viewModel.createGroupResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> Toast.makeText(context, "Creating group...", Toast.LENGTH_SHORT).show()
                is ApiResult.Success -> {
                    Toast.makeText(context, "Group created successfully!", Toast.LENGTH_LONG).show()
                    binding.editTextGroupName.text = null
                    groupCreationFriendsAdapter.clearSelection()
                    fetchFriendsAndGroups()
                }
                is ApiResult.Error -> Toast.makeText(context, "Error: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.userGroupsResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {}
                is ApiResult.Success -> {
                    val groupChatsForUi = result.data.map { groupResponse ->
                        val membersForUi = groupResponse.members.map { member ->
                            Friend(_id = member._id, username = member.username, email = null)
                        }
                        GroupChat(id = groupResponse._id, name = groupResponse.name, members = membersForUi)
                    }
                    groupChatsAdapter.updateGroups(groupChatsForUi)
                }
                is ApiResult.Error -> Toast.makeText(context, "Error fetching groups: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.friends.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {}
                is ApiResult.Success -> {
//                    friendsListAdapter.updateFriends(result.data)
                    groupCreationFriendsAdapter.updateFriends(result.data)
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Failed to fetch friends: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class GroupChat(val id: String, val name: String, val members: List<Friend>)

