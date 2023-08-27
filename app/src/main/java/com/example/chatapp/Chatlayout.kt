package com.example.chatapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chatapp.adapter.MesageAdapter
import com.example.chatapp.databinding.ActivityChatLayoutBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class Chatlayout : AppCompatActivity() {

    private lateinit var binding: ActivityChatLayoutBinding
    private lateinit var adapter: MesageAdapter
    private val messages: ArrayList<Message> = ArrayList()
    private var sendRoom: String? = null
    private var receiveRoom: String? = null
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var dialog: ProgressDialog
    private var sendUID: String? = null
    private var receiverUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityChatLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

//...................................        messages=ArrayList()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@Chatlayout)
        dialog.setMessage("Uploading Image...!")
        dialog.setCancelable(false)
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding.name.text = name

//...... Loading profile image using Glide
        Glide.with(this).load(profile)
            .placeholder(R.drawable.icimage_placeholder)
            .into(binding.profile01)

        binding.imageViewBack.setOnClickListener { finish() }

//....... Getting user UIDs from the intent
        receiverUID = intent.getStringExtra("uid")
        sendUID = FirebaseAuth.getInstance().uid
//....... Retrieving user's presence status from Firebase
        database.reference.child("Presence").child(receiverUID!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline") {
                            binding.status.visibility = View.GONE
                        } else {
                            binding.status.text = status
                            binding.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

// ........ Creating chat room IDs
        sendRoom = sendUID + receiverUID
        receiveRoom = receiverUID + sendUID

// ........Initializing message adapter and setting up RecyclerView
        adapter = MesageAdapter(this@Chatlayout, messages, sendRoom!!, receiveRoom!!)
        binding.ChatRecyclerView.layoutManager = LinearLayoutManager(this@Chatlayout)
        binding.ChatRecyclerView.adapter = adapter
// ........Listening for changes in the messages database
        database.reference.child("chats")
            .child(sendRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")

                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    for (snapshot1 in snapshot.children) {


                        val message: Message? = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messages.add(message)
                    }
                    adapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled
                }


            })
        // Sending a text message
        binding.msgSentBtn.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(messageId = null, message = messageTxt, senderId = sendUID, imageUrl = "", timestamp = date.time)
//.............................................................clearing the msgBox
            binding.messageBox.setText("")
//............................................................. Generating a random key for the message
            val randomKey = database.reference.push().key
//............................................................. Updating last message and time in the chat rooms
            val lastMsgOBJ = HashMap<String, Any>()
            lastMsgOBJ["lastMsg"] = message.message!!
            lastMsgOBJ["lastMsgTime"] = date.time

            database.reference.child("chats").child(sendRoom!!).updateChildren(lastMsgOBJ)
            database.reference.child("chats").child(receiveRoom!!).updateChildren(lastMsgOBJ)

// .............................................................Storing the message in the sender and receiver chat rooms

            database.reference.child("chats").child(sendRoom!!).child("messages").child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database.reference.child("chats").child(receiveRoom!!).child("message")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener {
//.............................................................. Message sent successfully
                        }

                }
        }
//..Attaching an image
        binding.attachment.setOnClickListener {

            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 2)

        }
//.. Handling user typing status
        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")


            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                TODO("Not yet implemented")
            }

            override fun afterTextChanged(p0: Editable?) {
                // Updating user's typing status to "typing..."
                database.reference.child("Presence").child(sendUID!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                // Delaying the update of typing status to "Online"
                handler.postDelayed(userStoppedTyping, 1000)
            }
            // Runnable to set user's status to "Online" after typing stops
            var userStoppedTyping = Runnable {
                database.reference.child("Presence")
                    .child(sendUID!!)
                    .setValue("Online")
            }

        })
//..hiding actionBar Title
        supportActionBar?.setDisplayShowTitleEnabled(false)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedimage = data.data
                    val calendar = Calendar.getInstance()
                    val refence = storage.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog.show()
                    refence.putFile(selectedimage!!).addOnCompleteListener { task ->
                        dialog.dismiss()
                        if (task.isSuccessful) {
                            refence.downloadUrl.addOnSuccessListener { uri ->

                                val filePath = uri.toString()
                                val messageTxt: String = binding.messageBox.text.toString()
                                val date = Date()
//                                val message = Message(messageTxt, sendUID, date.time)
                                val message = Message(messageId = null, message = messageTxt, senderId = sendUID, imageUrl = "", timestamp = date.time)

                                message.message = "photo"
                                message.imageUrl = filePath
                                binding.messageBox.setText("")
                                val randomKey = database.reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.message!!
                                lastMsgObj["lastMsgTime"] = date.time
                                database.reference.child("chats").updateChildren(lastMsgObj)
                                database.reference.child("chats").child(receiveRoom!!)
                                    .updateChildren(lastMsgObj)
                                database.reference.child("chats").child(sendRoom!!)
                                    .child("messages").child(randomKey!!)
                                    .setValue(message).addOnSuccessListener {

                                        database.reference.child("chats").child(receiveRoom!!)
                                            .child("message").child(randomKey)
                                            .setValue(message).addOnSuccessListener {

                                            }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currenId = FirebaseAuth.getInstance().uid
        database.reference.child("Presence")
            .child(currenId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currenId = FirebaseAuth.getInstance().uid
        database.reference.child("Presence")
            .child(currenId!!)
            .setValue("offline")
    }
}
