package com.sam.chatdemoappkt
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class VisitUserProfileActivity : AppCompatActivity() {
    private var  userVisitId: String = ""
    var user: Users? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)
        userVisitId = intent.getStringExtra("visit_id")
        val ref  = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference
            .child("users")
            .child(userVisitId)
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                     user = p0.getValue(Users::class.java)
                    val userNameDisplay = findViewById<TextView>(R.id.user_name_display)
                    userNameDisplay.text = user!!.getUserName()
                    val profileDisplay = findViewById<CircleImageView>(R.id.profile_display)
                    Picasso.get().load("gs://chatappkt-48d92.appspot.com/download.png").into(profileDisplay)
                    val coverDisplay = findViewById<ImageView>(R.id.profile_display)
                    Picasso.get().load("gs://chatappkt-48d92.appspot.com/images.jpg").into(coverDisplay)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        val facebookDisplay = findViewById<ImageView>(R.id.display_facebook)
        facebookDisplay.setOnClickListener {
            val url = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW,url)
            startActivity(intent)
        }
        val instagramDisplay = findViewById<ImageView>(R.id.display_instagram)
        instagramDisplay.setOnClickListener {
            val url = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW,url)
            startActivity(intent)
        }
        val websiteDisplay = findViewById<ImageView>(R.id.display_website)
        websiteDisplay.setOnClickListener {
            val url = Uri.parse(user!!.getWebsite())
            val intent = Intent(Intent.ACTION_VIEW,url)
            startActivity(intent)
        }
        val sendMessageButton = findViewById<Button>(R.id.send_msg_btn)
        sendMessageButton.setOnClickListener {
            val intent = Intent(this,MessageChatActivity::class.java)
            intent.putExtra("visit_id",user!!.getUID())
            startActivity(intent)
        }
    }
}