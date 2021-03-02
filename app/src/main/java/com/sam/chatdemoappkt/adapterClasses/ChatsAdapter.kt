package com.sam.chatdemoappkt.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.sam.chatdemoappkt.R
import com.sam.chatdemoappkt.ViewFullImageActivity
import com.sam.chatdemoappkt.WelcomeActivity
import com.sam.chatdemoappkt.modelClasses.Chat
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatsAdapter(
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String
) : RecyclerView.Adapter<ChatsAdapter.MyViewHolder>() {
    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mContext = mContext
        this.imageUrl = imageUrl
        this.mChatList = mChatList
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var profileImg: CircleImageView? = null
        var showTextMessage: TextView? = null
        var leftImageView: ImageView? = null
        var textSeen: TextView? = null
        var rightImageView: ImageView? = null

        init {
            profileImg = itemView.findViewById(R.id.profile_image_message)
            showTextMessage = itemView.findViewById(R.id.show_txt_message)
            leftImageView = itemView.findViewById(R.id.left_image_view)
            textSeen = itemView.findViewById(R.id.txt_seen)
            rightImageView = itemView.findViewById(R.id.right_image_view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): MyViewHolder {
        return if (position == 1) {
            val view: View = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_right, parent, false)
            return MyViewHolder(view)
        } else {
            val view: View = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_left, parent, false)
            return MyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val chat: Chat = mChatList[position]
        Picasso.get().load(imageUrl).into(holder.profileImg)
        // images messages
        if (chat.getMessage()!!.equals("hell") && !chat.getUrl().equals("")) {
            // image message -right side
            if (chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.rightImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.rightImageView)
                holder.rightImageView!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What Do You Want?")
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        } else if (which == 1) {
                            deleteSentMessage(position, holder)
                        }
                    })
                    builder.show()
                }
            } else if (!chat.getSender().equals(firebaseUser!!.uid)) {
                holder.showTextMessage!!.visibility = View.GONE
                holder.leftImageView!!.visibility = View.VISIBLE
                Picasso.get().load(chat.getUrl()).into(holder.leftImageView)
                holder.leftImageView!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("What Do You Want?")
                    builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                        if (which == 0) {
                            val intent = Intent(mContext, ViewFullImageActivity::class.java)
                            intent.putExtra("url", chat.getUrl())
                            mContext.startActivity(intent)
                        }
                    })
                    builder.show()
                }
            }
        }
        // text messages
        else {
            holder.showTextMessage!!.text = chat.getMessage()
           if (firebaseUser!!.uid == chat.getSender()){
               holder.showTextMessage!!.setOnClickListener {
                   val options = arrayOf<CharSequence>(
                       "Delete Message",
                       "Cancel"
                   )
                   var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                   builder.setTitle("What Do You Want?")
                   builder.setItems(options, DialogInterface.OnClickListener { dialog, which ->
                       if (which == 0) {
                           deleteSentMessage(position, holder)
                       }
                   })
                   builder.show()
               }
           }
        }
        // sent and seen message
        if (position == mChatList.size - 1) {
            if (chat.isSeen()) {
                holder.textSeen!!.text = "Seen"
                if (chat.getMessage()!!.equals("hell") && !chat.getUrl().equals("")) {
                    val lp: RelativeLayout.LayoutParams? =
                        holder.textSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    holder.textSeen!!.layoutParams = lp
                }
            } else {
                holder.textSeen!!.text = "Sent"
                if (chat.getMessage()!!.equals("hell") && !chat.getUrl().equals("")) {
                    val lp: RelativeLayout.LayoutParams? =
                        holder.textSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0, 245, 10, 0)
                    holder.textSeen!!.layoutParams = lp
                }
            }
        } else {
            holder.textSeen!!.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender().equals(firebaseUser!!.uid)) {
            1
        } else {
            0
        }
    }

    private fun deleteSentMessage(position: Int, holder: ChatsAdapter.MyViewHolder) {
        val ref = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference.child("Chats")
            .child(mChatList.get(position).getMessageId()!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(mContext, "Failed Not Deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }
}