package com.example.matterandroid.principalFragments

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentRegisterBinding
import com.example.matterandroid.entities.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class RegisterFragment : Fragment() {

    private lateinit var binding : FragmentRegisterBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRegisterBinding.inflate(inflater,container,false)
        return binding.root
    }

    /**
     * Initial code
     */
    override fun onResume() {
        super.onResume()

        db = FirebaseFirestore.getInstance()

        val mp = MediaPlayer.create(this.requireContext(),R.raw.welcome)
        mp.start()

        binding.btnRegister.setOnClickListener { register(it) }
    }

    fun register(view : View){

        val email: String = binding.email.text.toString().trim()
        val pass : String = binding.password.text.toString().trim()
        val rpass : String = binding.password2.text.toString().trim()
        val name : String = binding.nombre.text.toString().trim()
        val surname : String = binding.surname.text.toString().trim()


        if ((email.isNotEmpty()) && (pass.isNotEmpty()) && (rpass.isNotEmpty()) && (name.isNotEmpty()) && (surname.isNotEmpty())) {
            if(rpass == pass){
                auth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(){
                        if (it.isSuccessful) {
                            val userId = System.currentTimeMillis().toString()
                            val user = User(userId,name,surname,email,pass,rpass)
                            registerUserData(user)
                            Snackbar.make(binding.root,R.string.correctRegisterUser,Snackbar.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        }else {
                            Snackbar.make(binding.root, R.string.err_register,Snackbar.LENGTH_LONG).show()
                        }
                    }
            }else{
                Snackbar.make(view, R.string.diferent_pass, Snackbar.LENGTH_LONG).show()
            }
        }else {
            Snackbar.make(view, R.string.fillFields, Snackbar.LENGTH_LONG).show()
        }
    }

    fun registerUserData(user : User){

        val uid = Firebase.auth.uid
        val userPath = "users/${uid}/userData"

        db.collection(userPath).document("${user.userId}").set(user)
            .addOnSuccessListener { docRef ->
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }
}