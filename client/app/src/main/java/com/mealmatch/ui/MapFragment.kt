package com.mealmatch.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.recyclerview.widget.LinearLayoutManager
import android.location.Geocoder
import androidx.lifecycle.lifecycleScope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mealmatch.R
import com.mealmatch.databinding.FragmentMapBinding
import com.mealmatch.data.model.Restaurant
import com.mealmatch.ui.map.RestaurantAdapter

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var bottomSheet: BottomSheetBehavior<View>
    private lateinit var restaurantAdapter: RestaurantAdapter

    // Keep full list for resetting when query is empty
    private var allRestaurants = listOf<Restaurant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Ensure SupportMapFragment is present
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


        binding.searchView.apply {
            // never collapse back to icon
            setIconifiedByDefault(false)
            setIconified(false)
            onActionViewExpanded()
        }
        // tapping anywhere expands & shows keyboard
        binding.searchView.setOnClickListener {
            binding.searchView.apply {
                if (isIconified) {
                    isIconified = false
                    onActionViewExpanded()
                }
                requestFocus()
            }
            val imm = requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }
        // keep it expanded when focused
        binding.searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.searchView.isIconified = false
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                query?.takeIf { it.isNotBlank() }?.let { searchMapLocation(it) }
                return true
            }
            override fun onQueryTextChange(newText: String?) = true
        })
    }

    private fun searchMapLocation(query: String) {
        // offload geocoding to a background thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext())
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    val target = LatLng(addr.latitude, addr.longitude)

                    withContext(Dispatchers.Main) {
                        // clear old markers (optional)
                        map.clear()
                        // drop a new pin
                        map.addMarker(
                            MarkerOptions()
                                .position(target)
                                .title(query)
                        )
                        // zoom in
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(target, 13f)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // you might show a toast on failure
            }
        }
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
        allRestaurants = listOf(
            Restaurant("The Bauer Kitchen",      "Contemporary", 4.5, 0.8),
            Restaurant("Vincenzo's",             "Italian Deli", 4.7, 1.2),
            Restaurant("Beertown Public House",  "Gastropub",    4.4, 1.5)
        )
        restaurantAdapter.submitList(allRestaurants)
    }

    private fun filterRestaurants(query: String?) {
        val text = query.orEmpty().trim()
        val filtered = if (text.isEmpty()) {
            allRestaurants
        } else {
            allRestaurants.filter {
                it.name.contains(text, ignoreCase = true)
            }
        }
        restaurantAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
