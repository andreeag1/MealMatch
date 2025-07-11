package com.mealmatch.ui


import com.mealmatch.BuildConfig
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.RatingBar
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.Manifest
import android.content.pm.PackageManager
import android.widget.LinearLayout

import com.mealmatch.R
import com.mealmatch.data.model.Restaurant
import com.mealmatch.databinding.FragmentMapBinding
import com.mealmatch.ui.map.RestaurantAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var restaurantAdapter: RestaurantAdapter
    val mapsApiKey = BuildConfig.MAPS_API_KEY

    private lateinit var tabLayout: TabLayout
    private lateinit var svQuery: SearchView
    private lateinit var svAddress: SearchView

    private lateinit var rvFull: RecyclerView
    private lateinit var rvMapSheet: RecyclerView
    private lateinit var placesClient: PlacesClient

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    // Toggle to allow/disallow fetching on camera move
    private var fetchOnCameraMoveEnabled = false

    // Keep full list for resetting when query is empty
    private var allRestaurants = listOf<Restaurant>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), mapsApiKey)
        }
        placesClient        = Places.createClient(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout    = binding.tabLayout
        svQuery      = binding.svQuery
        svAddress    = binding.svAddress
        rvFull       = binding.rvRestaurantsFull
        rvMapSheet   = binding.rvMapRestaurants

        // Setup map fragment
        var mapFrag = childFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance()
            childFragmentManager.commitNow { replace(R.id.google_map, mapFrag) }
        }
        mapFrag.getMapAsync(this)

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        restaurantAdapter = RestaurantAdapter(placesClient)

        rvFull.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = restaurantAdapter
        }
        rvMapSheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = restaurantAdapter
        }

        // Tabs to switch views
        tabLayout.apply {
            addTab(newTab().setText("Restaurants"))
            addTab(newTab().setText("Map"))
            selectTab(getTabAt(0))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 0) showList() else showMap()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }

        // Search filter
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

        // Address search triggers a fetch at that location
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun searchMapLocation(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = Geocoder(requireContext()).getFromLocationName(query, 1)
                if (!results.isNullOrEmpty()) {
                    val addr   = results[0]
                    val target = LatLng(addr.latitude, addr.longitude)
                    withContext(Dispatchers.Main) {
                        map.clear()
                        map.addMarker(MarkerOptions().position(target).title(query))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(target, 13f))

                        // Fetch once for this search
                        currentLocation?.let { loc ->
                            fetchNearbyRestaurants(target, loc)
                        }
                        showList()
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchNearbyRestaurants(center: LatLng, origin: Location) {
        val placeFields = listOf(
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.RATING,
            Place.Field.TYPES,
            Place.Field.PHOTO_METADATAS
        )
        val circle  = CircularBounds.newInstance(center, 1500.0)
        val request = SearchNearbyRequest.builder(circle, placeFields)
            .setIncludedPrimaryTypes(listOf("restaurant"))
            .setExcludedPrimaryTypes(listOf("lodging"))
            .setMaxResultCount(20)
            .build()

        placesClient.searchNearby(request)
            .addOnSuccessListener { resp ->
                val results = resp.places.map { p ->
                    val rawType = p.placeTypes?.firstOrNull()?.toString().orEmpty()
                    val cuisine = rawType
                        .replace("_", " ")
                        .split(" ")
                        .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }

                    val rLoc = Location("").apply {
                        latitude  = p.latLng!!.latitude
                        longitude = p.latLng!!.longitude
                    }
                    val distKm = origin.distanceTo(rLoc) / 1000.0

                    Restaurant(
                        name          = p.name ?: "Unnamed",
                        cuisine       = cuisine,
                        rating        = p.rating ?: 0.0,
                        distance      = distKm,
                        latLng        = p.latLng!!,
                        photoMetadata = p.photoMetadatas?.firstOrNull()
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
            map.addMarker(MarkerOptions().position(r.latLng).title(r.name))?.tag = r
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        // Grab the device's last location once
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    val currLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currLatLng, 14f))
                    fetchNearbyRestaurants(currLatLng, location)
                    showList()
                }
            }

        // Always listen but only fetch if enabled
        map.setOnCameraIdleListener {
            if (fetchOnCameraMoveEnabled) {
                currentLocation?.let { loc ->
                    fetchNearbyRestaurants(map.cameraPosition.target, loc)
                }
            }
        }

        map.setOnMarkerClickListener { marker ->
            (marker.tag as? Restaurant)?.let { showDetailSheet(it) }
            true
        }
    }

    // Call this from a demo toggle or button
    fun toggleFreeExploreMode(enabled: Boolean) {
        fetchOnCameraMoveEnabled = enabled
    }

    private fun showList() {
        binding.googleMap.visibility = View.GONE
        bottomSheetBehavior.state   = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showMap() {
        binding.googleMap.visibility = View.VISIBLE
        bottomSheetBehavior.state   = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun filterRestaurants(query: String?) {
        val text     = query.orEmpty().trim()
        val filtered = if (text.isEmpty()) allRestaurants
        else allRestaurants.filter { it.name.contains(text, ignoreCase = true) }
        restaurantAdapter.submitList(filtered)
    }

    private fun showDetailSheet(r: Restaurant) {
        val dialog    = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.restaurant_details, null)

        val ivPhoto    = sheetView.findViewById<ImageView>(R.id.ivPhoto)
        val tvName     = sheetView.findViewById<TextView>(R.id.tvName)
        val rbRating   = sheetView.findViewById<RatingBar>(R.id.rbRating)
        val tvDistance = sheetView.findViewById<TextView>(R.id.tvDistance)

        tvName.text     = r.name
        rbRating.rating = r.rating.toFloat()
        tvDistance.text = String.format("%.1f km", r.distance)

        r.photoMetadata?.let { meta ->
            placesClient.fetchPhoto(FetchPhotoRequest.builder(meta).build())
                .addOnSuccessListener { resp -> ivPhoto.setImageBitmap(resp.bitmap) }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
