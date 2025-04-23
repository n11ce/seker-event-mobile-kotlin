package com.example.yeniproje

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.yeniproje.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap

    private val events = mutableListOf<Event>() // Etkinlikler listesi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root

        // Harita yükleme
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Etkinlikleri API'den al
        fetchEvents()

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableUserLocation()

        // API'den alınan etkinlikler haritada gösterilecek
        addEventMarkers()
    }

    private fun enableUserLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun fetchEvents() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/discovery/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val eventService = retrofit.create(EventService::class.java)
        val call = eventService.getEvents("uBmXlObGlKnGrt6JWMLdqPeMXx1L7RDU", "20")

        call.enqueue(object : Callback<EventsResponse> {
            override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val eventsResponse = response.body()!!
                    events.clear()
                    events.addAll(eventsResponse._embedded.events)

                    // Marker'ları haritaya ekle
                    addEventMarkers()
                }
            }

            override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                Log.e("API Error", "Etkinlikler alınamadı", t)
            }
        })
    }

    private fun addEventMarkers() {
        for (event in events) {
            val venue = event._embedded?.venues?.firstOrNull()
            val location = venue?.location

            if (location == null || location.latitude == null || location.longitude == null) {
                continue
            }

            val latitude = location.latitude
            val longitude = location.longitude

            val markerOptions = MarkerOptions()
                .position(LatLng(latitude, longitude))
                .title(event.name)

            // Marker'ı haritaya ekle
            val marker = googleMap.addMarker(markerOptions)

            // Marker tıklama olayını her bir marker için değil, bir kez ayarla
            googleMap.setOnMarkerClickListener { clickedMarker ->
                if (clickedMarker == marker) {
                    // EventDetailsDialogFragment'ı aç
                    openEventDetails(event)
                    true // Marker tıklama işlemi tamamlandı
                } else {
                    false // Diğer marker'lar için işlem yapılmaz
                }
            }
        }
    }

    private fun openEventDetails(event: Event) {
        val dialog = EventDetailsDialogFragment.newInstance(event)
        dialog.show(childFragmentManager , "EventDetailsDialog")
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



