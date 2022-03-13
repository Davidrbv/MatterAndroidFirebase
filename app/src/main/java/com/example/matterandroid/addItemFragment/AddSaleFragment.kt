package com.example.matterandroid.addItemFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentEditSaleBinding
import com.example.matterandroid.entities.Sale
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class AddSaleFragment : Fragment() {

    private lateinit var binding: FragmentEditSaleBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var sale : Sale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditSaleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.addSale)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        binding.btnRegisterSale.setText(R.string.add)

        binding.apply {

            //Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigate(R.id.action_addSaleFragment_to_saleFragment)
            }

            //Add button
            btnRegisterSale.setOnClickListener {

                sale = Sale().apply {
                    saleId = System.currentTimeMillis().toString()
                    date = binding.editDate.text.toString().trim()
                    cash = binding.editCash.text.toString().toFloatOrNull()
                    card = binding.editCard.text.toString().toFloatOrNull()
                }

                if( !sale.date.isNullOrEmpty() &&
                    sale.cash !== null &&
                    sale.card !== null) registerSale(sale)
                else Snackbar.make(binding.root, R.string.fillFields, Snackbar.LENGTH_LONG).show()

            }
        }

        super.onResume()
    }

    fun registerSale(sale : Sale){

        val uid = Firebase.auth.uid
        val salesPath = "users/${uid}/sales"

        sale.total = sale.cash!! + sale.card!!

        db.collection(salesPath).document("${sale.saleId}").set(sale)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root,R.string.correctRegisterSale, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_addSaleFragment_to_saleFragment)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }


}