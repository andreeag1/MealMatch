package com.mealmatch.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R

class GroupChatsAdapter(private val groupChats: MutableList<GroupChat>, private val onItemClick: (GroupChat) -> Unit) : RecyclerView.Adapter<GroupChatsAdapter.ViewHolder>() {

    fun updateGroups(newGroups: List<GroupChat>) {
        groupChats.clear()
        groupChats.addAll(newGroups)
        notifyDataSetChanged()
    }

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