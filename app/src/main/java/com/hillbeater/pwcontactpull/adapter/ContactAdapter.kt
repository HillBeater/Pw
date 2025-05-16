package com.hillbeater.pwcontactpull.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hillbeater.pwcontactpull.R
import com.hillbeater.pwcontactpull.data.model.ContactApiModel

class ContactAdapter(private val onEditClick: (ContactApiModel) -> Unit) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private var contactList = listOf<ContactApiModel>()

    private val backgroundColors = listOf(
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

    fun submitList(list: List<ContactApiModel>) {
        contactList = list
        notifyDataSetChanged()
    }

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtPhone: TextView = view.findViewById(R.id.txtPhone)
        val txtTitle: TextView = view.findViewById(R.id.txtTitle)
        val txtEmail: TextView = view.findViewById(R.id.txtEmail)
        val btnEdit: ImageView = view.findViewById(R.id.ivEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.txtName.text = contact.fullName
        holder.txtTitle.text = contact.course
        holder.txtPhone.text = contact.phone
        holder.txtEmail.text = contact.email

        val randomColorResId = backgroundColors.random()

        holder.itemView.findViewById<View>(R.id.background)
            .setBackgroundResource(randomColorResId)

        holder.btnEdit.setOnClickListener {
            onEditClick(contact)
        }
    }

    override fun getItemCount(): Int = contactList.size
}
