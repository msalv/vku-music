package org.kirillius.mymusic.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Created by Kirill on 14.02.2016.
 */
public class AppLoader extends Application {

    public static Context appContext;

    VKAccessTokenTracker tokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Intent intent = new Intent("org.kirillius.mymusic.ACCESS_TOKEN_INVALID");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                Log.e("mymusic", "VKAccessToken is invalid");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        tokenTracker.startTracking();
        VKSdk.initialize(this);
        appContext = this.getApplicationContext();
    }
}
