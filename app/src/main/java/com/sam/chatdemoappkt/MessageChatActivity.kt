package com.sam.chatdemoappkt

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val reference =
            FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                .reference
                .child("Users")
                .child(userIdVisit)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)
                    val userNameChat = findViewById<TextView>(R.id.username_message)
                    userNameChat.text = user!!.getUserName()
                    val profileImageChat = findViewById<CircleImageView>(R.id.profile_image_mchat)
                    Picasso
                        .get()
                        .load("gs://chatappkt-48d92.appspot.com/user images/1614151815523.jpg")
                        .into(profileImageChat)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        val sendMessage = findViewById<ImageView>(R.id.send_message_btn)
        sendMessage.setOnClickListener {
            val message = findViewById<EditText>(R.id.text_message).text.toString()
            if (message == "") {
                Toast.makeText(
                    this,
                    "   Please Write A Message,First",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            val textMessage = findViewById<EditText>(R.id.text_message)
            textMessage.setText("")
        }
        val attractImgFile = findViewById<ImageView>(R.id.attach_image_file_btn)
        attractImgFile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 111)
        }
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference =
            FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                .reference
        val messageKey = reference.push().key
        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = "gs://chatappkt-48d92.appspot.com/user images/1614151815523.jpg"
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatListReference = FirebaseDatabase
                        .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                        .reference.child("chatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                chatListReference.child("id").setValue(userIdVisit)
                            }
                            val chatListReceiverReference = FirebaseDatabase
                                .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                                .reference.child("chatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)
                            chatListReceiverReference.child("id").setValue(firebaseUser!!.uid)
                        }

                        override fun onCancelled(p0: DatabaseError) {
                        }
                    })
                    val reference =
                        FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                            .reference
                            .child("Users")
                            .child(firebaseUser!!.uid)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == Activity.RESULT_OK && data != null && data!!.data != null) {
            val loadingBar = ProgressDialog(applicationContext)
            loadingBar.setMessage("Please Wait, image is sending.....")
            loadingBar.show()
            val fileUri = data.data
            val storageReference = FirebaseStorage
                .getInstance("gs://chatappkt-48d92.appspot.com")
                .reference
                .child("chat images")
            val ref = FirebaseDatabase
                .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                .reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")
            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                }

            }

        }
    }
}