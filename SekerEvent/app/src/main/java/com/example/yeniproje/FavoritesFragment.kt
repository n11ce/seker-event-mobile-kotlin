package com.example.yeniproje

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yeniproje.databinding.FragmentEventBinding
import com.example.yeniproje.databinding.FragmentFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: EventAdapter

    // EventService'i initialize et
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://app.ticketmaster.com/discovery/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val eventService = retrofit.create(EventService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        // RecyclerView setup
        setupRecyclerView()

        // Firebase'den favori etkinlikleri al
        fetchFavoriteEventIds()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        eventAdapter = EventAdapter { event -> openEventDetails(event) }
        binding.recyclerView.adapter = eventAdapter
    }

    private fun fetchFavoriteEventIds() {
        // Firebase'den favori etkinliklerin ID'lerini çek
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid // Kullanıcı ID'sini al
        val favoritesRef = db.collection("users").document(userId!!).collection("favorites")

        favoritesRef.get().addOnSuccessListener { documents ->
            val favoriteEventIds = mutableListOf<String>()
            for (document in documents) {
                // Firebase'den alınan her favori etkinlik ID'si
                favoriteEventIds.add(document.id)
            }
            // Favori etkinlik ID'lerini kullanarak API'den verileri al
            getFavoriteEvents(favoriteEventIds)
        }.addOnFailureListener { e ->
            Log.e("FavoritesFragment", "Error getting favorite event IDs: ${e.message}")
        }
    }

    private fun getFavoriteEvents(favoriteEventIds: List<String>) {
        // Tüm favori etkinliklerini tutacak bir liste oluşturuyoruz
        val allEvents = mutableListOf<Event>()

        // Firebase'den alınan ID'leri kullanarak Ticketmaster API'den verileri al
        val calls = mutableListOf<Call<EventsResponse>>()

        // API çağrılarını paralel olarak yapıyoruz
        for (eventId in favoriteEventIds) {
            val call = eventService.getFavorites("uBmXlObGlKnGrt6JWMLdqPeMXx1L7RDU", eventId)
            calls.add(call)

            // API çağrılarını sırasıyla işliyoruz
            call.enqueue(object : Callback<EventsResponse> {
                override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val events = response.body()?._embedded?.events ?: emptyList()
                        allEvents.addAll(events)

                        // Tüm API çağrıları tamamlandığında adapter'ı güncelle
                        if (allEvents.size == favoriteEventIds.size) {
                            eventAdapter.setEvents(allEvents)
                        }
                    } else {
                        Log.e("API Error", "Failed to fetch event data for ID: ${eventId}")
                    }
                }

                override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                    Log.e("API Error", "API call failed for event ID: $eventId: ${t.message}")
                }
            })
        }
    }


    private fun openEventDetails(event: Event) {
        // Event detaylarına gitmek için dialog veya yeni fragment göster
        val dialog = EventDetailsDialogFragment.newInstance(event)
        dialog.show(parentFragmentManager, "EventDetailsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

