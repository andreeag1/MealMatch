package com.mealmatch.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.mealmatch.BuildConfig
import com.mealmatch.R
import com.mealmatch.data.model.Restaurant
import com.mealmatch.data.model.SearchCriteria
import com.mealmatch.databinding.FragmentMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragment : Fragment(), OnMapReadyCallback, MapManager.MapManagerListener, FiltersDialogFragment.FiltersDialogListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var placesClient: PlacesClient
    private var mapManager: MapManager? = null
    private var currentLocation: Location? = null
    private var currentSearchCenter: LatLng? = null

    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var mapRestaurantAdapter: RestaurantAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>

    private var currentSearchCriteria = SearchCriteria()

    private val mapViewModel: MapViewModel by viewModels {
        MapViewModelFactory(RestaurantRepository(placesClient))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupMap()
        observeViewModel()
    }

    private fun setupUI() {
        restaurantAdapter = RestaurantAdapter(placesClient)
        mapRestaurantAdapter = RestaurantAdapter(placesClient)

        binding.rvRestaurantsFull.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = restaurantAdapter
        }
        binding.rvMapRestaurants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mapRestaurantAdapter
        }

        setupFilterChips()

        binding.tabLayout.apply {
            addTab(newTab().setText("Restaurants"))
            addTab(newTab().setText("Map"))
            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    if (tab.position == 0) showList() else showMap()
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }

        binding.svQuery.isIconified = false

        binding.svAddress.apply {
            isIconified = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(address: String?): Boolean {
                    clearFocus()
                    if (!address.isNullOrBlank()) {
                        geocodeAddressAndFetch(address)
                    }
                    return true
                }
                override fun onQueryTextChange(s: String?): Boolean = false
            })
        }

        binding.rootCoordinator.requestFocus()

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setupFilterChips() {
        val quickCuisines = listOf("Pizza", "Sushi", "Cafe", "Burger")
        val chipGroup = binding.chipGroupCuisines
        chipGroup.isSingleSelection = true
        chipGroup.isSelectionRequired = false

        quickCuisines.forEach { cuisine ->
            val chip = (layoutInflater.inflate(R.layout.item_filter_chip, chipGroup, false) as Chip).apply {
                text = cuisine
                isCheckable = true
            }
            chipGroup.addView(chip)
        }

        val filterChip = (layoutInflater.inflate(R.layout.item_filter_chip, chipGroup, false) as Chip).apply {
            text = "Filters"
            chipIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close)
            isCheckable = false // This chip acts as a button
            setOnClickListener {
                FiltersDialogFragment.newInstance(currentSearchCriteria)
                    .show(childFragmentManager, FiltersDialogFragment.TAG)
            }
        }
        chipGroup.addView(filterChip)

        // Listener for quick filters
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // The listener provides a list of checked IDs. For single selection, it will have 0 or 1 item.
            if (checkedIds.isEmpty()) {
                currentSearchCriteria = SearchCriteria()
            } else {
                val checkedId = checkedIds.first()
                val selectedChip = group.findViewById<Chip>(checkedId)
                if (selectedChip != null && selectedChip.isCheckable) {
                    // Create new criteria based on the single quick filter selected
                    currentSearchCriteria = SearchCriteria(cuisines = mutableSetOf(selectedChip.text.toString()))
                }
            }
            mapViewModel.updateSearchCriteria(currentSearchCriteria)
        }
    }

    override fun onFiltersApplied(searchCriteria: SearchCriteria) {
        this.currentSearchCriteria = searchCriteria
        mapViewModel.updateSearchCriteria(searchCriteria)
        binding.chipGroupCuisines.clearCheck()
    }

    private fun setupMap() {
        var mapFrag = childFragmentManager.findFragmentById(R.id.google_map) as? SupportMapFragment
        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.google_map, mapFrag).commit()
        }
        mapFrag.getMapAsync(this)
    }

    private fun observeViewModel() {
        mapViewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            restaurantAdapter.submitList(restaurants)
            mapRestaurantAdapter.submitList(restaurants)
            mapManager?.updateMarkers(restaurants)
        }
        mapViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapManager = MapManager(requireContext(), googleMap, this)
        mapManager?.enableMyLocation(locationPermissionLauncher)
        fetchInitialLocation()
    }

    fun toggleFreeExploreMode(enabled: Boolean) {
        mapManager?.isFetchOnCameraMoveEnabled = enabled
    }

    private fun showList() {
        binding.rvRestaurantsFull.visibility = View.VISIBLE
        binding.googleMap.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun showMap() {
        binding.rvRestaurantsFull.visibility = View.GONE
        binding.googleMap.visibility = View.VISIBLE
        if (mapRestaurantAdapter.currentList.isNotEmpty()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun geocodeAddressAndFetch(address: String) {
        // Use a coroutine to avoid blocking the main thread
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results = Geocoder(requireContext()).getFromLocationName(address, 1)
                if (results?.isNotEmpty() == true) {
                    val addr = results[0]
                    val newSearchCenter = LatLng(addr.latitude, addr.longitude)
                    currentSearchCenter = newSearchCenter

                    val originForDistance = currentLocation ?: Location("").apply {
                        latitude = newSearchCenter.latitude
                        longitude = newSearchCenter.longitude
                    }

                    // Fetch the new list of restaurants
                    mapViewModel.fetchRestaurantsForLocation(newSearchCenter, originForDistance)

                    // Switch to the main thread to update the map UI
                    withContext(Dispatchers.Main) {
                        // This line pans the map to the new location
                        mapManager?.animateCamera(newSearchCenter)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Address not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error searching address", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchInitialLocation() {
        LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    currentSearchCenter = LatLng(location.latitude, location.longitude)
                    mapManager?.moveCamera(currentSearchCenter!!)
                    mapViewModel.fetchRestaurantsForLocation(currentSearchCenter!!, location)
                } else {
                    Toast.makeText(context, "Could not get current location.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCameraIdle(target: LatLng) {
        // UPDATED: Call the correct ViewModel function
        if (mapManager?.isFetchOnCameraMoveEnabled == true) {
            currentLocation?.let {
                mapViewModel.fetchRestaurantsForLocation(target, it)
            }
        }
    }

    override fun onMarkerClicked(restaurant: Restaurant): Boolean {
        showDetailSheet(restaurant)
        return true
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchInitialLocation()
            } else {
                Toast.makeText(context, "Location permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun showDetailSheet(r: Restaurant) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = layoutInflater.inflate(R.layout.restaurant_details, null)

        val ivPhoto = sheetView.findViewById<ImageView>(R.id.ivPhoto)
        val tvName = sheetView.findViewById<TextView>(R.id.tvName)
        val rbRating = sheetView.findViewById<RatingBar>(R.id.rbRating)
        val tvDistance = sheetView.findViewById<TextView>(R.id.tvDistance)

        tvName.text = r.name
        rbRating.rating = r.rating.toFloat()
        tvDistance.text = String.format("%.1f km", r.distance)

        r.photoMetadata?.let { meta ->
            val photoRequest = FetchPhotoRequest.builder(meta).build()
            placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener { response -> ivPhoto.setImageBitmap(response.bitmap) }
                .addOnFailureListener { ivPhoto.setImageResource(R.drawable.restaurant) }
        } ?: ivPhoto.setImageResource(R.drawable.restaurant)

        dialog.setContentView(sheetView)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
