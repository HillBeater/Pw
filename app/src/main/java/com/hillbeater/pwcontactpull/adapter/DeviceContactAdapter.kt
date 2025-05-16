package com.hillbeater.pwcontactpull.adapter

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.hillbeater.pwcontactpull.R

class DeviceContactAdapter(
    private val context: Context,
    contactsList: List<String>
) : RecyclerView.Adapter<DeviceContactAdapter.ViewHolder>() {

    private val fullList: List<String> = contactsList.toList() // full list immutable
    private var filteredList: MutableList<String> = contactsList.toMutableList() // filtered list shown

    private val colors = listOf(
        R.color.red,
        R.color.pink,
        R.color.purple,
        R.color.indigo,
        R.color.blue,
        R.color.teal,
        R.color.green,
        R.color.orange,
        R.color.brown,
        R.color.blue_grey
    )

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitial: TextView = itemView.findViewById(R.id.tvInitial)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_device_contact, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = filteredList[position]
        val name = contact.split(":")[0].trim()

        holder.tvName.text = name

        val initial = if (name.isNotEmpty()) name[0].uppercaseChar().toString() else "?"
        holder.tvInitial.text = initial

        val bgDrawable = holder.tvInitial.background.mutate() as GradientDrawable
        val colorRes = colors[position % colors.size]
        val colorInt = ContextCompat.getColor(context, colorRes)
        bgDrawable.setColor(colorInt)
    }

    fun filter(query: String) {
        filteredList = if (query.isBlank()) {
            fullList.toMutableList()
        } else {
            fullList.filter {
                it.split(":")[0].contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }
}
