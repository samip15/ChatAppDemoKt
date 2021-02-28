package com.sam.chatdemoappkt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.sam.chatdemoappkt.fragments.ChatsFragment
import com.sam.chatdemoappkt.fragments.SearchFragment
import com.sam.chatdemoappkt.fragments.SettingsFragment
import com.sam.chatdemoappkt.modelClasses.Chat
import com.sam.chatdemoappkt.modelClasses.Users
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {
    var refUsers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/").reference.child("users").child(firebaseUser!!.uid)
        val toolbar = findViewById<Toolbar>(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
//        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
//        viewPagerAdapter.addFragment(ChatsFragment(),"Chats")
//        viewPagerAdapter.addFragment(SearchFragment(),"Search")
//        viewPagerAdapter.addFragment(SettingsFragment(),"Settings")
//        viewPager.adapter = viewPagerAdapter
//        tabLayout.setupWithViewPager(viewPager)
        val ref = FirebaseDatabase
            .getInstance("https://chatappkt-48d92-default-rtdb.firebaseio.com/")
            .reference
            .child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0
                for (dataSnapShot in p0.children){
                    val chat = dataSnapShot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.isSeen()){
                       countUnreadMessages += 1
                    }
                }
                if (countUnreadMessages == 0){
                    viewPagerAdapter.addFragment(ChatsFragment(),"Chats")
                }else{
                    viewPagerAdapter.addFragment(ChatsFragment(),"($countUnreadMessages) Chats")
                }
                viewPagerAdapter.addFragment(SearchFragment(),"Search")
                viewPagerAdapter.addFragment(SettingsFragment(),"Settings")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        // display the username and profile picture
        refUsers!!.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user: Users? = p0.getValue(Users::class.java)
                    val userNam = findViewById<TextView>(R.id.user_name)
                    userNam.text = user!!.getUserName()
                    val profile = findViewById<ImageView>(R.id.profile_image)
                    Picasso.get().load("gs://chatappkt-48d92.appspot.com/download.png").placeholder(R.drawable.ic_person).into(profile)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        } )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when(item.itemId){
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }
    internal class ViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>
        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()

        }
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
           return fragments[position]
        }
        fun addFragment(fragment: Fragment,title: String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return titles[i]
        }
    }
}