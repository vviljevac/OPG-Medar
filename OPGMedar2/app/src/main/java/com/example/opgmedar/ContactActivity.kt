package com.example.opgmedar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.opgmedar.databinding.ActivityContactBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class ContactActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
    }
private lateinit var binding: ActivityContactBinding
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBackContact.setOnClickListener{
            val backIntent = Intent(this, MainActivity::class.java)
            startActivity(backIntent)        }
        binding.ivFacebook.setOnClickListener{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/"))
            startActivity(i)
        }
        binding.ivInstagram.setOnClickListener{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/?hl=en"))
            startActivity(i)
        }
        binding.ivTwitter.setOnClickListener{
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/home"))
            startActivity(i)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val halfway = LatLng(44.63604605597276, 16.273512978036514)
        val medulin = LatLng(44.82610666865398, 13.9371771421245)
        mMap.addMarker(MarkerOptions().position(medulin).title("OPG Medar")).showInfoWindow()
        val zoomLevel = 5.8f //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(medulin, zoomLevel))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
         {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
            location ->
            if (location !=null){
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(halfway, zoomLevel))
                mMap.addMarker(MarkerOptions().position(currentLatLng).title("Vaša lokacija"))
            }
        }
    }
}