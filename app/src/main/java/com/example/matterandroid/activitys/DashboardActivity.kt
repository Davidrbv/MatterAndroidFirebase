package com.example.matterandroid.activitys

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.matterandroid.R
import com.example.matterandroid.databinding.ActivityDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDashboardBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false);
        supportActionBar?.setDisplayShowHomeEnabled(false);

        auth = Firebase.auth

            //Set BottomBar
            val abc = AppBarConfiguration(setOf(R.id.panel,R.id.sale,R.id.invoice,R.id.employee))
            val navCtrl = findNavController(R.id.nav_host_dasboard)
            setupActionBarWithNavController(navCtrl,abc)

            binding.bottomNav.setupWithNavController(navCtrl)

            binding.bottomNav.setOnItemSelectedListener {
                when(it.itemId){
                    R.id.sale -> {
                        navCtrl.navigate(R.id.saleFragment)
                    }
                    R.id.invoice -> {
                        navCtrl.navigate(R.id.invoiceFragment)
                    }
                    R.id.employee-> {
                        navCtrl.navigate(R.id.employeeFragment)
                    }
                    R.id.panel -> {
                        navCtrl.navigate(R.id.panelFragment)
                    }
                    R.id.galery -> {
                        navCtrl.navigate(R.id.galeryFragment)
                    }
                    else -> false
                }
                true
            }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_app_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {

            R.id.exit -> {
                auth.signOut()
                finish()
                true
            }

            R.id.deleteAccount -> {

                val mp = MediaPlayer.create(this,R.raw.sound_delete_account)
                mp.start()

                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.deleteUser)
                    .setMessage(R.string.sure)
                    .setPositiveButton("Yes?"){d,i ->
                        val mp = MediaPlayer.create(this.applicationContext,R.raw.abuse)
                        mp.start()
                        val user = FirebaseAuth.getInstance().currentUser;
                        if(user !== null){

                            val uid = auth.uid
                            db = FirebaseFirestore.getInstance()
                            db.collection("/users").document("/${uid}").delete()
                                .addOnCompleteListener() {
                                    if(it.isSuccessful){
                                        user.delete()
                                        Snackbar.make(binding.root,R.string.confirmDeleteUser,Snackbar.LENGTH_LONG).show()
                                    }else Snackbar.make(binding.root,"A error success..",Snackbar.LENGTH_LONG).show()
                                }
                            finish()
                        }
                    }
                    .setNegativeButton("No"){d,i ->
                        val mp = MediaPlayer.create(this.applicationContext,R.raw.applause)
                        mp.start()
                        Snackbar.make(binding.root,R.string.deleteCancel,Snackbar.LENGTH_LONG).show()
                    }
                    .show()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
    }
}