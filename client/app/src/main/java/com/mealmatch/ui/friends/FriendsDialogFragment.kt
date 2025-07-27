package com.mealmatch.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mealmatch.data.local.TokenManager
import com.mealmatch.databinding.DialogFragmentFriendsBinding

class FriendsDialogFragment : DialogFragment() {

    private var _binding: DialogFragmentFriendsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        val token = TokenManager.getToken(requireContext())
        if (token != null) {
            val authToken = "Bearer $token"
            viewModel.fetchFriends(authToken)
            viewModel.getFriendRequests(authToken)
        } else {
            Toast.makeText(context, "Authentication error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        private val tabTitles = listOf("Friends", "Incoming", "Outgoing")

        override fun getItemCount(): Int = tabTitles.size

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FriendsTabFragment()
                1 -> RequestsListFragment.newInstance(isIncoming = true)
                else -> RequestsListFragment.newInstance(isIncoming = false)
            }
        }
        fun getPageTitle(position: Int): String {
            return tabTitles[position]
        }
    }
}