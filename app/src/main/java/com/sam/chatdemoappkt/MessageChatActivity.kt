package com.sam.chatdemoappkt

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.icu.lang.UCharacter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.sam.chatdemoappkt.Notification.Client
import com.sam.chatdemoappkt.Notification.MyResponse
import com.sam.chatdemoappkt.Notification.Sender
import com.sam.chatdemoappkt.Notification.Token
import com.sam.chatdemoappkt.adapterClasses.ChatsAdapter
import com.sam.chatdemoappkt.fragments.ApiService
import com.sam.chatdemoappkt.modelClasses.Chat
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.sam.chatdemoappkt.Notification.Data as Data

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatsAdapter: ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recyclerViewChats: RecyclerView
    var reference: DatabaseReference? = null
    var seenListner: ValueEventListener? = null
    var notify = false
    var apiService: ApiService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)
        val toolbar: Toolbar = findViewById(R.id.toolBar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        apiService = Client.client.getClient("https://fcm.googleapis.com/")!!.create(ApiService::class.java)
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference =
            FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                .reference
                .child("Users")
                .child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)
                    val userNameChat = findViewById<TextView>(R.id.username_message)
                    userNameChat.text = user!!.getUserName()
                    val profileImageChat = findViewById<CircleImageView>(R.id.profile_image_mchat)
                    Picasso
                        .get()
                        .load("gs://chatappkt-48d92.appspot.com/user images/1614151815523.jpg")
                        .into(profileImageChat)
                    retriveMessage(firebaseUser!!.uid, userIdVisit, user.getProfile())
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        val sendMessage = findViewById<ImageView>(R.id.send_message_btn)
        sendMessage.setOnClickListener {
            val progressBarMessageChat = findViewById<ProgressBar>(R.id.progressBar_chat)
            progressBarMessageChat.visibility = View.VISIBLE
            notify = true
            val message = findViewById<EditText>(R.id.text_message).text.toString()
            if (message == "") {
                Toast.makeText(
                    this,
                    "   Please Write A Message,First",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
                retriveMessage(firebaseUser!!.uid,userIdVisit,"gs://chatappkt-48d92.appspot.com/user images/1614151815523.jpg")
            }
            val textMessage = findViewById<EditText>(R.id.text_message)
            textMessage.setText("")
        }
        seenMessage(userIdVisit)
    }

    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val progressBarMessageChat = findViewById<ProgressBar>(R.id.progressBar_chat)
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
                    progressBarMessageChat.visibility = View.INVISIBLE
                }
                val userReference =
                    FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                        .reference
                        .child("Users")
                        .child(firebaseUser!!.uid)
                // fcm value event listener
                userReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val user = p0.getValue(Users::class.java)
                        if (notify) {
                            sendNotification(receiverId, user?.getUserName(), message)
                        }
                        notify = false
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
            }
    }

    private fun sendNotification(receiverId: String?, userName: String?, message: String) {
        val ref =
            FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                .reference
                .child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$userName: $message",
                        "New Message",
                        userIdVisit
                    )
                    val sender = Sender(data!!, token!!.getToken().toString())
                    apiService!!.sendNotification(sender)
                        .enqueue(object : Callback<MyResponse> {
                            override fun onResponse(
                                call: Call<MyResponse>,
                                response: Response<MyResponse>
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()!!.success != 1) {
                                        Toast.makeText(this@MessageChatActivity ,"Failed Nothing Happened", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponse>, t: Throwable) {

                            }
                        })

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
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
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val reference =
                                    FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
                                        .reference
                                        .child("Users")
                                        .child(firebaseUser!!.uid)
                                // fcm value event listener
                                reference.addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(Users::class.java)
                                        if (notify) {
                                            sendNotification(
                                                userIdVisit,
                                                user!!.getUserName(),
                                                "sent you an image."
                                            )
                                        }
                                        notify = false
                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }
                                })
                            }
                        }
                }

            }

        }
    }

    private fun retriveMessage(senderId: String, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children) {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderId) &&
                        chat!!.getSender().equals(receiverId) ||
                        chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId)
                    ) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(
                        this@MessageChatActivity,
                        (mChatList as ArrayList<Chat>),
                        receiverImageUrl!!
                    )
                    recyclerViewChats = findViewById(R.id.recycler_view_chat)
                    recyclerViewChats.setHasFixedSize(true)
                    var linearLayoutManager = LinearLayoutManager(applicationContext,RecyclerView.VERTICAL,false)
                    linearLayoutManager.stackFromEnd = true
                    recyclerViewChats.layoutManager = linearLayoutManager
                    recyclerViewChats.adapter = chatsAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun seenMessage(userId: String) {
        val reference = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference.child("Chats")
        seenListner = reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender()
                            .equals(userId)
                    ) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["isSeen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListner!!)
    }
}