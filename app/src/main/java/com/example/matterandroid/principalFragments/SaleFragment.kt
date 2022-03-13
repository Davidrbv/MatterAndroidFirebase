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
import com.example.matterandroid.adapters.SaleAdapter
import com.example.matterandroid.databinding.FragmentSaleBinding
import com.example.matterandroid.entities.Sale
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class SaleFragment : Fragment() {

    private lateinit var binding: FragmentSaleBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var saleAdapter : SaleAdapter
    private val sales = mutableListOf<Sale>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaleBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.sales)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        getSales()

        binding.btnAddSale.setOnClickListener {
            findNavController().navigate(R.id.action_saleFragment_to_addSaleFragment)
        }
        super.onResume()
    }

    /**
     * Recovery user's employees list
     */

    fun getSales(){

        val uid : String? = auth.uid
        val path = "users/${uid}/sales"

        db.collection(path).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for(sale in documents){
                        val actual = sale.toObject(Sale::class.java)
                        actual.saleId = sale.id
                        sales.add(actual)
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

    /**
     * Delete user's employee
     */

    fun deleteSale(sale : Sale,path : String){
        db.collection(path).document("${sale.saleId}").delete()
    }

    fun drawList(path : String){
        val shortClickE : (MenuItem, Sale) -> Boolean = {
                item : MenuItem, sale : Sale ->

            when(item.itemId) {

                R.id.deleteItem -> {
                    val mp = MediaPlayer.create(this.requireContext(),R.raw.sound_delete)
                    mp.start()
                    MaterialAlertDialogBuilder(this.requireContext())
                        .setTitle(R.string.deleteSale)
                        .setMessage(R.string.sure)
                        .setPositiveButton("Yes"){d,i ->
                            val idSale = sales.indexOfFirst { it==(sale) }
                            sales.removeAt(idSale)
                            saleAdapter.notifyItemRemoved(idSale)
                            deleteSale(sale,path)
                            Snackbar.make(binding.root,R.string.confirmDeleteSale,Snackbar.LENGTH_LONG).show()
                        }
                        .setNegativeButton("No"){d,i ->
                            Snackbar.make(binding.root,R.string.deleteCancel,Snackbar.LENGTH_LONG).show()
                        }
                        .show()
                    true
                }

                R.id.editItem -> {
                    val action = SaleFragmentDirections.actionSaleFragmentToEditSaleFragment(sale)
                    findNavController().navigate(action)
                    true
                }
            }
            false
        }

        saleAdapter = SaleAdapter(sales).apply{
            shortClick = shortClickE
        }
        binding.saleRecycler.apply {
            adapter = saleAdapter
            setHasFixedSize(true)
        }
    }

}