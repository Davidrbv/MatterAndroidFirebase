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
import com.example.matterandroid.adapters.EmployeeAdapter
import com.example.matterandroid.databinding.FragmentEmployeeBinding
import com.example.matterandroid.entities.Employee
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class EmployeeFragment : Fragment() {

    private lateinit var binding : FragmentEmployeeBinding
    private lateinit var db : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var employeeAdapter : EmployeeAdapter
    private val employees = mutableListOf<Employee>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmployeeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {

        db = FirebaseFirestore.getInstance()
        auth = Firebase.auth

        (activity as AppCompatActivity?)!!.supportActionBar!!.apply {
            title = getString(R.string.employee)
            setDisplayShowCustomEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(false)
        }

        getEmployees()

        binding.btnAddEmployee.setOnClickListener {
            findNavController().navigate(R.id.action_employeeFragment_to_addEmployeeFragment)
        }

        super.onResume()
    }

    /**
     * Recovery user's employees list
     */

    fun getEmployees(){

        val uid : String? = auth.uid
        val path = "users/${uid}/employees"

        db.collection(path).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for(employee in documents){
                        val actual = employee.toObject(Employee::class.java)
                        actual.employeeId = employee.id
                        employees.add(actual)
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

    fun deleteEmployee(employee : Employee,path : String){
        db.collection(path).document("${employee.employeeId}").delete()
    }

    fun drawList(path : String){
        val shortClickE : (MenuItem, Employee) -> Boolean = {
                item : MenuItem, employee : Employee ->

            when(item.itemId) {

                R.id.deleteItem -> {

                    val mp = MediaPlayer.create(this.requireContext(),R.raw.sound_delete)
                    mp.start()

                    MaterialAlertDialogBuilder(this.requireContext())
                        .setTitle(R.string.deleteEmployee)
                        .setMessage(R.string.sure)
                        .setPositiveButton("Yes"){d,i ->
                            val idEmployee = employees.indexOfFirst { it==(employee) }
                            employees.removeAt(idEmployee)
                            employeeAdapter.notifyItemRemoved(idEmployee)
                            deleteEmployee(employee,path)
                            Snackbar.make(binding.root,R.string.confirmDeleteEmployee,Snackbar.LENGTH_LONG).show()
                        }
                        .setNegativeButton("No"){d,i ->
                            Snackbar.make(binding.root,R.string.deleteCancel,Snackbar.LENGTH_LONG).show()
                        }
                        .show()
                    true
                }

                R.id.editItem -> {
                    val action = EmployeeFragmentDirections.actionEmployeeFragmentToEditEmployeeFragment(employee)
                    findNavController().navigate(action)
                    true
                }
            }
            false
        }

        employeeAdapter = EmployeeAdapter(employees).apply{
            shortClick = shortClickE
        }
        binding.employeeRecycler.apply {
            adapter = employeeAdapter
            setHasFixedSize(true)
        }
    }

}