package com.example.matterandroid.addItemFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentEditInvoiceBinding
import com.example.matterandroid.entities.Invoice
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class AddInvoiceFragment : Fragment() {

    private lateinit var binding : FragmentEditInvoiceBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var invoice : Invoice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditInvoiceBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.addInvoice)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        binding.btnRegisterInvoice.setText(R.string.add)

        binding.apply {

            //Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigate(R.id.action_addInvoiceFragment_to_invoiceFragment)
            }

            //Add button
            btnRegisterInvoice.setOnClickListener {

                invoice = Invoice().apply {
                    invoiceId = System.currentTimeMillis().toString()
                    provider = binding.editProvider.text.toString().trim()
                    date = binding.editDate.text.toString().trim()
                    quantity = binding.editQuantity.text.toString().toIntOrNull()
                    state = binding.editStateBox.isChecked
                }

                if( !invoice.provider.isNullOrEmpty() &&
                    !invoice.date.isNullOrEmpty() &&
                    invoice.quantity != null &&
                    invoice.state != null) registerInvoice(invoice)
                else Snackbar.make(binding.root, R.string.fillFields, Snackbar.LENGTH_LONG).show()

            }
        }
        super.onResume()
    }

    fun registerInvoice(invoice : Invoice){

        val uid = Firebase.auth.uid
        val invoicesPath = "users/${uid}/invoices"

        db.collection(invoicesPath).document("${invoice.invoiceId}").set(invoice)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root,R.string.correctRegisterInvoice, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_addInvoiceFragment_to_invoiceFragment)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

}