package com.example.matterandroid.editItemFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentEditSaleBinding
import com.example.matterandroid.entities.Sale
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class EditSaleFragment : Fragment() {

    private lateinit var binding : FragmentEditSaleBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var sale : Sale
    private val arguments : EditSaleFragmentArgs by navArgs<EditSaleFragmentArgs>()

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
            title = getString(R.string.editSale)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        sale = arguments.sale

        binding.btnRegisterSale.setText(R.string.edit)

        binding.apply {
            editDate.setText(sale.date)
            editCard.setText(sale.card.toString())
            editCash.setText(sale.cash.toString())
        }

        binding.apply {

            //Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigate(R.id.action_editSaleFragment_to_saleFragment)
            }

            //Update button
            btnRegisterSale.setOnClickListener {

                    sale.apply {
                        date = binding.editDate.text.toString().trim()
                        cash = binding.editCash.text.toString().toFloatOrNull()
                        card = binding.editCard.text.toString().toFloatOrNull()
                    }

                if( !sale.date.isNullOrEmpty() &&
                    sale.cash !== null &&
                    sale.card !== null) updateSale(sale)
                else Snackbar.make(binding.root, R.string.fillFields, Snackbar.LENGTH_LONG).show()
            }
        }
        super.onResume()
    }


    fun updateSale(sale: Sale){

        val uid = Firebase.auth.uid
        val salesPath = "users/${uid}/sales"

        sale.total = sale.cash!! + sale.card!!

        db.collection(salesPath).document("${sale.saleId}").delete()
        db.collection(salesPath).document("${sale.saleId}").set(sale)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root, R.string.correctModifySale, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_editSaleFragment_to_saleFragment)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

}