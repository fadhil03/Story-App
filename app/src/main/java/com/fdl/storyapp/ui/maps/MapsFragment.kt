package com.fdl.storyapp.ui.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.fdl.storyapp.R
import com.fdl.storyapp.databinding.FragmentMapsBinding
import com.fdl.storyapp.utilities.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val args: MapsFragmentArgs by navArgs()

    private val viewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var googleMap: GoogleMap? = null

    private val callbackStories = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        setupView()
        setMapStyle(googleMap)

        viewModel.stories.observe(viewLifecycleOwner) { stories ->
            if (stories.isNotEmpty()) {
                stories.forEach {
                    if (it.lat != null && it.lon != null) {
                        val coordinate = LatLng(it.lat, it.lon)
                        googleMap.addMarker(MarkerOptions().position(coordinate).title(it.name).snippet(it.caption))
                    }
                }

                val latestStory = stories[0]
                val latLng = LatLng(latestStory.lat ?: 0.0, latestStory.lon ?: 0.0)

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5F))
            } else {
                getMyLastLocation(googleMap)
            }
        }
    }

    private val callbackPickLocation = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        var currentPosition = googleMap.cameraPosition.target
        setMapStyle(googleMap)
        setupView()
        getMyLastLocation(googleMap)

        googleMap.setOnCameraMoveStartedListener {
            binding.ivMarker.animate().translationY(-40F).start()
            binding.ivShadow.animate().scaleX(0.5F).scaleY(0.5F).start()
        }

        googleMap.setOnCameraIdleListener {
            binding.ivMarker.animate().translationY(0F).start()
            binding.ivShadow.animate().scaleX(1F).scaleY(1F).start()
            currentPosition = googleMap.cameraPosition.target
        }

        binding.btnSave.setOnClickListener {
            setFragmentResult(
                KEY_RESULT,
                Bundle().apply { putParcelable(KEY_LATLONG, currentPosition) })
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        val action = args.action
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (action == ACTION_PICK_LOCATION) {
            binding.ivMarker.isVisible = true
            binding.ivShadow.isVisible = true
            binding.btnSave.isGone = false
            mapFragment?.getMapAsync(callbackPickLocation)
            return
        }

        mapFragment?.getMapAsync(callbackStories)
        observeViewModel()
    }

    @SuppressLint("MissingPermission")
    private fun getMyLastLocation(googleMap: GoogleMap) {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            googleMap.isMyLocationEnabled = true
            fusedLocationProviderClient?.lastLocation?.addOnSuccessListener { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                if (location != null) googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10F))
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: false -> googleMap?.let { getMyLastLocation(it) }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION]
                    ?: false -> googleMap?.let { getMyLastLocation(it) }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMapStyle(googleMap: GoogleMap) {
        try {
            val success =
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style
                    )
                )
            if (!success) {
                Log.e(TAG, "Failed to parse map style.")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupView() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.userModel.observe(viewLifecycleOwner) {
            if (it.token != null) {
                viewModel.getStoriesWithLocation(it.token)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        var TAG: String = MapsFragment::class.java.simpleName
        const val ACTION_STORIES = 0
        const val ACTION_PICK_LOCATION = 1
        const val KEY_RESULT = "key_result"
        const val KEY_LATLONG = "key_latlong"
    }
}