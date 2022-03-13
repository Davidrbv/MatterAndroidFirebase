package com.example.matterandroid.principalFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        //Firebase user
        auth = Firebase.auth

        //Buttons listeners
        binding.btnLogin.setOnClickListener { login() }
        binding.btnRegister.setOnClickListener { register() }

        super.onResume()
    }

    /**
     * Register user
     */

    fun register(){
        findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
    }

    /**
     * Login user
     */

    fun login(){

        val email : String = binding.email.text.toString().trim()
        val pass : String = binding.password.text.toString().trim()

        if(email != "" && pass != ""){
            auth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(){
                    if(it.isSuccessful) {
                        findNavController().navigate(R.id.action_loginFragment_to_dashboard)
                    }else Snackbar.make(binding.root, R.string.err_login, Snackbar.LENGTH_LONG).show()
                }
        }else Snackbar.make(binding.root, R.string.fillFields, Snackbar.LENGTH_LONG).show()
    }
}