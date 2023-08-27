package com.example.chatapp

import android.app.ProgressDialog
import android.content.Intent
import android.inputmethodservice.InputMethodService
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.example.chatapp.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTP : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    var auth: FirebaseAuth?=null
    var verificationID:String?=null
    var dialog:ProgressDialog?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dialog = ProgressDialog(this@OTP)
        dialog!!.setMessage("Sending OTP...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        auth=FirebaseAuth.getInstance()
        supportActionBar?.hide()
        val phonenumber =intent.getStringExtra("PhoneNumber")
        binding.Phonenumber.text= "Verify $phonenumber"

        val options= PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phonenumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this@OTP)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }
                override fun onVerificationFailed(p0: FirebaseException) {
//                    Toast.makeText(this@OTP, "Verification failed: ${p0.message}", Toast.LENGTH_SHORT).show()
//                    dialog?.dismiss()
                }
                override fun onCodeSent(verifyID: String, forceResendToken: PhoneAuthProvider.ForceResendingToken) {

                    super.onCodeSent(verifyID, forceResendToken)
                    dialog!!.dismiss()
                    verificationID = verifyID
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as  InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED  ,0)
                    binding.otpView.requestFocus()
                }
            }).build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        binding.otpView.setOtpCompletionListener { otp ->

            val credential = PhoneAuthProvider.getCredential(verificationID!!, otp)
            auth!!.signInWithCredential(credential)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val intent = Intent(this@OTP, SetupPtofileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this@OTP, "Failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}