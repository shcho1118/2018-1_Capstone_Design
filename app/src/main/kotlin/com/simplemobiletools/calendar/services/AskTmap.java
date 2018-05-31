package com.simplemobiletools.calendar.services;

import com.simplemobiletools.calendar.activities.MainActivity;
import com.skt.Tmap.TMapTapi;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

import android.app.Service;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static java.lang.Thread.sleep;


public class AskTmap extends AsyncTask<Void, Void, Integer> {

        Document pathDocument;
        TMapData data = new TMapData();
        TMapPoint start;
        TMapPoint end;
        int totalTime = 0;
        int movingmethod = 0;

        public AskTmap() {
        }

        public AskTmap(double start_lat, double start_lon, double end_lat, double end_lon, int movingmethod) {
            start = new TMapPoint(start_lat, start_lon);
            end = new TMapPoint(end_lat, end_lon);
            this.movingmethod = movingmethod;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {//실제로 background로 돌아가는 부분
            int result = getPathDataInDocument();//소요시간을 계산하는 함수입니다! 처음에 이름지은거라 getPathDataInDocument라고 이름지었어요ㅋㅋㅋㅋ
            return result;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Integer aVoid) {
            super.onPostExecute(1);
        }

        public int getPathDataInDocument() {//start에서 end까지 소요시간을 계산하는 함수
            //findPathDataAllType라는 함수가 두번나오는데 함수의 첫번째 인자값에 따라 자동차용, 도보용으로 나누어져요!
            //앱에서 자동차인지 도보인지 선탣하면 그 값에따라 if문으로 두 함수를 가각 나눠놓으면 될꺼 같아요
            //findPathDataAllType의 두번째인자와 세번째인자는 각각 시작점과 도착점 마지막 꺼는 무조건 new TMapData.FindPathDataAllListenerCallback()를 넣으면 됩니다!

            if (movingmethod == 2) {
                data.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, start, end, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        Element root = document.getDocumentElement();
                        NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                        for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                            NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                            for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                                if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:time")) {
                                    totalTime += Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim());
                                }
                            }
                        }
                        Log.d("location Tmap", "car성공! : " + totalTime);//계산된 소요시간 출력

                    }
                }); try{Thread.sleep(1000);} catch(Exception e){}; return totalTime;
            } else if (movingmethod == 1) {
                data.findPathDataAllType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end, new TMapData.FindPathDataAllListenerCallback() {
                    @Override
                    public void onFindPathDataAll(Document document) {
                        Element root = document.getDocumentElement();
                        System.out.println("pedestrian");
                        NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");
                        for (int i = 0; i < nodeListPlacemark.getLength(); i++) {
                            NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();
                            for (int j = 0; j < nodeListPlacemarkItem.getLength(); j++) {
                                if (nodeListPlacemarkItem.item(j).getNodeName().equals("tmap:time")) {
                                    totalTime += Integer.parseInt(nodeListPlacemarkItem.item(j).getTextContent().trim());
                                }
                            }
                        }
                        Log.d("location Tmap", "pedestrian성공! : " + totalTime);//계산된 소요시간 출력
                    }
                }); try{Thread.sleep(1000);} catch(Exception e){}; return totalTime;
            }
            return -100;
        }
}
