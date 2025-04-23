package com.example.yeniproje

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yeniproje.databinding.FragmentEventBinding
import kotlinx.parcelize.Parcelize
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class EventFragment : Fragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEventBinding.inflate(inflater, container, false)

        setupSpinner()
        setupRecyclerView()
        setupSearchButton()


        performSearch("", "")
        return binding.root
    }

    private fun setupSpinner() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/discovery/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val classificationService = retrofit.create(ClassificationService::class.java)
        val call = classificationService.getCategories("uBmXlObGlKnGrt6JWMLdqPeMXx1L7RDU")

        call.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val classifications = response.body()?._embedded?.classifications
                    val segments = classifications?.mapNotNull { it.segment?.name } ?: emptyList()

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        segments
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerCategories.adapter = adapter
                } else {
                    Log.e("API Error", "Failed to fetch categories")
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.e("API Error", "API call failed: ${t.message}")
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        eventAdapter = EventAdapter { event -> openEventDetails(event) }
        binding.recyclerView.adapter = eventAdapter
    }

    private fun setupSearchButton() {
        binding.buttonSearch.setOnClickListener {
            val searchQuery = binding.editTextSearch.text.toString()
            val selectedCategory = binding.spinnerCategories.selectedItem.toString()
            performSearch(searchQuery, selectedCategory)
        }
    }

    private fun performSearch(query: String, category: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://app.ticketmaster.com/discovery/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val eventService = retrofit.create(EventService::class.java)
        val call = eventService.getEventsWithQueryAndCategory(
            keyword = query,
            classificationName = category,
            apiKey = "uBmXlObGlKnGrt6JWMLdqPeMXx1L7RDU",
            size = "30"
        )

        call.enqueue(object : Callback<EventsResponse> {
            override fun onResponse(call: Call<EventsResponse>, response: Response<EventsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val events = response.body()?._embedded?.events ?: emptyList()
                    eventAdapter.setEvents(events)
                } else {
                    Log.e("API Error", "Failed to fetch events")
                }
            }

            override fun onFailure(call: Call<EventsResponse>, t: Throwable) {
                Log.e("API Error", "API call failed: ${t.message}")
            }
        })
    }

    private fun openEventDetails(event: Event) {
        val dialog = EventDetailsDialogFragment.newInstance(event)
        dialog.show(parentFragmentManager, "EventDetailsDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


// Event API Service
interface EventService {
    @GET("events.json")
    fun getEventsWithQueryAndCategory(
        @Query("keyword") keyword: String,
        @Query("classificationName") classificationName: String,
        @Query("apikey") apiKey: String,
        @Query("size") size: String
    ): Call<EventsResponse>
    @GET("events.json")
    fun getEvents(
        @Query("apikey") apiKey: String,
        @Query("size") size: String
    ): Call<EventsResponse>
    @GET("events.json")
    fun getFavorites(
        @Query("apikey") apiKey: String,
        @Query("id") id: String
    ): Call<EventsResponse>
}

// Classification API Service
interface ClassificationService {
    @GET("classifications.json")
    fun getCategories(@Query("apikey") apiKey: String): Call<CategoriesResponse>
}

// Response objects
@Parcelize
data class EventsResponse(
    val _embedded: Embedded
) : Parcelable

@Parcelize
data class CategoriesResponse(
    val _embedded: ClassificationEmbedded
) : Parcelable

@Parcelize
data class ClassificationEmbedded(
    val classifications: List<Classification>
) : Parcelable

@Parcelize
data class Embedded(
    val events: List<Event>,
    val venues: List<Venue>
) : Parcelable

@Parcelize
data class Venue(
    val location: Location?,
    val name: String?,  // Etkinlik yeri ismi
) : Parcelable

@Parcelize
data class Event(
    val name: String,
    val images: List<Image>,
    val classifications: List<Classification>,
    val dates: Date,
    val promoter: Promoter?,
    val location: Location,
    val _embedded: Embedded?,
    val products: List<Product>?,
    val id: String
) : Parcelable {
}

@Parcelize
data class Image(
    val url: String
) : Parcelable

@Parcelize
data class Classification(
    val name: String,
    val segment: Segment,
    val genre: Genre? // Alt kategori
) : Parcelable

@Parcelize
data class Date(
    val start: Start
) : Parcelable

@Parcelize
data class Start(
    val localDate: String,
    val localTime: String // Etkinlik saati
) : Parcelable

@Parcelize
data class Promoter(
    val name: String
) : Parcelable

@Parcelize
data class Segment(
    val name: String
) : Parcelable

@Parcelize
data class Genre(
    val name: String
) : Parcelable

@Parcelize
data class Location(
    val latitude: Double?,
    val longitude: Double?,
    val address: String? // Etkinlik adresi
) : Parcelable

@Parcelize
data class Product(
    val name: String, // Ürün adı (örneğin, etkinlik bileti)
    val venue: Venue // Etkinlik yeri
) : Parcelable
