package com.mealmatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mealmatch.R
import com.mealmatch.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var bottomSheet: BottomSheetBehavior<View>
    private lateinit var restaurantAdapter: RestaurantAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Dynamically ensure a SupportMapFragment is present
        var mapFrag = childFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance()
            childFragmentManager.commitNow {
                replace(R.id.google_map, mapFrag)
            }
        }
        mapFrag.getMapAsync(this)

        // 2. Bottom sheet setup
        bottomSheet = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED

        // 3. RecyclerView + adapter
        restaurantAdapter = RestaurantAdapter()
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restaurantAdapter
        }
        loadDummyRestaurants()

        // 4. SearchView filtering
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                query?.let { restaurantAdapter.filterByName(it) }
            }
            override fun onQueryTextChange(newText: String?) = true.also {
                newText?.let { restaurantAdapter.filterByName(it) }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val defaultLocation = LatLng(43.4723, -80.5449) // Waterloo, ON
        map.addMarker(
            MarkerOptions()
                .position(defaultLocation)
                .title("Waterloo, ON")
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f))
    }

    private fun loadDummyRestaurants() {
        val sample = listOf(
            Restaurant("The Bauer Kitchen", "Contemporary", 4.5, 0.8),
            Restaurant("Vincenzo's", "Italian Deli", 4.7, 1.2),
            Restaurant("Beertown Public House", "Gastropub", 4.4, 1.5)
        )
        restaurantAdapter.submitList(sample)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
