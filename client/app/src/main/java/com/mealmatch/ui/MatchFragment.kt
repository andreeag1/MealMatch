package com.mealmatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mealmatch.databinding.FragmentMatchBinding

class MatchFragment : Fragment() {
    private var _binding: FragmentMatchBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.matchTextView.text = "Welcome to the Match Page!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}