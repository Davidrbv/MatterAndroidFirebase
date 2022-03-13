package com.example.matterandroid.addItemFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentEditEmployeeBinding
import com.example.matterandroid.entities.Employee
import com.example.matterandroid.entities.Photo
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream


class AddEmployeeFragment : Fragment() {

    private lateinit var binding : FragmentEditEmployeeBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var storage : FirebaseStorage
    private lateinit var employee : Employee
    private lateinit var auth : FirebaseAuth
    private lateinit var photo : Photo

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
            val path = "${userUid}/userEmployeesPhotos/${photoUid}"

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
                            photo = Photo().apply {
                                employee.image = downloadUri
                            }
                            Glide
                                .with(binding.root)
                                .load(downloadUri)
                                .centerCrop()
                                .placeholder(R.drawable.employees)
                                .into(binding.employeeImage)
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
                            photo = Photo().apply {
                                employee.image = downloadUri
                            }
                            Glide
                                .with(binding.root)
                                .load(downloadUri)
                                .centerCrop()
                                .placeholder(R.drawable.employees)
                                .into(binding.employeeImage)
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
        binding = FragmentEditEmployeeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        employee = Employee()

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.addEmployee)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        Glide
            .with(binding.root)
            .load(R.drawable.employees)
            .centerCrop()
            .circleCrop()
            .placeholder(R.drawable.employees)
            .into(binding.employeeImage)

        binding.btnRegisterEmployee.setText(R.string.add)

        binding.apply {

            //Employee image listener
            //Pick image with camera.
            employeeImage.setOnClickListener {
                if (ContextCompat.checkSelfPermission(this@AddEmployeeFragment.requireContext(),
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    launchCamera.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                }
                else{
                    requestPermission.launch(Manifest.permission.CAMERA)
                }

            }

            //Pick image file
            employeeImage.setOnLongClickListener {
                launchExplorer.launch("image/*")
                true
            }

            //Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigate(R.id.action_addEmployeeFragment_to_employeeFragment)
            }

            //Add button
            btnRegisterEmployee.setOnClickListener {

                employee.apply {
                    name = binding.editName.text.toString().trim()
                    surname = binding.editSurname.text.toString().trim()
                    salary = binding.editSalary.text.toString().toIntOrNull()
                    email = binding.editEmail.text.toString().trim()
                    job = binding.editJob.text.toString().trim()
                    genre = binding.editGenre.text.toString().trim()
                    employeeId = System.currentTimeMillis().toString()
                }

                if( !employee.name.isNullOrEmpty() &&
                    !employee.surname.isNullOrEmpty() &&
                    employee.salary != null &&
                    !employee.email.isNullOrEmpty() &&
                    !employee.job.isNullOrEmpty() &&
                    !employee.genre.isNullOrEmpty()) registerEmployee(employee)
                else Snackbar.make(binding.root, R.string.fillFields,Snackbar.LENGTH_LONG).show()

            }
        }

        super.onResume()
    }

    fun registerEmployee(employee : Employee){

        val uid = Firebase.auth.uid
        val employeesPath = "users/${uid}/employees"

        db.collection(employeesPath).document("${employee.employeeId}").set(employee)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root,R.string.correctRegisterEmployee, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_addEmployeeFragment_to_employeeFragment)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

}