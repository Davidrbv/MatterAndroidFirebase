package com.example.matterandroid.principalFragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentPanelBinding
import com.example.matterandroid.entities.Photo
import com.example.matterandroid.entities.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class PanelFragment : Fragment() {

    private lateinit var binding : FragmentPanelBinding
    private lateinit var storage : FirebaseStorage
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var photo : Photo
    private val users = mutableListOf<User>()

    //Lunch external app persmission
    private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        if (it) launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        else
            Snackbar.make(binding.root, R.string.noWay, Snackbar.LENGTH_LONG)
                .show()
    }

    //Lunch Camera
    private val launchCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { it ->
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            val userUid = user?.uid
            val photoUid = System.currentTimeMillis().toString()
            val path = "${userUid}/userPhoto/${photoUid}"

            val ref = storage.reference
                .child(path)
            val baos = ByteArrayOutputStream()
            val image = it.data?.extras?.get("data") as Bitmap
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)

            ref.putBytes(baos.toByteArray())
                .addOnSuccessListener {
                    Snackbar.make(binding.root,R.string.addImageOk,Snackbar.LENGTH_LONG).show()
                    ref.downloadUrl
                        .addOnCompleteListener {
                            val downloadUri = it.result.toString()
                            setUserImage(downloadUri)
                            photo = Photo().apply {
                                url = downloadUri
                                location = "PSP Campanillas 2022: ${photoUid}"
                            }
                            registerPhoto(photo)
                        }
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
            val path = "${userUid}/userPhoto/${photoUid}"

            val ref = storage.reference
                .child(path)
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl
                        .addOnCompleteListener {
                            val downloadUri = it.result.toString()
                            setUserImage(downloadUri)
                            photo = Photo().apply {
                                url = downloadUri
                                location = "PSP Campanillas 2022: ${photoUid}"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPanelBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.dashboard)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        drawUserCard()

        binding.apply {

            Glide
                .with(binding.root)
                .load(R.drawable.invoices)
                .centerCrop()
                .placeholder(R.drawable.invoices)
                .into(invoiceCardImage)

            Glide
                .with(binding.root)
                .load(R.drawable.sales)
                .centerCrop()
                .placeholder(R.drawable.sales)
                .into(saleCardImage)

            Glide
                .with(binding.root)
                .load(R.drawable.employees)
                .centerCrop()
                .placeholder(R.drawable.employees)
                .into(employeeCardImage)

            Glide
                .with(binding.root)
                .load(R.drawable.galery)
                .centerCrop()
                .placeholder(R.drawable.galery)
                .into(galeryCardImage)

            employeeCardText.setText(R.string.employee)
            invoiceCardText.setText(R.string.invoice)
            saleCardText.setText(R.string.sale)
            galeryCardText.setText(R.string.galery)

            //Pick image with camera.
            userCard.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@PanelFragment.requireContext(),
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                }
                else{
                    requestPermission.launch(Manifest.permission.CAMERA)
                }

            }

            //Pick image file
            userCard.setOnLongClickListener {
                launchExplorer.launch("image/*")
                true
            }

            cardSale.setOnClickListener {
                findNavController().navigate(R.id.action_panelFragment_to_saleFragment)
            }

            invoiceCard.setOnClickListener {
                findNavController().navigate(R.id.action_panelFragment_to_invoiceFragment)
            }

            employeeCard.setOnClickListener {
                findNavController().navigate(R.id.action_panelFragment_to_employeeFragment)
            }

            galeryCard.setOnClickListener {
                findNavController().navigate(R.id.action_panelFragment_to_galeryFragment)
            }
        }
        super.onResume()
    }

    private fun setUserImage(pathImage : String){

        val uid = Firebase.auth.uid
        val userPath = "users/${uid}/userData"
        val user = users[0]
        user.image = pathImage
        db.collection(userPath).document("${user.userId}").set(user)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root, R.string.correctModifySale, Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
        drawUserCard()
    }

    fun drawUserCard(){
        val uid = Firebase.auth.uid
        val userPath = "users/${uid}/userData"

        db.collection(userPath).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for(user in documents){
                        val actual = user.toObject(User::class.java)
                        users.add(actual)
                    }
                    Glide
                        .with(binding.root)
                        .load(users[0].image.toString())
                        .centerCrop()
                        .circleCrop()
                        .placeholder(R.drawable.employees)
                        .into(binding.userImage)
                    binding.userName.text = users[0].name!!.uppercase()
                } else {
                    Snackbar.make(binding.root, R.string.without,Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(binding.root,exception.message.toString(),Snackbar.LENGTH_LONG).show()
            }
    }

    fun registerPhoto(photo : Photo){

        val uid = Firebase.auth.uid
        val employeesPath = "users/${uid}/photos/${photo.photoId}"

        db.collection(employeesPath).add(photo)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root,R.string.correctRegisterPhoto, Snackbar.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

}