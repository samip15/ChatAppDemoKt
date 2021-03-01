package com.sam.chatdemoappkt.fragments;

import com.sam.chatdemoappkt.Notification.MyResponse;
import com.sam.chatdemoappkt.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAhkY9Pio:APA91bHQCTGG_hrFERWTKGy41aF-nD5zQUDx8icvoD1VMxGCvCD8dtObrgLuv6p7lKvuCfZ7gEYQWvdwmH-ol3phnZnsviATl0O_5TGcgGLSt-l8aXBybFXaP2ngdCwjxzfn5poU3lf-"
    })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
