package com.mealmatch.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.FriendModel as Friend

class FriendsAdapter(
    private var friends: List<Friend>,
    private val allowSelection: Boolean = false,
    private val allowRemoval: Boolean = false,
    private val onRemoveClick: ((Friend) -> Unit)? = null
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private val selectedFriendIds = mutableSetOf<String>()

    fun getSelectedFriends(): List<Friend> =
        friends.filter { it._id in selectedFriendIds }

    fun clearSelection() {
        selectedFriendIds.clear()
        notifyDataSetChanged()
    }

    fun updateFriends(newFriends: List<Friend>) {
        friends = newFriends
        selectedFriendIds.retainAll(newFriends.map { it._id })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val friend = friends[position]
        val isSelected = selectedFriendIds.contains(friend._id)
        holder.bind(friend, isSelected, allowSelection, allowRemoval, onRemoveClick)
        holder.itemView.setOnClickListener {
            if (allowSelection) {
                if (isSelected) selectedFriendIds.remove(friend._id)
                else selectedFriendIds.add(friend._id)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = friends.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: ImageView = itemView.findViewById(R.id.friendAvatar)
        private val name: TextView = itemView.findViewById(R.id.friendName)
        private val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)
        private val removeIcon: ImageView = itemView.findViewById(R.id.removeIcon)

        fun bind(
            friend: Friend,
            isSelected: Boolean,
            allowSelection: Boolean,
            allowRemoval: Boolean,
            onRemoveClick: ((Friend) -> Unit)?
        ) {
            name.text = friend.username
            avatar.setImageResource(R.drawable.friends)
            checkIcon.isVisible = allowSelection && isSelected
            removeIcon.isVisible = allowRemoval

            removeIcon.setOnClickListener {
                onRemoveClick?.invoke(friend)
            }
        }
    }
}
