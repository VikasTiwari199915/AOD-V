package com.anvi.aodv;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    ImageButton nextTrack,prevTrack;
    TextView musicTitleText,albumNameTxt,artistNameTxt,appNameText,notificationTextView;
    TextView notificationTitleText,timeTxt;
    AudioManager mAudioManager;
    ImageView notificationImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Handler handler = new Handler();
        super.onCreate(savedInstanceState);
        try {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|WindowManager.LayoutParams.FLAG_FULLSCREEN);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        notificationImage=findViewById(R.id.notificationImage);
        notificationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseTrack(v);
            }
        });
        appNameText=findViewById(R.id.appNameTxt);
        notificationTextView=findViewById(R.id.notificationText);
        notificationTitleText=findViewById(R.id.notificationTitle);
        timeTxt=findViewById(R.id.timeTxt);
        nextTrack=findViewById(R.id.next_track);
        prevTrack=findViewById(R.id.prev_track);
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(nReceiver,filter);

        //Declaration
        //play_pause_btn=findViewById(R.id.play_pause_track);

        musicTitleText=findViewById(R.id.musicTitleTxt);
        musicTitleText.setVisibility(View.GONE);
        albumNameTxt=findViewById(R.id.albumNameTxt);
        albumNameTxt.setVisibility(View.GONE);
        artistNameTxt=findViewById(R.id.artistNameTxt);
        artistNameTxt.setVisibility(View.GONE);

        //region timer
        final Handler handler2 =new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                handler2.postDelayed(this, 10000);
                Date dt= Calendar.getInstance().getTime();
                //SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf=new SimpleDateFormat("hh:mm");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat ddf=new SimpleDateFormat("EEE,d MMM");
                timeTxt.setText(sdf.format(dt));
                //date.setText(ddf.format(dt));
                 }
        };
        handler2.postDelayed(r, 0);
        //endregion


        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //place a listener to detect if music is playing or not
            mAudioManager.registerAudioPlaybackCallback(new AudioManager.AudioPlaybackCallback() {
                @Override
                public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
                    super.onPlaybackConfigChanged(configs);
                    if (configs.size() > 0) {
                        prevTrack.setVisibility(View.VISIBLE);
                        nextTrack.setVisibility(View.VISIBLE);
//                        if(mAudioManager.isMusicActive()){
//                            musicTitleText.setVisibility(View.VISIBLE);
//                            albumNameTxt.setVisibility(View.VISIBLE);
//                            artistNameTxt.setVisibility(View.VISIBLE);
//                        }
                    } else {
                        prevTrack.setVisibility(View.GONE);
                        nextTrack.setVisibility(View.GONE);
//                        if(!mAudioManager.isMusicActive()){
//                            musicTitleText.setText("");
//                            musicTitleText.setVisibility(View.GONE);
//                            albumNameTxt.setText("");
//                            albumNameTxt.setVisibility(View.GONE);
//                            artistNameTxt.setText("");
//                            artistNameTxt.setVisibility(View.GONE);
//                        }
                    }


                }
            }, handler);

            //Check if playing or not at startup
            if (mAudioManager.getActivePlaybackConfigurations().size() > 0) {
                prevTrack.setVisibility(View.VISIBLE);
                nextTrack.setVisibility(View.VISIBLE);
            } else {
                prevTrack.setVisibility(View.GONE);
                nextTrack.setVisibility(View.GONE);
            }
        }


        //region Getting Event Broadcasts
        IntentFilter musicIntent = new IntentFilter();
        //Android Stock (I guess)
        musicIntent.addAction("com.miui.player.metachanged");
        musicIntent.addAction("com.android.music.metachanged");
        musicIntent.addAction("com.android.music.queuechanged");
        musicIntent.addAction("com.android.music.playbackcomplete");
        musicIntent.addAction("com.android.music.playstatechanged");
        //HTC
        musicIntent.addAction("com.htc.music.metachanged");
        musicIntent.addAction("fm.last.android.metachanged");
        musicIntent.addAction("com.sec.android.app.music.metachanged");
        musicIntent.addAction("com.nullsoft.winamp.metachanged");
        musicIntent.addAction("com.amazon.mp3.metachanged");
        //MIUI (Doesn't Work :<)
        musicIntent.addAction("com.miui.player.service.metachanged");
        musicIntent.addAction("com.miui.player.service.playstatechanged");

        //Spotify
        musicIntent.addAction("com.spotify.music.playbackstatechanged");
        musicIntent.addAction("com.spotify.music.metadatachanged");
        musicIntent.addAction("metadatachanged");


        musicIntent.addAction("com.real.IMP.metachanged");
        musicIntent.addAction("com.sonyericsson.music.metachanged");
        musicIntent.addAction("com.rdio.android.metachanged");
        musicIntent.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        musicIntent.addAction("com.andrew.apollo.metachanged");
        musicIntent.addAction("com.mxtech.videoplayer.metachanged");
        musicIntent.addAction("com.simplemobiletools.musicplayer.metadatachanged");

        musicIntent.addAction("com.awedea.nyx.metachanged");
        musicIntent.addAction("com.musicplayer.blackplayerfree.metachanged");

        musicIntent.addAction("com.jio.media.jiobeats.playbackstatechanged");
        musicIntent.addAction("com.jio.media.jiobeats.metadatachanged");
        //musicIntent.addAction(Intent.ACTION_SCREEN_OFF);


        PackageManager packageManager= getApplicationContext().getPackageManager();
        @SuppressLint("QueryPermissionsNeeded") List<ApplicationInfo> packages=packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo app : packages){
            Log.e("#",app.packageName);
            musicIntent.addAction(app.packageName+".playbackstatechanged");
            musicIntent.addAction(app.packageName+".metachanged");
            musicIntent.addAction(app.packageName+".metadatachanged");
            musicIntent.addAction(app.packageName+".queuechanged");
            musicIntent.addAction(app.packageName+".playbackcomplete");
            musicIntent.addAction(app.packageName+".playstatechanged");
        }


        registerReceiver(mReceiver, musicIntent);
        //endregion
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
//            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            long eventTime = SystemClock.uptimeMillis();
//            KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
//            assert mAudioManager != null;
//            mAudioManager.dispatchMediaKeyEvent(downEvent);
//            KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
//            mAudioManager.dispatchMediaKeyEvent(upEvent);
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }
    public void playPauseTrack(View view){
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        if(mAudioManager.isMusicActive()){
            long eventTime = SystemClock.uptimeMillis();
            KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
            mAudioManager.dispatchMediaKeyEvent(downEvent);
            KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0);
            mAudioManager.dispatchMediaKeyEvent(upEvent);
        }
        else{
            long eventTime = SystemClock.uptimeMillis();
            KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
            mAudioManager.dispatchMediaKeyEvent(downEvent);
            KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY, 0);
            mAudioManager.dispatchMediaKeyEvent(upEvent);
        }
    }
    public void nextTrack(View view){
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        long eventTime = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        mAudioManager.dispatchMediaKeyEvent(downEvent);
        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_NEXT, 0);
        mAudioManager.dispatchMediaKeyEvent(upEvent);

    }
    public void prevTrack(View view){
        long eventTime = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert mAudioManager != null;
        mAudioManager.dispatchMediaKeyEvent(downEvent);
        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0);
        mAudioManager.dispatchMediaKeyEvent(upEvent);
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(intent.getBooleanExtra("playing",false)){
                    String action = intent.getAction();
                    String cmd = intent.getStringExtra("command");
                    Log.i("tag ", "Action=["+action+"], Command=["+ cmd+"]");
                    String artist = intent.getStringExtra("artist");
                    String album = intent.getStringExtra("album");
                    String track = intent.getStringExtra("track");

                    musicTitleText.setText(track);
                    musicTitleText.setVisibility(View.VISIBLE);
                    albumNameTxt.setText(album);
                    albumNameTxt.setVisibility(View.VISIBLE);
                    artistNameTxt.setText(artist);
                    artistNameTxt.setVisibility(View.VISIBLE);
                    //Log.i("Song Info", id);
                }else{
                    musicTitleText.setText("");
                    musicTitleText.setVisibility(View.GONE);
                    albumNameTxt.setText("");
                    albumNameTxt.setVisibility(View.GONE);
                    artistNameTxt.setText("");
                    artistNameTxt.setVisibility(View.GONE);
                }





//                Log.e("Key","Value");
//                for(String str:intent.getExtras().keySet()){
//                    Log.d("Key",str+"="+intent.getStringExtra(str));
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver nReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(intent!=null){
                    String pkgName=intent.getStringExtra("notification_Package");
                    String appName=intent.getStringExtra("notification_AppName");
                    String notificationTitle=intent.getStringExtra("notification_Title");
                    String notificationText=intent.getStringExtra("notification_Text");
                    PackageManager packageManager= getApplicationContext().getPackageManager();
                    notificationImage.setImageDrawable(packageManager.getApplicationIcon(pkgName));
                    appNameText.setText(appName);
                    notificationTitleText.setText(notificationTitle);
                    notificationTextView.setText(notificationText);
                }
            } catch (Exception ignored) {
            }
        }
    };


    public void clearAllNotification(){
        Intent i = new Intent("com.anvi.aodv.NOTIFICATION_ACTION_SERVICE");
        i.putExtra("command","clearall");
        sendBroadcast(i);
    }
    public void getNotificationList(){
        Intent i = new Intent("com.anvi.aodv.NOTIFICATION_ACTION_SERVICE");
        i.putExtra("command","list");
        sendBroadcast(i);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nReceiver);
        unregisterReceiver(mReceiver);
    }


//    static class NotificationReceiver extends BroadcastReceiver{
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String temp = intent.getStringExtra("notification_event");
//            String image=intent.getStringExtra("notification_Icon");
//
//            //txtView.setText(temp);
//            //Log.e("Data",temp);
//        }
//    }



//    Set<String> extrasList = intent.getExtras().keySet();
//
//    for (String str : extrasList) {
//        Log.d(TAG, str);
//    }

}