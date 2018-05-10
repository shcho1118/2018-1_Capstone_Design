package com.simplemobiletools.calendar.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.simplemobiletools.calendar.R;

public class CaptureService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Intent prj = new Intent(this, ProjectorService.class)
                .putExtra(ProjectorService.EXTRA_RESULT_CODE
                        , intent.getIntExtra(ProjectorService.EXTRA_RESULT_CODE, -1))
                .putExtra(ProjectorService.EXTRA_RESULT_INTENT
                        , intent.getParcelableExtra(ProjectorService.EXTRA_RESULT_INTENT));
        PendingIntent prjPending = PendingIntent
                .getService(this
                        , -1
                        , prj, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Tap this notification for capture")
                .setContentText("Your event is added automatically")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(prjPending)
                .build();
        startForeground(-1, notification);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
