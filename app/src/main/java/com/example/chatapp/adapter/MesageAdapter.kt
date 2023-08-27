package com.example.chatapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.R
import com.example.chatapp.databinding.DeleteLayoutBinding
import com.example.chatapp.databinding.SendMsgBinding
import com.example.chatapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MesageAdapter(var context: Context, messages: ArrayList<Message>?, senderRoom: String, receiverRoom: String) :
     RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var message: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    val sendRoom: String
    val receiverRoom: String

    inner class SendMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }
    init {
        if (messages != null) {
            this.message = messages
        }
        this.sendRoom = senderRoom
        this.receiverRoom = receiverRoom
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SendMsgHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(
                R.layout.receive_msg, parent, false
            )
            ReceiveMsgHolder(view)
        }
    }
    override fun getItemViewType(position: Int): Int {

        val message = message[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = message[position]
        if (holder.javaClass == SendMsgHolder::class.java) {
//................................................................
            val viewHolder = holder as SendMsgHolder
            if (message.message.equals("photo")) {

                viewHolder.binding.imagePlaceHolder.visibility = View.VISIBLE
                viewHolder.binding.sendMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.icimage_placeholder)
                    .into(viewHolder.binding.imagePlaceHolder)
            }
            viewHolder.binding.sendMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("This is...")
                    .setView(binding.root)
                    .create()

                binding.everyoneDelete.setOnClickListener {

//................. Delete message logic here
                    message.message = "This message is removed"
                    message.messageId?.let { messageId ->
                        val chatReference = FirebaseDatabase.getInstance().reference.child("chats")
                        chatReference.child(sendRoom).child("message").child(messageId)
                            .setValue(message)
                        chatReference.child(receiverRoom).child("message").child(messageId)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.forMeDelete.setOnClickListener {

                    message.messageId?.let { messageId ->
                        val chatReference = FirebaseDatabase.getInstance().reference.child("chats")
                        chatReference.child(sendRoom).child("message").child(messageId)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()

                false
            }
        }
//................................................................
        else {
//................................................................
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {


                viewHolder.binding.imagePlaceHolder.visibility = View.VISIBLE
                viewHolder.binding.sendMessage.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.icimage_placeholder)
                    .into(viewHolder.binding.imagePlaceHolder)
            }
            viewHolder.binding.sendMessage.text = message.message
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setMessage("This is...")
                    .setView(binding.root)
                    .create()

                binding.everyoneDelete.setOnClickListener {

//................. Delete message logic here
                    message.message = "This message is removed"
                    message.messageId?.let { messageId ->
                        val chatReference = FirebaseDatabase.getInstance().reference.child("chats")
                        chatReference.child(sendRoom).child("message").child(messageId)
                            .setValue(message)
                        chatReference.child(receiverRoom).child("message").child(messageId)
                            .setValue(message)
                    }
                    dialog.dismiss()
                }
                binding.forMeDelete.setOnClickListener {

                    message.messageId?.let { messageId ->
                        val chatReference = FirebaseDatabase.getInstance().reference.child("chats")
                        chatReference.child(sendRoom).child("message").child(messageId)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener { dialog.dismiss() }
                dialog.show()
                false
            }
        }
//................................................................
    }


    override fun getItemCount(): Int = message.size




}