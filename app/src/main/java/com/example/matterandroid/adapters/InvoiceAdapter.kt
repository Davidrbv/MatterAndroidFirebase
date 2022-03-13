package com.example.matterandroid.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matterandroid.R
import com.example.matterandroid.databinding.InvoiceItemBinding
import com.example.matterandroid.entities.Invoice

class InvoiceAdapter(var invoices : MutableList<Invoice>) : RecyclerView.Adapter<InvoiceAdapter.InvoiceContainer>() {


    //ShorClick listener
    var shortClick: (MenuItem, Invoice) -> Boolean ={ menuItem : MenuItem, invoice : Invoice -> false }
        set(value){
            field = value
        }

    /**
     * Override de métodos del RecyclerView
     */

    //items count
    override fun getItemCount() : Int = invoices.size

    //Create container
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceContainer {
        val inflador = LayoutInflater.from(parent.context)
        val binding = InvoiceItemBinding.inflate(inflador,parent,false)
        return InvoiceContainer(binding)
    }

    //Associate container to invoice item
    override fun onBindViewHolder(holder: InvoiceContainer, position: Int) {
        holder.bindInvoice(invoices[position])
    }


    /**
     * Employees Container inner class
     */

    inner class InvoiceContainer (val binding : InvoiceItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindInvoice ( invoice : Invoice ){

            //Show invoice
            binding.invoiceProvider.text = invoice.provider
            binding.invoiceDate.text = invoice.date
            binding.invoiceQuantity.text = invoice.quantity.toString()
            if(invoice.state!!) {
                Glide
                    .with(binding.root)
                    .load(R.drawable.paid)
                    .centerCrop()
                    .circleCrop()
                    .placeholder(R.drawable.invoices)
                    .into(binding.invoiceImage)
                binding.inCard.background = ColorDrawable(Color.parseColor("#AFFF19"))
            }else{
                Glide
                    .with(binding.root)
                    .load(R.drawable.unpaid)
                    .centerCrop()
                    .circleCrop()
                    .placeholder(R.drawable.invoices)
                    .into(binding.invoiceImage)
                binding.inCard.background = ColorDrawable(Color.parseColor("#FF8D75"))
            }

            //shortClick listener
            binding.root.setOnClickListener {

                val menu = PopupMenu(binding.root.context, binding.invoiceProvider)
                menu.inflate(R.menu.menu_items)
                menu.setOnMenuItemClickListener { shortClick(it,invoice) }
                menu.show()

                true
            }

        }
    }
}