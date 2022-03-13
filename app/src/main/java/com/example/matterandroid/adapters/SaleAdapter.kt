package com.example.matterandroid.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.matterandroid.R
import com.example.matterandroid.databinding.SaleItemBinding
import com.example.matterandroid.entities.Sale

class SaleAdapter(var sales : MutableList<Sale>) : RecyclerView.Adapter<SaleAdapter.SaleContainer>() {

    var shortClick: (MenuItem, Sale) -> Boolean = { menuItem, sale -> false }
        set(value) {
            field = value
        }

    /**
     * Override Recycler Methods
     */

    override fun getItemCount(): Int = sales.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleContainer {
        val inflador = LayoutInflater.from(parent.context)
        val binding = SaleItemBinding.inflate(inflador, parent, false)
        return SaleContainer(binding)
    }

    override fun onBindViewHolder(holder: SaleContainer, position: Int) {
        holder.bindSale(sales[position])
    }


    /**
     * Sales container
     */
    inner class SaleContainer(val binding: SaleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindSale(sale: Sale) {

            //Show sale
            binding.saleDate.text = sale.date
            binding.saleCash.text = sale.cash.toString()
            binding.saleCard.text = sale.card.toString()
            binding.saleQuantity.text = sale.total.toString()

            if(sale.total!! <= 500f){
                binding.seCard.background = ColorDrawable(Color.parseColor("#FD4500"))
            }else if(sale.total!! > 500f && sale.total!! <= 1500f){
                binding.seCard.background = ColorDrawable(Color.parseColor("#00D7FD"))
            }else binding.seCard.background = ColorDrawable(Color.parseColor("#AFFF19"))

            //Short click listener
            binding.root.setOnClickListener {
                val menu = PopupMenu(binding.root.context, binding.saleDate)
                menu.inflate(R.menu.menu_items)
                menu.setOnMenuItemClickListener { shortClick(it, sale) }
                menu.show()
                true
            }
        }
    }
}