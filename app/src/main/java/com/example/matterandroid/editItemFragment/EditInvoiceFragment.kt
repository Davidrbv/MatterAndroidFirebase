package com.example.matterandroid.editItemFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.matterandroid.R
import com.example.matterandroid.databinding.FragmentEditInvoiceBinding
import com.example.matterandroid.entities.Invoice
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class EditInvoiceFragment : Fragment() {

    private lateinit var binding : FragmentEditInvoiceBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var invoice : Invoice
    private val arguments : EditInvoiceFragmentArgs by navArgs<EditInvoiceFragmentArgs>()

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


        invoice = arguments.invoice

        binding.btnRegisterInvoice.setText(R.string.edit)


        binding.apply {
            editProvider.setText(invoice.provider)
            editDate.setText(invoice.date)
            editQuantity.setText(invoice.quantity.toString())
            editStateBox.isChecked = invoice.state!!
        }

        binding.apply {

            //Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigate(R.id.action_editInvoiceFragment_to_invoiceFragment)
            }

            //Update button
            btnRegisterInvoice.setOnClickListener {
                //Todo:Ver estado de factura para setear valor con un toogle
                invoice.apply {
                    provider = binding.editProvider.text.toString().trim()
                    date = binding.editDate.text.toString().trim()
                    quantity = binding.editQuantity.text.toString().toIntOrNull()
                    state = binding.editStateBox.isChecked
                }

                if( !invoice.provider.isNullOrEmpty() &&
                    !invoice.date.isNullOrEmpty() &&
                    invoice.quantity != null &&
                    invoice.state != null) updateInvoice(invoice)
                else Snackbar.make(binding.root, R.string.fillFields, Snackbar.LENGTH_LONG).show()

            }
        }
        super.onResume()
    }


    fun updateInvoice(invoice: Invoice){

        val uid = Firebase.auth.uid
        val invoicesPath = "users/${uid}/invoices"

        db.collection(invoicesPath).document("${invoice.invoiceId}").delete()
        db.collection(invoicesPath).document("${invoice.invoiceId}").set(invoice)
            .addOnSuccessListener { docRef ->
                Snackbar.make(binding.root, R.string.correctModifyInvoice, Snackbar.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_editInvoiceFragment_to_invoiceFragment)
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root,e.message.toString(), Snackbar.LENGTH_LONG).show()
            }
    }

}