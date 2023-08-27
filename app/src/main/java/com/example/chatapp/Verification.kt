package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatapp.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class Verification : AppCompatActivity() {
    private lateinit var binding: ActivityVerificationBinding

    var auth:FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth=FirebaseAuth.getInstance()
        if(auth!!.currentUser!=null){
            val intent = Intent(this@Verification ,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        
        supportActionBar?.hide()
        binding.ETTypePhoneNumber.requestFocus()
        binding.SendOTPBtn.setOnClickListener {

            val intent=Intent(this@Verification, OTP::class.java)
            intent.putExtra("PhoneNumber", binding.ETTypePhoneNumber.text.toString())
            startActivity(intent)
        }

    }
}