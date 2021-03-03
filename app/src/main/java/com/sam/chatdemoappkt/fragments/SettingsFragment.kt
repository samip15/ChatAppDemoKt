package com.sam.chatdemoappkt.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.sam.chatdemoappkt.R
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class SettingsFragment : Fragment() {
    var userRefrence: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val REQUEST_CODE = 4312
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""
    private val REQUEST_CODE2 = 200

    //    private var coverImgUrl: String? = ""
   //private var profileImgUrl: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        userRefrence =
            FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/").reference.child(
                "users"
            ).child(firebaseUser!!.uid)
        storageRef =
            FirebaseStorage.getInstance("gs://chatappkt-48d92.appspot.com").reference.child(
                "user images/"
            )

        userRefrence!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)
                    if (context != null) {
                        val userNameSettings = view.findViewById<TextView>(R.id.user_name_settings)
                        userNameSettings.text = user!!.getUserName()
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
        val profileImageSettings = view.findViewById<CircleImageView>(R.id.profile_image_settings)
        profileImageSettings.setOnClickListener {
            pickImage()
        }
        val coverImageSettings = view.findViewById<ImageView>(R.id.cover_image_settings)
        coverImageSettings.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }
        val setFb = view.findViewById<ImageView>(R.id.set_facebook)
        setFb.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }
        val setInsta = view.findViewById<ImageView>(R.id.set_instagram)
        setInsta.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }
        val setWebSite = view.findViewById<ImageView>(R.id.set_website)
        setWebSite.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }
        return view
    }

    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context!!, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        if (socialChecker == "website") {
            builder.setTitle("Write Url:")
        } else {
            builder.setTitle("Write Username:")
        }
        val editText = EditText(context)
        if (socialChecker == "website") {
            editText.hint = "e.g www.google.com"
        } else {
            editText.hint = "e.g samip123"
        }
        builder.setView(editText)
        builder.setPositiveButton("create", DialogInterface.OnClickListener { dialog, which ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(requireContext(), "Please write something", Toast.LENGTH_SHORT)
                    .show()
            } else {
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun saveSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()
//        mapSocial["cover"] = url
//        userRefrence!!.updateChildren(mapCoverImage)
        when (socialChecker) {
            "facebook" -> {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" -> {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        userRefrence!!.updateChildren(mapSocial).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val progressBar = view!!.findViewById<ProgressBar>(R.id.progressBar_settings)
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(requireContext(), "Updated Succussufully..", Toast.LENGTH_SHORT)
            }
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data.data
            Toast.makeText(requireContext(), "Uploading....", Toast.LENGTH_SHORT).show()
            uploadImageToDatabase()
            // done check
            val profileImageSettings =
                view?.findViewById<ImageView>(R.id.profile_image_settings)
            val coverImageSettings =
                view?.findViewById<ImageView>(R.id.cover_image_settings)
            Picasso.get()
                .load(imageUri)
                .into(profileImageSettings)
            Picasso.get()
                .load(imageUri)
                .into(coverImageSettings)

        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = view!!.findViewById<ProgressBar>(R.id.progressBar_settings)
        progressBar.visibility = View.VISIBLE
        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString())
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    if (coverChecker == "cover") {
                        val mapCoverImage = HashMap<String, Any>()
                        mapCoverImage["cover"] = url
                        userRefrence!!.updateChildren(mapCoverImage)
                        coverChecker = ""

                    } else {
                        val mapProfileImage = HashMap<String, Any>()
                        mapProfileImage["profile"] = url
                        userRefrence!!.updateChildren(mapProfileImage)
                        coverChecker = ""
                    }
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}