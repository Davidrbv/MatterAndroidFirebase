package com.example.matterandroid.principalFragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.matterandroid.R
import com.example.matterandroid.adapters.PhotoAdapter
import com.example.matterandroid.databinding.FragmentGaleryBinding
import com.example.matterandroid.entities.Photo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class GaleryFragment : Fragment() {

    private lateinit var binding : FragmentGaleryBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var photoAdapter : PhotoAdapter
    private val photos = mutableListOf<Photo>()
    private lateinit var photo : Photo

    //Lunch external app persmission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        if (it) launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        else
            Snackbar.make(binding.root, R.string.noWay, Snackbar.LENGTH_LONG).show()
    }

    //Lunch Camera
    private val launchCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { it ->
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val userUid = user?.uid
            val photoUid = System.currentTimeMillis().toString()
            val path = "${userUid}/galery/${photoUid}"
            val locationCampanillas = "PSP Campanillas 2022: ${photoUid}"

            val ref = storage.reference
                .child(path)
            val baos = ByteArrayOutputStream()
            val image = it.data?.extras?.get("data") as Bitmap
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            ref.putBytes(baos.toByteArray())
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnCompleteListener {
                            val downloadUri = it.result.toString()
                            photo = Photo().apply {
                                url = downloadUri
                                location = locationCampanillas
                            }
                            registerPhoto(photo)
                        }
                    Snackbar.make(binding.root,R.string.addImageOk,Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root,R.string.err_addImage,Snackbar.LENGTH_SHORT).show()
                }
        }
    }


    //Select image file
    private val launchExplorer = registerForActivityResult(ActivityResultContracts.GetContent())
    {
        if (it != null) {
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val userUid = user?.uid
            val photoUid = System.currentTimeMillis().toString()
            val path = "${userUid}/galery/${photoUid}"
            val locationCampanillas = "PSP: ${photoUid}"

            val ref = storage.reference.child(path)
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnCompleteListener {
                            val downloadUri = it.result.toString()
                            photo = Photo().apply {
                                url = downloadUri
                                location = locationCampanillas
                            }
                            photos.add(photo)
                            val uid = photos.size
                            photoAdapter.notifyItemInserted(uid)
                            registerPhoto(photo)
                        }
                    Snackbar.make(binding.root,R.string.addImageOk,Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Snackbar.make(binding.root,R.string.err_addImage,Snackbar.LENGTH_SHORT).show()
                }

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGaleryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth
        storage = FirebaseStorage.getInstance()

        //Setter bar
        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.galery)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        getPhotos()

        binding.photoRecycler.layoutManager = GridLayoutManager(this.requireContext(),2)

        binding.btnAddPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@GaleryFragment.requireContext(),
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            }
            else{
                requestPermission.launch(Manifest.permission.CAMERA)
            }

        }

        //Pick image file
        binding.btnAddPhoto.setOnLongClickListener {
            launchExplorer.launch("image/*")
            true
        }

        super.onResume()
    }

    /**
     * Recovery user's galery photo list
     */

    fun getPhotos(){

        val uid : String? = auth.uid
        val path = "users/${uid}/photos"

        db.collection(path).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for(photo in documents){
                        val actual = photo.toObject(Photo::class.java)
                        actual.photoId = photo.id
                        photos.add(actual)
                    }
                    drawList(path)
                } else {
                    Snackbar.make(binding.root, R.string.without, Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(binding.root,exception.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

    /**
     * Delete user's photo
     */

    fun deletePhoto(photo : Photo,path : String){
        db.collection(path).document("${photo.photoId}").delete()
    }

    fun drawList(path : String){
        val shortClickE : (MenuItem, Photo) -> Boolean = {
                item : MenuItem, photo : Photo ->

            when(item.itemId) {

                R.id.deleteItem -> {

                    val mp = MediaPlayer.create(this.requireContext(),R.raw.sound_delete)
                    mp.start()

                    MaterialAlertDialogBuilder(this.requireContext())
                        .setTitle(R.string.deletePhoto)
                        .setMessage(R.string.sure)
                        .setPositiveButton("Yes"){d,i ->
                            val idPhoto = photos.indexOfFirst { it==(photo) }
                            photos.removeAt(idPhoto)
                            photoAdapter.notifyItemRemoved(idPhoto)
                            deletePhoto(photo,path)
                            Snackbar.make(binding.root,R.string.confirmDeletePhoto, Snackbar.LENGTH_LONG).show()
                        }
                        .setNegativeButton("No"){d,i ->
                            Snackbar.make(binding.root,R.string.deleteCancel, Snackbar.LENGTH_LONG).show()
                        }
                        .show()
                    true
                }
            }
            false
        }

        photoAdapter = PhotoAdapter(photos).apply{
            shortClick = shortClickE
        }
        binding.photoRecycler.apply {
            adapter = photoAdapter
            setHasFixedSize(true)
        }
    }

    fun registerPhoto(photo : Photo){

        val uid = Firebase.auth.uid
        val photosPath = "users/${uid}/photos/${photo.photoId}"

        db.collection(photosPath).add(photo)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root,R.string.correctRegisterPhoto, Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }


    }

}