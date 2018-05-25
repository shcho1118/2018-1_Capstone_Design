package com.simplemobiletools.calendar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.simplemobiletools.calendar.helpers.DBHelper;

import java.util.Calendar;


public class MyLocationService extends Service {
    public double longitude = 0;
    public double latitude = 0;

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
                    longitude = location.getLongitude();
                    latitude = location.getLatitude();
                    Log.d("Location Service : ", "현재 위치 : " + longitude + ", " + latitude );

                    updateTargetEventTime();
                    testfun();
                }
            };

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000*60*20, 0, locationListener);
            Log.d("MyLocationService","LocationManager가 실행이 되었어요");

        }
        catch(SecurityException e){
            longitude = 0;
            latitude = 0;
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
        longitude = 0;
        latitude = 0;
        super.onDestroy();
    }

    public void updateTargetEventTime(){
        try{
            // Target Event를 불러오는 함수
            // for(Target Event 수 만큼){
            // Target Event들의 시간을 계산하는 함수
            // Target Event들의 알람 시간을 DB에 적용하는 함수
            // }
        }
        catch(Exception e) {

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
                    String eventTitle = cursor1.getString(3);
                    String eventPlaceID = cursor1.getString(18);
                    int reminderTime = cursor1.getInt(5);
                    // DB의 Start Time은 1초 단위고 여기선 1/1000 단위라 단위를 맞추어주기 위함이다.
                    long startTime2 = ((long) startTime) * 1000;
                    long lefttime = startTime2 - now;
                    int remaintime = 0;

                    // 만약 설정이 거리 기반으로 설정되어 있고 8시간 안에 일어날 사건이라면
                    if(/*reminderTime == -2 && */ (lefttime <= 60000 * 60 * 8 && lefttime > 0) && eventPlaceID != "") {
                        if(longitude != 0 && latitude != 0){
                            AskGoogle askGoogle = new AskGoogle(eventPlaceID, longitude, latitude);
                            if(askGoogle.GetResult() != 0){
                                remaintime = askGoogle.GetResult();
                                Log.d("Location Test", "바뀐 이벤트 remaintime " + remaintime/60);
                            }
                        }
                        // 거리 계산하는 프로그램이 만든 결과를 reminder minute로 만든다.
                        db.execSQL("UPDATE events SET reminder_minutes = " + remaintime/60 + " WHERE id = " + eventNo);
                    }
                    Log.d("Location Test", "Event = " + eventTitle + startTime + ", reminderTime " + reminderTime);
                    if(cursor1.isLast())
                        break;
                    else
                        cursor1.moveToNext();
                }
            }
            Log.d("Location Test", "testfun 정상적으로 완료");
        }
        catch(Exception e) {
            Log.d("Location Test Error", "testfun에 문제 ");
        }

    }


}

// 참조
// http://unikys.tistory.com/283
// https://okky.kr/article/366824
// DB 커서를 다뤄보아요!(아래)
// http://ssunno.tistory.com/4
