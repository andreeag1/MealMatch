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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest


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

    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(
                requireContext(),        // ctx
                getString(R.string.google_maps_key)  // keep key in gradle/local.properties
            )
        }
        placesClient = Places.createClient(requireContext())
    }
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
        restaurantAdapter = RestaurantAdapter(placesClient)
        binding.rvRestaurants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restaurantAdapter
        }
        //loadDummyRestaurants()


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

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) map.isMyLocationEnabled = true
        }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
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

    private fun fetchNearbyRestaurants(center: LatLng) {
        // 1) Request the real fields, including photo metadata
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.RATING,
            Place.Field.TYPES,
            Place.Field.PHOTO_METADATAS
        )

        // 2) Define the search circle
        val circle = CircularBounds.newInstance(center, /*radiusMeters=*/1500.0)

        // 3) Build & fire the request
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedTypes(listOf("restaurant"))
            .setMaxResultCount(20)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { response ->
                val results = response.places.map { p ->
                    // Grab the first photo metadata (if any)
                    val photoMd = p.photoMetadatas?.firstOrNull()
                    val raw = p.placeTypes?.firstOrNull()?.toString() ?: "unknown"

                    val cuisine = raw
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar)
                        }
                    Restaurant(
                        name          = p.name    ?: "Unnamed",
                        cuisine       = cuisine.toString(),
                        rating        = p.rating  ?: 0.0,
                        distance      = 0.0,           // compute this later
                        latLng        = p.latLng!!,
                        photoMetadata = photoMd
                    )
                }
                updateUi(results)
            }
            .addOnFailureListener { it.printStackTrace() }
    }




    private fun updateUi(restaurants: List<Restaurant>) {
        allRestaurants = restaurants
        restaurantAdapter.submitList(restaurants)

        map.clear()
        restaurants.forEach { r ->
            map.addMarker(
                MarkerOptions()
                    .position(r.latLng)
                    .title(r.name)
            )
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()               // ask runtime permission

        val waterloo = LatLng(43.4723, -80.5449)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(waterloo, 14f))

        // run first fetch when the map is ready
        fetchNearbyRestaurants(waterloo)

        // optional: refresh when camera stops moving
        map.setOnCameraIdleListener {
            fetchNearbyRestaurants(map.cameraPosition.target)
        }
    }


//    private fun loadDummyRestaurants() {
//        allRestaurants = listOf(
//            Restaurant("The Bauer Kitchen",      "Contemporary", 4.5, 0.8, LatLng(43.4723, -80.5449)),
//            Restaurant("Vincenzo's",             "Italian Deli", 4.7, 1.2, LatLng(43.4723, -80.5449)),
//            Restaurant("Beertown Public House",  "Gastropub",    4.4, 1.5, LatLng(43.4723, -80.5449))
//        )
//        restaurantAdapter.submitList(allRestaurants)
//    }

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
