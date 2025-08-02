package com.mealmatch.ui.match

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mealmatch.data.local.TokenManager
import com.mealmatch.databinding.FragmentSoloMatchBinding
import com.mealmatch.ui.match.MatchActivity
import com.mealmatch.ui.friends.ApiResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.location.Location

class SoloMatchFragment : Fragment() {

    private var _binding: FragmentSoloMatchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSoloMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocation()

        binding.buttonExploreSolo.setOnClickListener {
            val token = TokenManager.getToken(requireContext())
            if (token == null) {
                Toast.makeText(context, "Authentication Error", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.startNewSession("Bearer $token", null)
        }

        observeViewModel()
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                }
            }
        } else {
            // error handling needed? or do we prompt for different location
        }
    }

    private fun observeViewModel() {
        viewModel.sessionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(context, "Starting session...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    val session = result.data
                    Toast.makeText(context, "Session started!", Toast.LENGTH_SHORT).show()
                    // Pass location to MatchActivity via intent
                    val intent = Intent(requireContext(), MatchActivity::class.java).apply {
                        putExtra("SESSION_ID", session._id)
                        userLocation?.let {
                            putExtra("USER_LAT", it.latitude)
                            putExtra("USER_LNG", it.longitude)
                        }
                    }
                    startActivity(intent)
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error starting session: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}