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

class LoginActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUser: DatabaseReference
    private var firebaseUserId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar = findViewById<Toolbar>(R.id.toolBar_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        mAuth = FirebaseAuth.getInstance()
        val loginBtn = findViewById<Button>(R.id.login_btn)
        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val emailLog = findViewById<EditText>(R.id.email_login)
        val email: String = emailLog.text.toString()
        val passwordLog = findViewById<EditText>(R.id.password_login)
        val password: String = passwordLog.text.toString()
        if (email == "") {
            Toast.makeText(this, "Please write email", Toast.LENGTH_SHORT).show()
        } else if (password == "") {
            Toast.makeText(this, "Please write password", Toast.LENGTH_SHORT).show()
        } else {
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{
                    task ->
                    if (task.isSuccessful){
                        val intent = Intent(this,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }else{
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