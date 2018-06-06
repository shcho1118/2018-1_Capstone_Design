package com.simplemobiletools.calendar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.simplemobiletools.calendar.services.PopupActivity;
import com.simplemobiletools.calendar.helpers.DBHelper;
import com.simplemobiletools.calendar.models.Event;
import com.skt.Tmap.TMapTapi;

import java.util.Calendar;


public class MyLocationService extends Service {
    public double longitude = 0;
    public double latitude = 0;
    private LocationManager mLocationManager = null;
    private int timer = 280;
    private final String TMAP_API_KEY = "91641875-873e-44bf-ad99-6021fec7a262";//api키 값입니다
    private TMapTapi tmaptapi;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;
        private String TAG = "LocationListener";

        public LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            timer = timer + 20;
            mLastLocation.set(location);
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            Log.d(TAG, "현재 위치 : " + longitude + ", " + latitude + " timer : " + timer);
            if(timer >= 300){
                timer = 0;
                testfun();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }
    }

    private void initializeLocationManager() {
        Log.e("Location Manager", "initialize LocationManager");
        tmaptapi = new TMapTapi(this);  // 원래 여기가 this 였음.
        tmaptapi.setSKTMapAuthentication(TMAP_API_KEY);

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    LocationListener mLocationListeners = new LocationListener(LocationManager.NETWORK_PROVIDER);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        initializeLocationManager();
        try{

            Log.e("Location onCreate", "onCreate입니다.");
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 3 , 0, mLocationListeners); //, Looper.getMainLooper());
        } catch (java.lang.SecurityException ex) {
            Log.i("Location onCreate", "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d("Location onCreate", "network provider does not exist, " + ex.getMessage());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }

    @Override
    public void onCreate(){
    }

    @Override
    public void onDestroy() {
        Log.e("Location onDestroy", "onDestroy입니다.");
        longitude = 0;
        latitude = 0;
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListeners);
            } catch (Exception ex) {
                Log.i("Location Destroy", "fail to remove location listners, ignore", ex);
            }
        }
    }


    // 유지성을 이미 이 부분에서 갖다버렸습니다ㅓ ㅠㅠ
    // 조건에 맞는 이벤트를 찾아 알람 시간을 갱신해주는 메서드입니다.
    public void testfun() {
        Calendar calendar = Calendar.getInstance();
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        long now = calendar.getTimeInMillis();
        Log.d("Location Test", "DBNAME + " + DBHelper.DB_NAME);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor1 = db.rawQuery("SELECT * FROM events", null);
        try {
            // Main Table의 모든 Row를 읽어온다.
            if (cursor1.moveToFirst()) {
                while(true) {
                    int eventNo = cursor1.getInt(0 );
                    int startTime = cursor1.getInt(1);
                    String eventPlaceID = cursor1.getString(18);
                    int reminderTime = cursor1.getInt(5);
                    int checked = cursor1.getInt(19);
                    int delay1 = cursor1.getInt(24);
                    int delay2 = cursor1.getInt(25);
                    // DB의 Start Time은 1초 단위고 여기선 1/1000 단위라 단위를 맞추어주기 위함이다.
                    long startTime2 = ((long) startTime) * 1000;
                    long lefttime = startTime2 - now;
                    int remaintime = 0;

                    // 만약 설정이 거리 기반으로 설정되어 있고 8시간 안에 일어날 사건이고 알람이 울린 시간이 지났다면
                    if(checked != 0 && (lefttime <= 60000 * 60 * 8 && (lefttime/60000) > reminderTime) && eventPlaceID != null) {
                        if(longitude != 0 && latitude != 0) {

                            // 도보나 자동차의 경우 askTmap 클래스를 이용한다.
                            if(checked == 1 || checked == 2){
                                String temp1 = cursor1.getString(22);
                                String temp2 = cursor1.getString(23);
                                try {
                                    remaintime = new AskTmap(latitude, longitude, Double.parseDouble(temp1), Double.parseDouble(temp2), checked).execute().get();
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            // 대중교통의 경우 Google Direction API를 이용한다.
                            else if(checked == 3) {
                                AskGoogle askGoogle = new AskGoogle(eventPlaceID, longitude, latitude);
                                if (askGoogle.GetResult() != 0) {
                                    remaintime = askGoogle.GetResult();
                                }
                            }
                        }
                        // 거리 계산하는 프로그램이 만든 결과를 reminder minute로 만든다.
                        remaintime = (remaintime /60) + 2;

                        // 사용자 설정에 따라 알람을 좀 빨리 울릴 수 있다.
                        if(delay1 == -1) {
                            double temp = ((remaintime * delay2) / 100);
                            remaintime = remaintime + (int)temp;
                        }
                        else remaintime = remaintime + delay1;

                        if((lefttime/60000) - remaintime <= 0)
                            remaintime = ((int)(lefttime/60000)) - 2;

                        Log.d("Location Test", "바뀐 이벤트 remaintime " + remaintime);

                        db.execSQL("UPDATE events SET reminder_minutes = " + remaintime + " WHERE id = " + eventNo);
                        Event mEvent = dbHelper.getEventWithId(eventNo);
                        dbHelper.update(mEvent, true, null);
                    }

                    if(checked != 0 && lefttime/60000 < -20 && lefttime/60000 > -40 && delay1 == -1)
                    {
                        getPopup();
                        db.execSQL("UPDATE events SET delay_time = " + -2 + " WHERE id = " + eventNo);
                        db.execSQL("UPDATE events SET is_finished = " + 1 + " WHERE id = " + eventNo);
                    }

                    String eventTitle = cursor1.getString(3);
                    Log.d("Location Test", "Event = " + eventTitle + startTime + ", reminderTime " + reminderTime + " PLACEID : "+eventPlaceID);
                    if(cursor1.isLast())
                        break;
                    else
                        cursor1.moveToNext();
                }
            }
            Log.d("Location Test", "testfun 정상적으로 완료");
        }
        catch(Exception e) {
            Log.d("Location Test Error", "testfun에 문제 " + e);
        }

    }

    public void getPopup(){
        Intent intent = new Intent(this, PopupActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}

// 참조
// http://unikys.tistory.com/283
// https://okky.kr/article/366824
// DB 커서를 다뤄보아요!(아래)
// http://ssunno.tistory.com/4

//https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android
