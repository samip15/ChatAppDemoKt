package com.sam.chatdemoappkt.Notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sam.chatdemoappkt.MessageChatActivity

class MyFirebaseMessagingService : FirebaseMessagingService()
{
    override fun onMessageReceived(mRemoteMessage: RemoteMessage) {
        super.onMessageReceived(mRemoteMessage)
        val sented = mRemoteMessage.data["sented"]
        val user = mRemoteMessage.data["user"]
        val sharedPref = getSharedPreferences("PREFS",Context.MODE_PRIVATE)
        val currentOnlineUser = sharedPref.getString("currentUser","none")
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser!=null && sented == firebaseUser.uid){
            if (currentOnlineUser!=user){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    sendOrioNotification(mRemoteMessage)
                }else{
                    sendNotification(mRemoteMessage)
                }
            }
        }
    }

    private fun sendNotification(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]
        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent =    Intent(this,MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
       val builder =  NotificationCompat.Builder(this,"com.sam.chatdemoappkt")
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(defaultSound)
            .setAutoCancel(true)
        val noti = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j>0){
            i=j
        }
        noti.notify(i,builder.build())
    }

    private fun sendOrioNotification(mRemoteMessage: RemoteMessage) {
        val user = mRemoteMessage.data["user"]
        val icon = mRemoteMessage.data["icon"]
        val title = mRemoteMessage.data["title"]
        val body = mRemoteMessage.data["body"]
        val notification = mRemoteMessage.notification
        val j = user!!.replace("[\\D]".toRegex(),"").toInt()
        val intent =    Intent(this,MessageChatActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userid",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val orioNotification = OrioNotification(this)
        val builder : Notification.Builder = orioNotification.getOrieoNotification(title,body,pendingIntent,defaultSound,icon)
        var i = 0
        if (j>0){
            i=j
        }
        orioNotification.getManager!!.notify(i,builder.build())
    }
}