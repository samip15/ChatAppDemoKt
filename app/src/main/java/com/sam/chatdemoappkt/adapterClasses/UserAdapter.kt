package com.sam.chatdemoappkt.adapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.sam.chatdemoappkt.MainActivity
import com.sam.chatdemoappkt.MessageChatActivity
import com.sam.chatdemoappkt.R
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(mContext: Context, mUsers: List<Users>, isChatCheck: Boolean) :
    RecyclerView.Adapter<UserAdapter.MyViewHolder?>() {
    private val mContext: Context
    private val mUser: List<Users>
    private var isChatCheck: Boolean

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

        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What Do You Want?")
            builder.setItems(options,DialogInterface.OnClickListener { dialog, position ->
                if (position==0){
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)
                }

                if (position==0){

                }

            })
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
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