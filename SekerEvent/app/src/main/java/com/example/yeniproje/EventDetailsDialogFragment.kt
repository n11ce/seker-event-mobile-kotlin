package com.example.yeniproje

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.yeniproje.databinding.FragmentEventDetailsBinding
import com.bumptech.glide.Glide
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EventDetailsDialogFragment : DialogFragment() {

    private var _binding: FragmentEventDetailsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_EVENT = "event"

        fun newInstance(event: Event): EventDetailsDialogFragment {
            val fragment = EventDetailsDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_EVENT, event)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailsBinding.inflate(inflater, container, false)

        // Retrieve the event from arguments once
        val event = arguments?.getParcelable<Event>(ARG_EVENT)

        event?.let { event ->
            // Event data binding
            binding.textViewEventName.text = event.name
            binding.textViewEventDate.text = "Etkinlik Tarihi: " + event.dates.start.localDate
            binding.textViewEventTimee.text = "Etkinlik Zamanı: "+ event.dates.start.localTime
            binding.textViewEventVenue.text = "Lokasyon: "+ event.products?.firstOrNull()?.name ?: "Venue Not Available"
            binding.textViewEventCategoryy.text = "Kategori: "+"${event.classifications?.firstOrNull()?.segment?.name} - ${event.classifications?.firstOrNull()?.genre?.name}"
            binding.textViewEventPromoter.text = "Organizatör: "+event.promoter?.name ?: "Organizatör Yok"
            event.images?.firstOrNull()?.url?.let { imageUrl ->
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(binding.imageViewEventt)
            }

            // Check if the event is already a favorite
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                checkIfFavorite(userId, event.id)
            }

            // Add to calendar button
            binding.buttonAddToCalendar.setOnClickListener {
                addEventToCalendar(event)
            }

            // Favorite button
            binding.buttonFavorite.setOnClickListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    // Check current button text and toggle favorite status
                    if (binding.buttonFavorite.text == "Favoriye Ekle") {
                        addEventToFavorites(userId, event.id)
                    } else {
                        removeEventFromFavorites(userId, event.id)
                    }
                }
            }
        }

        // Close button listener
        binding.buttonClose.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun addEventToCalendar(event: Event) {
        val startDate = event.dates.start.localDate
        val startTime = event.dates.start.localTime

        val calendar = Calendar.getInstance()
        val startTimeArray = startTime.split(":").map { it.toInt() }
        calendar.set(
            startDate.split("-")[0].toInt(),
            startDate.split("-")[1].toInt() - 1,
            startDate.split("-")[2].toInt(),
            startTimeArray[0],
            startTimeArray[1]
        )

        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + 60 * 60 * 1000)
        intent.putExtra(CalendarContract.Events.TITLE, event.name)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Event: ${event.name}")
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.products?.firstOrNull()?.name ?: "Venue Not Available")
        intent.putExtra(CalendarContract.Events.ALL_DAY, false)

        // Optionally set reminder
        val reminderTime = 10
        intent.putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT=1")
        intent.putExtra(CalendarContract.Reminders.MINUTES, reminderTime)

        startActivity(intent)
    }

    private fun addEventToFavorites(userId: String, eventId: String) {
        val db = FirebaseFirestore.getInstance()
        val favoritesRef = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(eventId)

        // Set data for favorite event (Optional: add additional fields like eventName, eventDate)
        favoritesRef.set(mapOf("eventId" to eventId))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Etkinlik favorilere eklendi!", Toast.LENGTH_SHORT).show()
                binding.buttonFavorite.text = "Favoriden Kaldır" // Update button text
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeEventFromFavorites(userId: String, eventId: String) {
        val db = FirebaseFirestore.getInstance()
        val favoritesRef = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(eventId)

        // Delete the event from favorites collection
        favoritesRef.delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Etkinlik favorilerden kaldırıldı!", Toast.LENGTH_SHORT).show()
                binding.buttonFavorite.text = "Favoriye Ekle" // Update button text
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfFavorite(userId: String, eventId: String) {
        val db = FirebaseFirestore.getInstance()
        val favoritesRef = db.collection("users")
            .document(userId)
            .collection("favorites")
            .document(eventId)

        // Check if the event is already in the favorites
        favoritesRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Event is already in favorites, set the button text to "Favoriden Kaldır"
                    binding.buttonFavorite.text = "Favoriden Kaldır"
                } else {
                    // Event is not in favorites, set the button text to "Favoriye Ekle"
                    binding.buttonFavorite.text = "Favoriye Ekle"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
