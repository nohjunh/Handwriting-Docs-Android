package com.example.capstoneandroid.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneandroid.R
import com.example.capstoneandroid.stylusActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ItemAdapter(private val context: Context, private val dataset: List<StorageReference>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.fileName)
        val imageView: ImageView = view.findViewById(R.id.fileImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_recyclerview, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView.text = item.name.substring(0 until item.name.length - 4)

        val ONE_MEGABYTE: Long = 1024 * 1024
        item.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            holder.imageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
        }.addOnFailureListener {
            // Handle any errors
        }

        holder.itemView.setOnClickListener {
            Log.d("click", "$item")
            val intent = Intent(context, stylusActivity::class.java)
            intent.putExtra("번호", 7)
            intent.putExtra("이미지", item.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = dataset.size
}