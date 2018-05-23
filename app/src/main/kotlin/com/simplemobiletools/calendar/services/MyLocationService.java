package com.simplemobiletools.calendar.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import com.simplemobiletools.calendar.helpers.DBHelper;
import android.util.Log;

import com.simplemobiletools.calendar.R;


public class MyLocationService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }

                @Override
                public void onLocationChanged(Location location) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    Log.d("Location Service : ", "현재 위치 : " + longitude + ", " + latitude );
                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000000, 0, locationListener);
            Log.d("MyLocationService","일단 여기는 실행되었어요");

        }
        catch(SecurityException e){
            Log.d("MyLocationService Error","위치 정보를 가져오지 못했어요");
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

// 참조
// http://unikys.tistory.com/283
// https://okky.kr/article/366824
