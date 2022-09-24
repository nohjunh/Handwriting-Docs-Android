package com.example.howistagram_f16.navigation

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.howistagram_f16.R
import com.example.howistagram_f16.databinding.ActivityLoginBinding
import com.example.howistagram_f16.databinding.FragmentDetailBinding
import com.example.howistagram_f16.databinding.ItemDetailBinding
import com.example.howistagram_f16.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {

    var firestore : FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
        firestore = FirebaseFirestore.getInstance()

        var test= view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.detailviewfragmentRecycleView)
        test.adapter= DetailViewRecyclerViewAdapter()
        test.layoutManager = LinearLayoutManager(activity)
        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf()

        init{
            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            var detailViewItem_profile_image = viewholder.findViewById<ImageView>(R.id.detailviewitemProfileImage)
            var detailViewItem_explain_textView = viewholder.findViewById<TextView>(R.id.detailviewitemExplainTextView)
            var detailViewItem_imageView_content=viewholder.findViewById<ImageView>(R.id.detailviewitemImageViewContent)
            var detailViewItem_favoriteCounter_textView = viewholder.findViewById<TextView>(R.id.detailviewitemFavoriteCounterTextView)
            var detailViewItem_favorite_imageView = viewholder.findViewById<ImageView>(R.id.detailviewitemFavoriteImageView)
            //UserId
            var detailViewItem_profile_textView= viewholder.findViewById<TextView>(R.id.detailviewitemProfileTextView)
            detailViewItem_profile_textView.text= contentDTOs!![position].userId

            //Image
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(detailViewItem_imageView_content)

            //Explain of content
            detailViewItem_explain_textView.text = contentDTOs!![position].explain

            //likes counter
            detailViewItem_favoriteCounter_textView.text = "Likes "+ contentDTOs!![position].favoriteCount

            //profileImage
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(detailViewItem_profile_image)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

    }
}