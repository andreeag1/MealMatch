package com.mealmatch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mealmatch.databinding.FragmentMapBinding

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // find the SupportMapFragment and request the map async
        val mapFrag = childFragmentManager
            .findFragmentById(R.id.google_map) as SupportMapFragment
        mapFrag.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val defaultLocation = LatLng(43.4723, -80.5449) //Waterloo
        map.addMarker(MarkerOptions()
            .position(defaultLocation)
            .title("Default Location"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 13f))

        // HERE you can enable “My Location” layer if you’ve got location permission:
        // map.isMyLocationEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
