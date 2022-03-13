package com.example.matterandroid.principalFragments

import android.media.MediaPlayer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.matterandroid.R
import com.example.matterandroid.adapters.InvoiceAdapter
import com.example.matterandroid.databinding.FragmentInvoiceBinding
import com.example.matterandroid.entities.Invoice
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class InvoiceFragment : Fragment() {

    private lateinit var  binding : FragmentInvoiceBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var invoiceAdapter : InvoiceAdapter
    private val invoices = mutableListOf<Invoice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInvoiceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth


        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.invoices)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        getAllInvoices()

        binding.btnAddInvoice.setOnClickListener {
            findNavController().navigate(R.id.action_invoiceFragment_to_addInvoiceFragment)
        }
        super.onResume()
    }

    /**
     * Recovery user's invoices list
     */
    fun getAllInvoices(){

        val uid : String? = auth.uid
        val path = "users/${uid}/invoices"

        db.collection(path).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for(invoice in documents){
                        val actual = invoice.toObject(Invoice::class.java)
                        actual.invoiceId = invoice.id
                        invoices.add(actual)
                    }
                    drawList(path)
                } else {
                    Snackbar.make(binding.root, R.string.without,Snackbar.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Snackbar.make(binding.root,exception.message.toString(),Snackbar.LENGTH_LONG).show()
            }
    }

    fun deleteInvoice(invoice : Invoice,path : String){
        db.collection(path).document("${invoice.invoiceId}").delete()
    }

    fun drawList(path : String){
        val shortClickE : (MenuItem, Invoice) -> Boolean = {
                item : MenuItem, invoice : Invoice ->

            when(item.itemId) {

                R.id.deleteItem -> {

                    val mp = MediaPlayer.create(this.requireContext(),R.raw.sound_delete)
                    mp.start()

                    MaterialAlertDialogBuilder(this.requireContext())
                        .setTitle(R.string.deleteInvoice)
                        .setMessage(R.string.sure)
                        .setPositiveButton("Yes"){d,i ->
                            val idInvoice = invoices.indexOfFirst { it==(invoice) }
                            invoices.removeAt(idInvoice)
                            invoiceAdapter.notifyItemRemoved(idInvoice)
                            deleteInvoice(invoice,path)
                            Snackbar.make(binding.root,R.string.confirmDeleteInvoice,Snackbar.LENGTH_LONG).show()
                        }
                        .setNegativeButton("No"){d,i ->
                            Snackbar.make(binding.root,R.string.deleteCancel,Snackbar.LENGTH_LONG).show()
                        }
                        .show()
                    true
                }

                R.id.editItem -> {
                    val action = InvoiceFragmentDirections.actionInvoiceFragmentToEditInvoiceFragment(invoice)
                    findNavController().navigate(action)
                    true
                }
            }
            false
        }

        invoiceAdapter = InvoiceAdapter(invoices).apply{
            shortClick = shortClickE
        }
        binding.invoiceRecycler.apply {
            adapter = invoiceAdapter
            setHasFixedSize(true)
        }
    }

}