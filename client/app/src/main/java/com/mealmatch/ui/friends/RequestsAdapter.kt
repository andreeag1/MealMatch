package com.mealmatch.ui.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mealmatch.R
import com.mealmatch.data.model.FriendRequest

class RequestsAdapter(
    private var requests: List<FriendRequest>,
    private val isIncoming: Boolean,
    private val onAccept: (String) -> Unit,
    private val onDecline: (String) -> Unit,
    private val onCancel: (String) -> Unit
) : RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

    fun updateRequests(newRequests: List<FriendRequest>) {
        this.requests = newRequests
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isIncoming) R.layout.item_request_incoming else R.layout.item_request_outgoing
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request, isIncoming, onAccept, onDecline, onCancel)
    }

    override fun getItemCount() = requests.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)

        fun bind(
            request: FriendRequest,
            isIncoming: Boolean,
            onAccept: (String) -> Unit,
            onDecline: (String) -> Unit,
            onCancel: (String) -> Unit
        ) {
            if (isIncoming) {
                usernameTextView.text = request.fromUser.username
                itemView.findViewById<Button>(R.id.acceptButton).setOnClickListener { onAccept(request._id) }
                itemView.findViewById<Button>(R.id.declineButton).setOnClickListener { onDecline(request._id) }
            } else {
                usernameTextView.text = request.toUser.username
                itemView.findViewById<Button>(R.id.cancelButton).setOnClickListener { onCancel(request._id) }
            }
        }
    }
}