package org.kirillius.mymusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;

/**
 * Created by Kirill on 21.02.2016.
 */
public class PlayerService extends Service {

    public static final String EXTRA_TRACKS = "ExtraTracks";
    public static final String EXTRA_POSITION = "ExtraPosition";
    public static final String EXTRA_TOTAL = "ExtraTotal";

    public static final String ACTION_PREV = "org.kirillius.mymusic.ActionPrev";
    public static final String ACTION_PLAY_PAUSE = "org.kirillius.mymusic.ActionPlayPause";
    public static final String ACTION_NEXT = "org.kirillius.mymusic.ActionNext";
    public static final String ACTION_CLOSE = "org.kirillius.mymusic.ActionClose";

    public static final int NOTIFICATION_ID = 1;

    private ArrayList<VKApiAudio> mTracks = new ArrayList<>();
    private int mCurrentPosition = 0;
    private int mTotalCount = 0;
    private boolean mIsPlaying = false;

    private PlayerBroadcastReceiver mReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mTracks = intent.getParcelableArrayListExtra(EXTRA_TRACKS);
        mCurrentPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        mTotalCount = intent.getIntExtra(EXTRA_TOTAL, 0);

        // register broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        intentFilter.addAction(ACTION_PLAY_PAUSE);
        intentFilter.addAction(ACTION_PREV);
        intentFilter.addAction(ACTION_NEXT);
        intentFilter.addAction(ACTION_CLOSE);

        registerReceiver(mReceiver, intentFilter);

        // show notification
        updateNotification();

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new PlayerBroadcastReceiver();
    }

    /**
     * Updates the notification
     */
    private void updateNotification() {
        VKApiAudio track = mTracks.get(mCurrentPosition);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.service_player);

        contentView.setTextViewText(R.id.artist, track.artist);
        contentView.setTextViewText(R.id.track, track.title);

        int resId = mIsPlaying ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
        contentView.setImageViewResource(R.id.play, resId);

        contentView.setOnClickPendingIntent(R.id.prev, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PREV), 0));
        contentView.setOnClickPendingIntent(R.id.play, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_PLAY_PAUSE), 0));
        contentView.setOnClickPendingIntent(R.id.next, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_NEXT), 0));
        contentView.setOnClickPendingIntent(R.id.close, PendingIntent.getBroadcast(this, 0, new Intent(ACTION_CLOSE), 0));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_headset_white_18dp).setContent(contentView);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class PlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if ( PlayerService.ACTION_PLAY_PAUSE.equals(action) ) {
                mIsPlaying = !mIsPlaying;
                PlayerService.this.updateNotification();
            }
            else if ( PlayerService.ACTION_PREV.equals(action) ) {
                mCurrentPosition = --mCurrentPosition < 0 ? mTracks.size() - 1 : mCurrentPosition;
                PlayerService.this.updateNotification();
            }
            else if ( PlayerService.ACTION_NEXT.equals(action) ) {
                mCurrentPosition = ++mCurrentPosition % mTracks.size();
                PlayerService.this.updateNotification();
            }
            else if ( PlayerService.ACTION_CLOSE.equals(action) ) {
                PlayerService.this.stopForeground(true);
            }
        }
    }
}
