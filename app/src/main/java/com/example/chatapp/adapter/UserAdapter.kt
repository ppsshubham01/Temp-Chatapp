package com.example.chatapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatapp.Chatlayout
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemProfileBinding
import com.example.chatapp.model.User

class UserAdapter(var context: Context, var userList: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

//..............inner class UserViewHolder that extends RecyclerView.ViewHolder.
// .............It holds references to the views in your list item layout using data binding.
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemProfileBinding = ItemProfileBinding.bind(itemView)

    }
//.................his method is called when the RecyclerView needs a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }
//.................This method binds data to the views of the ViewHolder. It gets called for each item in the list.
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.binding.username.text = user.name
        Log.d("Profilee", user.toString())
        Log.d("ProfileImageURL", user.profileImage.toString())
        Glide.with(context).load(user.profileImage).placeholder(R.drawable.user)
            .into(holder.binding.profile)
        holder.itemView.setOnClickListener {
            val intent = Intent(context,Chatlayout::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }
//.................This method returns the number of items in the list.
    override fun getItemCount(): Int = userList.size
}