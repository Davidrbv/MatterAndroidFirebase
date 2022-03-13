package com.example.matterandroid.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matterandroid.R
import com.example.matterandroid.databinding.PhotoItemBinding
import com.example.matterandroid.entities.Photo

class PhotoAdapter(var photos : MutableList<Photo>) : RecyclerView.Adapter<PhotoAdapter.PhotoContainer>() {

    //shortClick listener
    var shortClick: (MenuItem, Photo) -> Boolean ={ menuItem : MenuItem, photo : Photo -> false }
        set(value){
            field = value
        }

    /**
     * Override RecyclerView Methods
     */

    //Elements count
    override fun getItemCount() : Int = photos.size

    //Create container
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoContainer {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PhotoItemBinding.inflate(inflater,parent,false)
        return PhotoContainer(binding)
    }

    //Associate container to photo item
    override fun onBindViewHolder(holder: PhotoAdapter.PhotoContainer, position: Int) {
        holder.bindPhoto(photos[position])
    }


    /**
     * Photo Container inner class
     */

    inner class PhotoContainer (val binding : PhotoItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindPhoto ( photo : Photo){

            //Show photo

            Glide
                .with(binding.root)
                .load(photo.url)
                .centerCrop()
                .into(binding.imageGalery);
            binding.locationImage.text = photo.location


            //Listener largeClick
            binding.root.setOnClickListener {

                val menu = PopupMenu(binding.root.context, binding.imageGalery)
                menu.inflate(R.menu.menu_items)
                menu.setOnMenuItemClickListener { shortClick(it,photo) }
                menu.show()

                true
            }

        }
    }


}