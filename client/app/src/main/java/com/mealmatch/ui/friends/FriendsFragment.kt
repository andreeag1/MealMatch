package com.mealmatch.ui.friends

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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.mealmatch.R

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
    private val initialGroupChats = mutableListOf<GroupChat>()

    // Adapters for the RecyclerViews
    private lateinit var selectableFriendsAdapter: SelectableFriendsAdapter
    private lateinit var groupChatsAdapter: GroupChatsAdapter
    private lateinit var friendsListAdapter: FriendsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews(view)
        setupClickListeners(view)
    }

    private fun setupRecyclerViews(view: View) {
        // View for showing friends when creating a group
        val rvSelectableFriends = view.findViewById<RecyclerView>(R.id.recyclerViewAddFriends)
        selectableFriendsAdapter = SelectableFriendsAdapter(initialFriends)
        rvSelectableFriends.layoutManager = LinearLayoutManager(context)
        rvSelectableFriends.adapter = selectableFriendsAdapter

        // View that shows current groups
        val rvGroupChats = view.findViewById<RecyclerView>(R.id.recyclerViewGroupChats)
        groupChatsAdapter = GroupChatsAdapter(initialGroupChats) { groupChat ->
            // TODO: handle group click
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
            val authToken = "Bearer YOUR_JWT_TOKEN_HERE"

            // Tell the ViewModel to create the group
            viewModel.createGroup(authToken, groupName, memberUsernames)
            groupChatsAdapter.notifyItemInserted(initialGroupChats.size - 1)

            groupNameEditText.text = null
            selectableFriendsAdapter.clearSelection()

            Toast.makeText(context, "Group '$groupName' created!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class SelectableFriendsAdapter(private val friends: List<Friend>) : RecyclerView.Adapter<SelectableFriendsAdapter.ViewHolder>() {

    private val selectedFriendIds = mutableSetOf<String>()

    fun getSelectedFriends(): List<Friend> {
        return friends.filter { it.id in selectedFriendIds }
    }

    fun clearSelection() {
        selectedFriendIds.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_selectable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        holder.bind(friend, selectedFriendIds.contains(friend.id))
        holder.itemView.setOnClickListener {
            if (selectedFriendIds.contains(friend.id)) {
                selectedFriendIds.remove(friend.id)
            } else {
                selectedFriendIds.add(friend.id)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = friends.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.friendAvatar)
        private val name: TextView = itemView.findViewById(R.id.friendName)
        private val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)

        fun bind(friend: Friend, isSelected: Boolean) {
            name.text = friend.name
            avatar.setImageResource(R.drawable.friends)
            checkIcon.isVisible = isSelected
            itemView.isActivated = isSelected
        }
    }
}

class GroupChatsAdapter(private val groupChats: List<GroupChat>, private val onItemClick: (GroupChat) -> Unit) : RecyclerView.Adapter<GroupChatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = groupChats[position]
        holder.bind(chat)
        holder.itemView.setOnClickListener { onItemClick(chat) }
    }

    override fun getItemCount() = groupChats.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.groupName)
        private val memberCount: TextView = itemView.findViewById(R.id.memberCount)

        fun bind(chat: GroupChat) {
            groupName.text = chat.name
            memberCount.text = "${chat.members.size} members"
        }
    }
}

class FriendsListAdapter(private val friends: List<Friend>) : RecyclerView.Adapter<FriendsListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_selectable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount() = friends.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.friendAvatar)
        private val name: TextView = itemView.findViewById(R.id.friendName)

        fun bind(friend: Friend) {
            name.text = friend.name
            avatar.setImageResource(R.drawable.friends)
        }
    }
}
