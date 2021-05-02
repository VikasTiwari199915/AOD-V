package com.anvi.aodv;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.util.Log;

public class NLService extends NotificationListenerService {

    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("NLService","Started");
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.anvi.aodv.NOTIFICATION_ACTION_SERVICE");
        registerReceiver(nlservicereciver,filter);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d("Listener","Connected");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereciver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        try {
            //Log.i(TAG,"**********  onNotificationPosted  **********");
            //Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
            PackageManager packageManager= getApplicationContext().getPackageManager();
            String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.getPackageName(), PackageManager.GET_META_DATA));
            Notification mNotification=sbn.getNotification();
//            String TickerText="";
//            try {TickerText=mNotification.tickerText.toString();} catch (Exception ignored) {}
            SpannableString Title= SpannableString.valueOf("");
            try {
                Title= (SpannableString) mNotification.extras.get("android.title");
            } catch (Exception ignored) {
                Title= SpannableString.valueOf((CharSequence) mNotification.extras.get("android.title"));
            }

            SpannableString Text= SpannableString.valueOf("");
            try {
                Text = (SpannableString) mNotification.extras.get("android.text");
            } catch (Exception ignored) {
                Text = SpannableString.valueOf((CharSequence) mNotification.extras.get("android.text"));
            }

            if(mNotification.priority>=Notification.PRIORITY_DEFAULT && (mNotification.category!=null|| mNotification.category.equals(Notification.CATEGORY_TRANSPORT))) {
                Log.i("Notification",appName+"=>"+mNotification.category);
                Intent i = new Intent("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
                i.putExtra("notification_Package", sbn.getPackageName());
                i.putExtra("notification_AppName", appName);
                i.putExtra("notification_Title", Title.toString());
                i.putExtra("notification_Text", Text.toString());
                sendBroadcast(i);
            }
        } catch (Exception ignored) {
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        try {
//            Log.i(TAG,"********** onNotificationRemoved");
//            Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
            Intent i = new  Intent("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
            i.putExtra("notification_event","onNotificationRemoved :" + sbn.getPackageName() + "\n");
            sendBroadcast(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    class NLServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(intent.getStringExtra("command").equals("clearall")){
                        NLService.this.cancelAllNotifications();
                }
                else if(intent.getStringExtra("command").equals("list")){
                    Intent i1 = new  Intent("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
                    i1.putExtra("notification_event","=====================");
                    sendBroadcast(i1);
                    int i=1;
                    for (StatusBarNotification sbn : NLService.this.getActiveNotifications()) {
                        Intent i2 = new  Intent("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
                        i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
                        sendBroadcast(i2);
                        i++;
                    }
                    Intent i3 = new  Intent("com.anvi.aodv.NOTIFICATION_LISTENER_SERVICE");
                    i3.putExtra("notification_event","===== Notification List ====");
                    sendBroadcast(i3);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}