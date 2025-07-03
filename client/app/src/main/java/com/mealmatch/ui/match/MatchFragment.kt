package com.mealmatch.ui

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
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import com.mealmatch.R
import com.mealmatch.databinding.FragmentMatchBinding
import kotlin.math.abs

data class Restaurant(val id: String, val name: String, val description: String, val imageId: Int)

class MatchFragment : Fragment() {
    private var _binding: FragmentMatchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MatchViewModel by viewModels()

    private val restaurants = listOf(
        Restaurant("restaurant_id_1", "ABC Sushi", "Premier Sushi and Sashimi.", R.drawable.sushi),
        Restaurant("restaurant_id_2", "DEF Burgers", "Delicious gourmet burgers and hand cut fries!", R.drawable.burgers),
        Restaurant("restaurant_id_3", "GHI Pasta", "Authentic Italian Cuisine.", R.drawable.pasta),
        Restaurant("restaurant_id_4", "JKL Tacos", "Fine Tacos and Burritos made fresh just for you!", R.drawable.tacos)
        )
    
    private var sessionId: String? = null
    private var currIndex = 0
    private val userSwipes = mutableListOf<Swipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()

        sessionId = arguments?.getString("SESSION_ID")

        if (sessionId == null) {
            // if no session ID was passed, it's a solo session created from match page
            startNewSoloSession()
        } else {
            // If a session ID exists, we are in a group session.
            showNextCard()
        }
    }

    private fun startNewSoloSession() {
        val token = TokenManager.getToken(requireContext())
        if (token == null) {
            Toast.makeText(context, "Authentication Error", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
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
        userSwipes.add(Swipe(restaurantId = restaurant.id, liked = liked))

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
            Toast.makeText(context, "Submitting your choices...", Toast.LENGTH_SHORT).show()
            submitAllSwipes()
        }
        val card = createCard(restaurants[currIndex])
        setupSwipe(card)
        binding.cardContainer.addView(card)
    }

     private fun submitAllSwipes() {
        val token = TokenManager.getToken(requireContext())
        if (token != null && sessionId != null) {
            viewModel.submitSwipes("Bearer $token", sessionId!!, userSwipes)
        } else {
            Toast.makeText(context, "Authentication Error", Toast.LENGTH_LONG).show()
        }
    }

    private fun observeViewModel() {
        viewModel.sessionResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {
                    Toast.makeText(context, "Starting session...", Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Success -> {
                    //this is only for solo sessions
                    val session = result.data
                    this.sessionId = session._id
                    Toast.makeText(context, "Session started!", Toast.LENGTH_SHORT).show()
                    showNextCard()
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error starting session: ${result.message}", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }

        viewModel.submitSwipesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ApiResult.Loading -> {  }
                is ApiResult.Success -> {
                    Toast.makeText(context, "Your choices have been submitted!", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                }
                is ApiResult.Error -> {
                    Toast.makeText(context, "Error submitting choices: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createCard(restaurant: Restaurant) : CardView {
        val context = requireContext()

        val card = CardView(context).apply {
            layoutParams = FrameLayout.LayoutParams(800, 1000).apply {
                gravity = Gravity.CENTER
            }
            radius = 30f
            cardElevation = 12f
            setCardBackgroundColor(Color.WHITE)
        }

        val container = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(32)
        }

        val imageView = android.widget.ImageView(context).apply {
            setImageResource(restaurant.imageId)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                500
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }

        val titleView = TextView(context).apply {
            text = restaurant.name
            textSize = 22f
            setTextColor(Color.BLACK)
            setPadding(0, 520, 0, 0)
        }

        val descView = TextView(context).apply {
            text = restaurant.description
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 580, 0, 0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}