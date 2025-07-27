package com.mealmatch.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mealmatch.data.local.TokenManager
import com.mealmatch.databinding.FragmentRequestsListBinding

class RequestsListFragment : Fragment() {

    private var _binding: FragmentRequestsListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by activityViewModels()
    private var isIncoming: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isIncoming = it.getBoolean(ARG_IS_INCOMING)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.requestsRecyclerView.layoutManager = LinearLayoutManager(context)
        val requestsAdapter = RequestsAdapter(listOf(), isIncoming,
            onAccept = { requestId -> handleAccept(requestId) },
            onDecline = { requestId -> handleDecline(requestId) },
            onCancel = { requestId -> handleCancel(requestId) }
        )
        binding.requestsRecyclerView.adapter = requestsAdapter
    }

    private fun observeViewModel() {
        val liveData = if (isIncoming) viewModel.incomingRequests else viewModel.outgoingRequests
        liveData.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {}
                is ApiResult.Success -> {
                    (binding.requestsRecyclerView.adapter as? RequestsAdapter)?.updateRequests(result.data)
                    if (result.data.isEmpty()) {
                        binding.requestsRecyclerView.visibility = View.GONE
                        binding.emptyStateTextView.visibility = View.VISIBLE
                        binding.emptyStateTextView.text = if (isIncoming) "No incoming requests" else "No outgoing requests"
                    } else {
                        binding.requestsRecyclerView.visibility = View.VISIBLE
                        binding.emptyStateTextView.visibility = View.GONE
                    }
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getToken(): String? {
        val token = TokenManager.getToken(requireContext())
        if (token == null) Toast.makeText(context, "Authentication Error", Toast.LENGTH_SHORT).show()
        return token
    }

    private fun handleAccept(requestId: String) {
        getToken()?.let { viewModel.acceptFriendRequest("Bearer $it", requestId) }
    }

    private fun handleDecline(requestId: String) {
        getToken()?.let { viewModel.declineFriendRequest("Bearer $it", requestId) }
    }

    private fun handleCancel(requestId: String) {
        getToken()?.let { viewModel.declineFriendRequest("Bearer $it", requestId) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_IS_INCOMING = "is_incoming"
        @JvmStatic
        fun newInstance(isIncoming: Boolean) =
            RequestsListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_INCOMING, isIncoming)
                }
            }
    }
}