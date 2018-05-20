package com.simplemobiletools.calendar.activities;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;

public class CurrentModule_Class {


    // GetPlaceData는 입력받은 문자열을 이용해 위치를 자동완성하고 그에 따른 Place ID를 반환하는 역할을 합니다.
    public class GetPlaceData {

        private String mykey = "&key=AIzaSyADOyPOEzJ3AXLBBFtWpvfxPozZYRbs0Y8";
        private JSONObject obj;
        private String input;
        private String getpage = new String();
        private ArrayList<String> placename = new ArrayList<String>();
        private ArrayList<String> placeid = new ArrayList<String>();

        // 생성자
        GetPlaceData(){}
        GetPlaceData(String input){
            this.input = input;
            if(GetJSONData() == false)
                placename.add(null);    // 잘못된 검색일 경우 null을 넣어준다.
            GetListFromJSON();
        }

        public String GetPlaceName(int n){
            return placename.get(n);
        }

        public int GetPlaceNum() { return placename.size(); }

        public String GetPlaceid(int n){
            return placeid.get(n);
        }

        // 만들어진 웹 주소를 이용하여 JSON Object를 만드는 메서드이다.
        // 이 메서드는 네트워크 연결을 필요로 하므로 밑의 DownloadJson과 함께 작동하게 된다.
        public boolean GetJSONData(){
            try {
                String basic = "https://maps.googleapis.com/maps/api/place/autocomplete/json?language=ko&";
                String finalurl = basic + "input=" + URLEncoder.encode(input,"utf-8") + mykey;

                System.out.println(finalurl); // 테스트용 코드

                new DownloadJson().execute(finalurl).get();

                // StreamData를 JSON Object로 저장한다.
                obj = new JSONObject(getpage);

                if (!obj.getString("status").equals("OK"))
                    return false;

                return true;
            }
            catch(Exception e) {
                System.out.println(e + "GetPlaceData의 GetJSONData에서 오류. Input = " + input);
                return false ;
            }
        }

        // JSON Data를 Parsing하여 Place 이름과 ID를 얻어내는 메서드이다.
        public String GetListFromJSON() {
            try {
                //JSON에서 description과 place_id 파트를 Parsing 하고 그 리스트를 만든다.
                JSONArray legs = obj.getJSONArray("predictions");
                for(int i = 0; i < legs.length(); i++){
                    JSONObject leg = legs.getJSONObject(i);
                    placename.add(leg.getString("description"));
                    placeid.add(leg.getString("place_id"));
                }
                return placeid.get(0);
            }
            catch(Exception e) {
                System.out.println(e + "GetPlaceData의 GetListFromJSON에서 오류");
                return null ;
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

                    System.out.println("Test1 : " + googleurl);
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
    }
}





