package com.example.chatapp

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapter.UserAdapter
import com.example.chatapp.databinding.ActivityMainBinding
import com.example.chatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var auth: FirebaseAuth? = null
    var database: FirebaseDatabase? = null
    var users: ArrayList<User>? = null
    var userAdapter: UserAdapter? = null
    var dialog: ProgressDialog? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//.................... ProgressDialog setup
        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Updating Images...")
        dialog!!.setCancelable(false)
// .....................Firebase setup
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
//...................... RecyclerView setup
        users = ArrayList<User>()
        userAdapter = UserAdapter(this@MainActivity, users!!)
        val layoutManager = LinearLayoutManager(this@MainActivity)
        binding.VerticalRV.layoutManager = layoutManager

//...................... Getting the current user's info from Firebase
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)

                }
                override fun onCancelled(error: DatabaseError) {}
            })

//......................... Setting up the RecyclerView adapter and populating user data
        binding.VerticalRV.adapter = userAdapter
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    users!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val user: User? = snapshot1.getValue(User::class.java)

                        if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                    }
//........................ Notify adapter of data changes
                    userAdapter!!.notifyItemInserted(users!!.size - 1)
                }
            }
            override fun onCancelled(error: DatabaseError) {  }
        })
    }
//...............These methods are used to manage the user's online/offline status for Firebase only
    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }
}