package com.example.yeniproje

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yeniproje.databinding.ItemEventBinding
import com.squareup.picasso.Picasso

class EventAdapter(
    private val onEventClick: (Event) -> Unit // Tıklama işlemi için callback
) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    private var events: List<Event> = emptyList()

    fun setEvents(events: List<Event>) {
        this.events = events
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.eventName.text = event.name
            Picasso.get().load(event.images[0].url).into(binding.eventImage) // Resmi Picasso ile set ediyoruz
            binding.eventSegment.text = event.classifications[0].segment.name
            binding.eventDate.text = event.dates.start.localDate
            binding.eventOrganizer.text = event.promoter?.name ?: "Organizatör Yok"

            // Tıklama işlemi için root'a listener ekleniyor
            binding.root.setOnClickListener {
                onEventClick(event) // Tıklanan etkinlik geri gönderiliyor
            }
        }
    }
}