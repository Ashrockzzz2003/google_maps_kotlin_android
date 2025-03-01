package com.mad.maps

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mad.maps.data.MarkerData
import com.mad.maps.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isCardVisible = false
    private var markerDataCacheHandler: MarkerDataCacheHandler? = null

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LocationPermission", "Requesting permissions")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("LocationPermission", "Permissions already granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        markerDataCacheHandler = MarkerDataCacheHandler(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val latitudeInput = findViewById<EditText>(R.id.latitude_input)
        val longitudeInput = findViewById<EditText>(R.id.longitude_input)
        val titleInput = findViewById<EditText>(R.id.title_input)
        val addMarkerButton = findViewById<Button>(R.id.add_marker_button)

        addMarkerButton.setOnClickListener {
            val latitude = latitudeInput.text.toString().toDoubleOrNull()
            val longitude = longitudeInput.text.toString().toDoubleOrNull()
            val title = titleInput.text.toString()

            if (latitude != null && longitude != null && title.isNotEmpty()) {
                val markerData = MarkerData(title, LatLng(latitude, longitude))
                addMarker(markerData)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(markerData.latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f))
                isCardVisible = false
                updateAddMarkerCardVisibility()
            } else {
                Toast.makeText(this, "Please enter valid coordinates and title", Toast.LENGTH_SHORT).show()
            }
        }

        val fabAddMarker = findViewById<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton>(R.id.fab_add_marker)
        fabAddMarker.setOnClickListener {
            isCardVisible = !isCardVisible
            updateAddMarkerCardVisibility()
        }

        // Used to choose between 3 map types: Normal, Terrain, Satellite
        val mapTypeSpinner: Spinner = findViewById(R.id.mapTypeSpinner)
        val mapTypes = arrayOf("Normal  ", "Terrain  ", "Satellite")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mapTypeSpinner.adapter = adapter

        mapTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    1 -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    2 -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

    }

    private fun showDeleteConfirmationDialog(marker: Marker) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Marker")
        builder.setMessage("Do you want to delete this marker?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            marker.remove()
            val markerData = MarkerData(marker.title ?: "", marker.position)
            markerDataCacheHandler?.deleteMarkerData(markerData)
            Toast.makeText(this, "Marker deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val markerDataList = markerDataCacheHandler?.getMarkerDataList()
        markerDataList?.forEach { markerData ->
            addMarker(markerData)
        }

        // Uncomment the following line to enable satellite view.
        // mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        mMap.setOnMarkerClickListener { marker ->
            showDeleteConfirmationDialog(marker)
            true // Return true to indicate we have handled the event
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, update the map
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(8.0f))
                        }
                    }
                }
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addMarker(markerData: MarkerData) {
        mMap.addMarker(MarkerOptions().position(markerData.latLng).title(markerData.title))
        markerDataCacheHandler?.saveMarkerData(markerData)
    }

    private fun updateAddMarkerCardVisibility() {
        if (isCardVisible) {
            binding.fabAddMarker.text = getString(R.string.close)
            binding.latitudeInput.text.clear()
            binding.longitudeInput.text.clear()
            binding.titleInput.text.clear()
            binding.cardAddMarker.visibility = View.VISIBLE
        } else {
            binding.fabAddMarker.text = getString(R.string.add_marker_button_text)
            binding.latitudeInput.text.clear()
            binding.longitudeInput.text.clear()
            binding.titleInput.text.clear()
            binding.cardAddMarker.visibility = View.GONE
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.fabAddMarker.windowToken, 0)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
