package com.mealmatch.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R

class FriendsAdapters(private val friends: List<Friend>) : RecyclerView.Adapter<FriendsAdapters.ViewHolder>() {

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