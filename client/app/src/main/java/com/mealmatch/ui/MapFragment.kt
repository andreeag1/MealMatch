package com.mealmatch.ui

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.mealmatch.R
import com.mealmatch.data.model.Restaurant
import com.mealmatch.databinding.FragmentMapBinding
import com.mealmatch.ui.map.RestaurantAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var restaurantAdapter: RestaurantAdapter

    // ← NEW: tabs and the two SearchViews
    private lateinit var tabLayout: TabLayout
    private lateinit var svQuery: SearchView
    private lateinit var svAddress: SearchView

    private lateinit var rvFull: RecyclerView
    private lateinit var rvMapSheet: RecyclerView

    private lateinit var placesClient: PlacesClient

    // Keep full list for resetting when query is empty
    private var allRestaurants = listOf<Restaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(
                requireContext(),
                getString(R.string.google_maps_key)
            )
        }
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ─── 1) Bind views ──────────────────────────────────────────────
        tabLayout    = binding.tabLayout
        svQuery      = binding.svQuery
        svAddress    = binding.svAddress
        rvFull       = binding.rvRestaurantsFull
        rvMapSheet   = binding.rvMapRestaurants

        // ─── 2) Set up GoogleMap fragment ──────────────────────────────
        var mapFrag = childFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance()
            childFragmentManager.commitNow {
                replace(R.id.google_map, mapFrag)
            }
        }
        mapFrag.getMapAsync(this)

        // ─── 3) Bottom‐sheet setup (hidden by default) ─────────────────
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        // ─── 4) RecyclerViews + adapter ────────────────────────────────
        restaurantAdapter = RestaurantAdapter(placesClient)

        rvFull.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = restaurantAdapter
        }
        rvMapSheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = restaurantAdapter
        }

        // Kick off your first load if you want (or wait for the Map callback)
        // fetchNearbyRestaurants(...)

        // ─── 5) TabLayout: Restaurants vs Map ──────────────────────────
        tabLayout.apply {
            addTab(newTab().setText("Restaurants"))
            addTab(newTab().setText("Map"))
            selectTab(getTabAt(0))  // start on Restaurants

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 0) {
                        // RESTAURANTS tab
                        rvFull.visibility           = View.VISIBLE
                        binding.googleMap.visibility = View.GONE
                        bottomSheetBehavior.state    = BottomSheetBehavior.STATE_HIDDEN
                    } else {
                        // MAP tab
                        rvFull.visibility           = View.GONE
                        binding.googleMap.visibility = View.VISIBLE
                        bottomSheetBehavior.state    = BottomSheetBehavior.STATE_HIDDEN
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }

        // ─── 6) Search by name / cuisine ────────────────────────────────
        svQuery.apply {
            setIconifiedByDefault(false)
            isIconified = false
            queryHint   = "Search restaurant, cuisine…"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String?) = true
                override fun onQueryTextChange(q: String?): Boolean {
                    filterRestaurants(q)
                    return true
                }
            })
        }

        // ─── 7) Search by address ───────────────────────────────────────
        svAddress.apply {
            setIconifiedByDefault(false)
            isIconified = false
            queryHint   = "Current location"
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(address: String?): Boolean {
                    clearFocus()
                    address?.takeIf { it.isNotBlank() }?.let { searchMapLocation(it) }
                    return true
                }
                override fun onQueryTextChange(s: String?) = true
            })
        }
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
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext())
                val results = geocoder.getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val addr = results[0]
                    val target = LatLng(addr.latitude, addr.longitude)
                    withContext(Dispatchers.Main) {
                        map.clear()
                        map.addMarker(MarkerOptions().position(target).title(query))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 13f))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchNearbyRestaurants(center: LatLng) {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.RATING,
            Place.Field.TYPES,
            Place.Field.PHOTO_METADATAS
        )
        val circle = CircularBounds.newInstance(center, 1500.0)
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setExcludedPrimaryTypes(listOf("lodging"))
            .setMaxResultCount(20)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { resp ->
                val results = resp.places.map { p ->
                    val photoMd = p.photoMetadatas?.firstOrNull()
                    val raw = p.placeTypes?.firstOrNull()?.toString() ?: "unknown"
                    val cuisine = raw
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
                    Restaurant(
                        name          = p.name ?: "Unnamed",
                        cuisine       = cuisine,
                        rating        = p.rating ?: 0.0,
                        distance      = 0.0,
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
            map.addMarker(MarkerOptions().position(r.latLng).title(r.name))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        val waterloo = LatLng(43.4723, -80.5449)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(waterloo, 14f))

        // FIRST fetch + show list
        fetchNearbyRestaurants(waterloo)
        showList()

        map.setOnCameraIdleListener {
            fetchNearbyRestaurants(map.cameraPosition.target)
        }
    }

    private fun showList() {
        // hide the map container
        binding.googleMap.visibility = View.GONE
        // expand the sheet so it fills from under the AppBar down
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showMap() {
        // show the map full-screen
        binding.googleMap.visibility = View.VISIBLE
        // hide the sheet off-screen
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }


    private fun filterRestaurants(query: String?) {
        val text = query.orEmpty().trim()
        val filtered = if (text.isEmpty()) allRestaurants
        else allRestaurants.filter {
            it.name.contains(text, ignoreCase = true)
        }
        restaurantAdapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
