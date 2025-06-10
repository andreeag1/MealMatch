package com.mealmatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mealmatch.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {
    private var _binding: FragmentFriendsBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.friendsTextView.text = "Welcome to the Friends Page!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}