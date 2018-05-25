package com.simplemobiletools.calendar.services;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AskGoogle {

    private String mykey = "&key=AIzaSyB1MsgY6fqVK4Egx4k8T5PBh79JYxt7eX8";
    private JSONObject obj;
    private String placeId;
    private double longitude;
    private double latitude;
    private int result = 0;
    private String getpage = new String();

    AskGoogle(){;}
    AskGoogle(String placeId, double longitude, double latitude){
        this.placeId = placeId;
        this.longitude = longitude;
        this.latitude = latitude;
        GetJSONFromWeb(3);
        ParseJSON();
    }

    public int GetResult() { return result; }

    // 출발점을 지정하는 메서드
    public String GetOrigin() {
        try {
            String str = new String();
            str = "origin=" + this.latitude + "," + this.longitude;
            return str;
        }
        catch(Exception e) {
            System.out.println(e + "AskGoogle의 GetOrigin에서 오류");
            return null ;
        }
    }

    // 도착점을 지정하는 메서드
    public String GetDestination() {
        try {
            String str = new String();

            str = "&destination=place_id:" + this.placeId;
            return str;
        }
        catch(Exception e) {
            System.out.println(e + "AskGoogle의 GetDestination에서 오류");
            return null ;
        }
    }


    // mode = 1 자동차, mode = 2 도보 , mode = 3 대중교통(한국은 3밖에 지원이 안되여..)
    public boolean GetJSONFromWeb(int mode) {
        String basic = "https://maps.googleapis.com/maps/api/directions/json?";
        String urlstring = basic + GetOrigin() + GetDestination();

        // mode를 설정하는 곳
        String modeurl = new String();
        if(mode == 1)
            modeurl = "&mode=driving";
        else if(mode == 2)
            modeurl = "&mode=walking";
        else
            modeurl = "&mode=transit";
        String finalurl = urlstring + modeurl+ mykey;
        // System.out.println(finalurl);  // 테스트용 코드

        try {

            new AskGoogle.DownloadJson().execute(finalurl).get();

            // StreamData를 JSON Object로 저장한다.
            obj = new JSONObject(getpage);
            if(!obj.getString("status").equals("OK"))
                return false;

            return true;

        }
        catch(Exception e) {
            System.out.println(e + "AskGoogle의 GetJSONFromWeb에서 오류");
            return false;
        }
    }

    // JSON Object를 Parsing하는 메서드이다.
    public int ParseJSON() {
        try {
            // JSON에서 Duration 파트를 Parsing 해온다.
            JSONArray legs = obj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            JSONObject leg = legs.getJSONObject(0);
            int distance = leg.getJSONObject("duration").getInt("value");
            this.result = distance;
            return distance;
        }
        catch(Exception e) {
            System.out.println(e + "AskGoogle의 ParseJSON에서 오류");
            return -1;
        }
    }


    // DownloadJson 하위 클래스는 스레드이다. doInBackGround의 작업이 스레드로 실행되게 된다.
    private class DownloadJson extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... arg0) {
            try {
                return GetData(arg0[0]);
            }
            catch (Exception e) {
                return "JSON Download Failed";
            }
        }

        protected void onPostExecute(String result){
            ;
        }

        private String GetData(String finalurl) {
            try {
                URL googleurl = new URL(finalurl);
                HttpURLConnection con = (HttpURLConnection) googleurl.openConnection();

                Scanner scan = new Scanner(con.getInputStream());
                while (scan.hasNext())
                    getpage += scan.nextLine();
                scan.close();
                System.out.println("Test2 : " + googleurl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getpage;
        }
    }


    // 참고 사이트
    // http://theoryapp.com/parse-json-in-java/
    // http://aroundck.tistory.com/215
    // https://calyfactory.github.io/%EC%A0%9C%EC%9D%B4%EC%8A%A8%ED%8C%8C%EC%8B%B1/
    // https://gist.github.com/mopkaloppt/c17620744f01304242f3
}

