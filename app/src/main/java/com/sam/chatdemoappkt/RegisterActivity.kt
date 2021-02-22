package com.sam.chatdemoappkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUser: DatabaseReference
    private var firebaseUserId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val toolbar = findViewById<Toolbar>(R.id.toolBar_register)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        mAuth = FirebaseAuth.getInstance()
        val registerBtn = findViewById<Button>(R.id.register_btn)
        registerBtn.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val userNameReg = findViewById<EditText>(R.id.username_register)
        val userName: String = userNameReg.text.toString()
        val emailReg = findViewById<EditText>(R.id.email_register)
        val email: String = emailReg.text.toString()
        val passwordReg = findViewById<EditText>(R.id.password_register)
        val password: String = passwordReg.text.toString()
        if (userName == "") {
            Toast.makeText(this, "Please write username", Toast.LENGTH_SHORT).show()
        } else if (email == "") {
            Toast.makeText(this, "Please write email", Toast.LENGTH_SHORT).show()
        } else if (password == "") {
            Toast.makeText(this, "Please write password", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseUserId = mAuth.currentUser!!.uid
                        refUser = FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/").reference.child("users")
                            .child(firebaseUserId)
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserId
                        userHashMap["username"] = userName
                        userHashMap["profile"] =
                            "https://firebasestorage.googleapis.com/v0/b/chatappkt-48d92.appspot.com/o/download.png?alt=media&token=455306d6-88a9-4bee-b3fc-9fc31321eed6"
                        userHashMap["cover"] =
                            "https://firebasestorage.googleapis.com/v0/b/chatappkt-48d92.appspot.com/o/images.jpg?alt=media&token=573b60f7-1c68-4bf5-aa0a-cea147817265"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = userName.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https//m.instagram.com"
                        userHashMap["username"] = "https://www.google.com"
                        refUser.updateChildren(userHashMap)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this,MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                    } else {
                        Toast.makeText(
                            this,
                            "Error Message" + task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
