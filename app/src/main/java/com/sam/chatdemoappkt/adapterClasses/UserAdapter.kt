package com.sam.chatdemoappkt.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sam.chatdemoappkt.MessageChatActivity
import com.sam.chatdemoappkt.R
import com.sam.chatdemoappkt.VisitUserProfileActivity
import com.sam.chatdemoappkt.modelClasses.Chat
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: Context, mUsers: List<Users>, isChatCheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder?>() {
    private val mContext: Context
    private val mUser: List<Users>
    private var isChatCheck: Boolean
    var lastMsg: String = ""

    init {
        this.mUser = mUsers
        this.mContext = mContext
        this.isChatCheck = isChatCheck
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        val view: View = LayoutInflater.from(mContext)
            .inflate(R.layout.user_search_item_layout, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user: Users? = mUser[position]
        holder.userNameTxt.text = user!!.getUserName()
        Picasso.get().load("gs://chatappkt-48d92.appspot.com/download.png")
            .placeholder(R.drawable.ic_person).into(holder.profileImageView)

        if (isChatCheck) {
            retriveLastMessage(user.getUID(), holder.lastMessageTxt)
        } else {
            holder.lastMessageTxt.visibility = View.GONE
        }

        if (isChatCheck) {
            if (user.getStatus() == "online") {
                holder.onlineImageView.visibility = View.VISIBLE
                holder.offlineImageView.visibility = View.GONE
            } else {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.VISIBLE
            }
        } else {
            holder.onlineImageView.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What Do You Want?")
            builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                if (position == 0) {
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
                if (position == 1){
                    val intent = Intent(mContext, VisitUserProfileActivity::class.java)
                    intent.putExtra("visit_id", user.getUID())
                    mContext.startActivity(intent)
                }
            })
            builder.show()
        }
    }

    private fun retriveLastMessage(chatUserId: String?, lastMessageTxt: TextView) {
        lastMsg = "defaultMsg"
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference
            .child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (datasnapshot in p0.children) {
                    val chat = datasnapshot.getValue(Chat::class.java)
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver() == firebaseUser!!.uid
                            && chat.getSender() == chatUserId
                            || chat.getReceiver() == chatUserId
                            && chat.getSender() == firebaseUser!!.uid
                        ) {
                            lastMsg = chat.getMessage()!!
                        }
                    }
                }
                when(lastMsg){
                    "defaultMsg" -> lastMessageTxt.text = "no Message"
                    "hell" -> lastMessageTxt.text = "image sent"
                    else -> lastMessageTxt.text = lastMsg
                }
                lastMsg = "defaultMsg"
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getItemCount(): Int {
        return mUser!!.size ?: 0
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameTxt: TextView
        val profileImageView: CircleImageView
        val onlineImageView: CircleImageView
        val offlineImageView: CircleImageView
        val lastMessageTxt: TextView

        init {
            userNameTxt = itemView.findViewById(R.id.user_name_search)
            profileImageView = itemView.findViewById(R.id.profile_image_search)
            onlineImageView = itemView.findViewById(R.id.image_search_online)
            offlineImageView = itemView.findViewById(R.id.image_search_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)
        }

    }
}