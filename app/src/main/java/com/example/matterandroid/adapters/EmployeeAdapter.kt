package com.example.matterandroid.adapters

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.matterandroid.R
import com.example.matterandroid.databinding.EmployeeItemBinding
import com.example.matterandroid.entities.Employee

class EmployeeAdapter(var employees : MutableList<Employee>) : RecyclerView.Adapter<EmployeeAdapter.EmployeeContainer>() {


    var shortClick: (MenuItem, Employee) -> Boolean ={ menuItem : MenuItem, invoice : Employee -> false }
        set(value){
            field = value
        }

    /**
     * Override RecyclerView Methods
     */

    //Elements count
    override fun getItemCount() : Int = employees.size

    //Create container
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeContainer {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EmployeeItemBinding.inflate(inflater,parent,false)
        return EmployeeContainer(binding)
    }

    //Associate container to employee item
    override fun onBindViewHolder(holder: EmployeeContainer, position: Int) {
        holder.bindEmployee(employees[position])
    }


    /**
     * Employees Container inner class
     */

    inner class EmployeeContainer (val binding : EmployeeItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bindEmployee ( employee : Employee ){

            //Show employee
            binding.employeeName.text = employee.name
            binding.employeeSurname.text = employee.surname
            binding.employeeJob.text = employee.job
            binding.employeeGenre.text = employee.genre
            binding.employeeSalary.text = employee.salary.toString()
            Glide
                .with(binding.root)
                .load(employee.image)
                .centerCrop()
                .circleCrop()
                .placeholder(R.drawable.employees)
                .into(binding.employeeImage);


            //Listener shortClick
            binding.root.setOnClickListener {

                val menu = PopupMenu(binding.root.context, binding.employeeName)
                menu.inflate(R.menu.menu_items)
                menu.setOnMenuItemClickListener { shortClick(it,employee) }
                menu.show()

                true
            }

        }
    }
}