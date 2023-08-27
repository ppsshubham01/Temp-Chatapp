package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.databinding.ActivitySetupPtofileBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class SetupPtofileActivity : AppCompatActivity() {

    private val TAG: String=this.javaClass.simpleName
    private lateinit var binding: ActivitySetupPtofileBinding
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null
    private val STORAGE_PERMISSION_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupPtofileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this@SetupPtofileActivity)
        dialog!!.setMessage("Updating Profile")
        dialog!!.setCancelable(false)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        supportActionBar?.hide()

//an OnClickListener for the profile image view
        binding.UserImageView.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                openImagePicker()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),STORAGE_PERMISSION_CODE)
            }
        }
//Set an OnClickListener for the setup button
        binding.setupProfileBtn.setOnClickListener {
            val name: String = binding.nameBox.text.toString()
            if (name.isEmpty()) {
//                binding.nameBox.setError("Please type a name!")
                binding.nameBox.error = "Please type a name!"

            } else {
                dialog!!.show()
                if (selectedImage != null) {
                    val referece = storage!!.reference.child("Profile").child(auth!!.uid!!)
//                    val imageRef = storage!!.reference.child("Profile").child("MizPXgsflQVx4SAdymzSgKd5YdG3"+".jpg")
                    referece.putFile(selectedImage!!).addOnCompleteListener { task ->
                        if (task.isSuccessful) {


                            task.result.storage.downloadUrl.addOnCompleteListener { uri ->
                                val imagUrl = uri.result.toString()
                                val uid = auth!!.uid
                                val phone = auth!!.currentUser!!.phoneNumber
                                val enteredName: String = binding.nameBox.text.toString()
                                val user = User(uid,enteredName, phone, imagUrl)
//
                                Log.d(TAG, "onCreate: downloadUrl: $imagUrl")
                                Log.d(TAG, "onCreate: downloadUrl: ${task.result}")

                                database!!.reference.child("users").child(uid!!).setValue(user).addOnCompleteListener {
                                        dialog!!.dismiss()
                                        val intent = Intent(
                                            this@SetupPtofileActivity,
                                            MainActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
// Function to open the image picker
    private fun openImagePicker() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        startActivityForResult(intent, 45)
    }
// Handle permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Call super first
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                // Handle permission denial
            }
        }
    }

}