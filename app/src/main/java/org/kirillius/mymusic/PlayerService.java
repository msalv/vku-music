package org.kirillius.mymusic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Kirill on 21.02.2016.
 */
public class PlayerService extends Service {

    public static final String EXTRA_TRACKS = "ExtraTracks";
    public static final String EXTRA_POSITION = "ExtraPosition";
    public static final String EXTRA_TOTAL = "ExtraTotal";

    private ArrayList<VKApiAudio> mTracks = new ArrayList<>();
    private int mCurrentPosition = 0;
    private int mTotalCount = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mTracks = intent.getParcelableArrayListExtra(EXTRA_TRACKS);
        mCurrentPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        mTotalCount = intent.getIntExtra(EXTRA_TOTAL, 0);

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
