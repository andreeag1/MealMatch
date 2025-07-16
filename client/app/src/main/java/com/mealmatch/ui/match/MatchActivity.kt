package com.mealmatch.ui.match

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.view.MotionEvent
import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.TextView
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.mealmatch.R
import com.mealmatch.data.local.TokenManager
import com.mealmatch.data.model.Swipe
import com.mealmatch.data.model.Restaurant
import com.mealmatch.databinding.ActivityMatchBinding
import com.mealmatch.ui.friends.ApiResult
import kotlin.math.abs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.pm.PackageManager
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import android.location.Location
import android.text.TextUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.PhotoMetadata
import com.google.android.libraries.places.api.net.FetchPhotoRequest

class MatchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMatchBinding
    private val viewModel: MatchViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var restaurants: List<Restaurant> = emptyList()
    
    private var sessionId: String? = null
    private var currIndex = 0
    private val userSwipes = mutableListOf<Swipe>()

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndRestaurants()
        } else {
            Toast.makeText(this, "Location permission is required for matching.", Toast.LENGTH_LONG).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        sessionId = intent.getStringExtra("SESSION_ID")
        val userLat = intent.getDoubleExtra("USER_LAT", Double.NaN)
        val userLng = intent.getDoubleExtra("USER_LNG", Double.NaN)
        var usedLocation: Location? = null
        if (!userLat.isNaN() && !userLng.isNaN()) {
            usedLocation = Location("").apply {
                latitude = userLat
                longitude = userLng
            }
        }

        if (sessionId == null) {
            startNewSoloSession()
        }
        if (usedLocation != null) {
            viewModel.fetchNearbyRestaurants(usedLocation)
        } else {
            fetchLocationAndRestaurants()
        }
        setupToolbar()
        setupButtons()
        observeViewModel()
    }

    private fun fetchLocationAndRestaurants() {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, fetch the location.

                fusedLocationClient.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            viewModel.fetchNearbyRestaurants(location)
                        } else {
                            Toast.makeText(this, "Could not get location. Please ensure location services are on.", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else -> {
                // Permission is not granted, launch the request.
                locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun loadPlacePhoto(photoMetadata: PhotoMetadata?, imageView: ImageView) {
        if (photoMetadata == null) {
            // if no image avail, use default icon
            imageView.setImageResource(R.drawable.restaurant)
            return
        }
        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(800)
            .setMaxHeight(500)
            .build()
        //  val placesClient = Places.createClient(this)
        //  Creating a client for every photo request which is bad practice
        //  Use the 1 client from viewModel for all requests below to avoid resource leaks
        viewModel.placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener { response ->
                imageView.setImageBitmap(response.bitmap)
            }
            .addOnFailureListener {
                imageView.setImageResource(R.drawable.restaurant)
            }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun startNewSoloSession() {
        val token = TokenManager.getToken(this)
        if (token == null) {
            Toast.makeText(this, "Authentication Error", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        viewModel.startNewSession("Bearer $token", null)
    }

    private fun setupButtons() {
        binding.noButton.setOnClickListener { swipeCard(right = false) }
        binding.yesButton.setOnClickListener { swipeCard(right = true) }
    }

    private fun swipeCard(right: Boolean) {
        if (currIndex >= restaurants.size) return

        val restaurant = restaurants[currIndex]
        userSwipes.add(Swipe(restaurantId = restaurant.name, liked = right))

        val topId = binding.cardContainer.childCount - 1
        if (topId < 0) {
            return
        }
        val card = binding.cardContainer.getChildAt(topId)

        val directionX : Float
        if (right) {
            directionX = 2000f
        } else {
            directionX = -2000f
        }

        card.animate()
            .x(directionX)
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.cardContainer.removeView(card)
                currIndex++
                showNextCard()
            }
            .start()
    }

    private fun showNextCard() {
        if (currIndex >= restaurants.size) {
            submitAllSwipes()
            return
        }
        val card = createCard(restaurants[currIndex])
        setupSwipe(card)
        binding.cardContainer.addView(card)
    }

     private fun submitAllSwipes() {
        val token = TokenManager.getToken(this)
        if (token != null && sessionId != null) {
            viewModel.submitSwipes("Bearer $token", sessionId!!, userSwipes)
        } else {
            Toast.makeText(this, "Authentication Error", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        viewModel.restaurants.observe(this) { list ->
            if (list != null && list.isNotEmpty()) {
                restaurants = list
                currIndex = 0
                binding.cardContainer.removeAllViews()
                showNextCard()
            } else {
                binding.matchTitle.text = "No restaurants found nearby."
            }
        }

        viewModel.sessionResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(this, "Starting session...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    //this is only for solo sessions
                    val session = result.data
                    this.sessionId = session._id
                    Toast.makeText(this, "Session started!", Toast.LENGTH_SHORT).show()
                    showNextCard()
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error starting session: ${result.message}", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }

        viewModel.submitSwipesResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    binding.cardContainer.visibility = View.GONE
                    binding.buttonContainer.visibility = View.GONE
                    binding.matchTitle.text = "Waiting for other members..."
                }
                is ApiResult.Success -> {
                    Toast.makeText(this, "Your choices have been submitted!", Toast.LENGTH_LONG).show()
                    // Start polling for the result after successfully submitting swipes.
                    val token = TokenManager.getToken(this)
                    if (token != null && sessionId != null) {
                        viewModel.pollForSessionResult("Bearer $token", sessionId!!)
                    }
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error submitting choices: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.matchResult.observe(this) { result ->
            when (result) {
                is ApiResult.Loading -> {  }
                is ApiResult.Success -> {
                    val match = result.data
                    if (match.restaurantId != null) {
                        val matchedRestaurant = restaurants.find { it.name == match.restaurantId }
                        if (matchedRestaurant != null) {
                            binding.matchTitle.text = "It's a Match!"
                            binding.cardContainer.removeAllViews()
                            val resultCard = createCard(matchedRestaurant)
                            binding.cardContainer.addView(resultCard)
                            binding.cardContainer.visibility = View.VISIBLE
                        } else {
                            binding.matchTitle.text = "Match found, but restaurant details are missing."
                        }
                    } else {
                        binding.matchTitle.text = "No match was found."
                    }
                }
                is ApiResult.Error -> {
                    Toast.makeText(this, "Error getting match result: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createCard(restaurant: Restaurant) : CardView {
        val card = CardView(this).apply {
            layoutParams = FrameLayout.LayoutParams(800, 1000).apply {
                gravity = Gravity.CENTER
            }
            radius = 30f
            cardElevation = 12f
            setCardBackgroundColor(Color.WHITE)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32)
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                500
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // load image
        loadPlacePhoto(restaurant.photoMetadata, imageView)

        // wrap title
        val titleView = TextView(this).apply {
            text = restaurant.name
            textSize = 22f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 16, 0, 8)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val descView = TextView(this).apply {
            // parse to capitalize
            text = restaurant.primaryCuisine.split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 8)
            maxLines = 3
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        container.addView(imageView)
        container.addView(titleView)
        container.addView(descView)
        card.addView(container)
        return card
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupSwipe(card: View) {
        var dX = 0f
        var dY = 0f

        card.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val deltaX = v.x - v.width / 2
                    if (abs(deltaX) > 300) {
                        binding.cardContainer.removeView(v)
                        currIndex++
                        showNextCard()
                    } else {
                        v.animate()
                            .x(0f)
                            .y(0f)
                            .setDuration(300)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }
    }
}