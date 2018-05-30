package com.simplemobiletools.calendar.extras;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ExtractRule1 {
    private static JSONObject json_result;

    private Calendar final_time = new GregorianCalendar(Locale.KOREA);
    private String Final_loc = null;


    private static final Set<String> TTA_TIME_TI =
            new HashSet<>(Arrays.asList(
                    "TI_OTHERS", "TI_HOUR", "TI_MINUTE", "TI_SECOND"
            ));

    private static final Set<String> TTA_TIME_DT =
            new HashSet<>(Arrays.asList(
                    "DT_OTHERS", "DT_DAY", "DT_MONTH", "DT_YEAR"
            ));

    private static final Set<String> TTA_LOC =
            new HashSet<>(Arrays.asList(
                    "LC_OTHERS", "LCP_COUNTRY", "LCP_PROVINCE", "LCP_COUNTY", "LCP_CITY",
                    "LCP_CAPITALCITY", "LCG_RIVER", "LCG_OCEAN", "LCG_BAY", "LCG_MOUNTAIN",
                    "LCG_ISLAND", "LCG_CONTINENT", "LC_TOUR", "LC_SPACE", "OG_OTHERS",
                    "OGG_ECONOMY", "OGG_EDUCATION", "OGG_MILITARY", "OGG_MEDIA", "OGG_SPORTS",
                    "OGG_ART", "OGG_MEDICINE", "OGG_RELIGION", "OGG_SCIENCE", "OGG_LIBRARY",
                    "OGG_LAW", "OGG_POLITICS", "OGG_FOOD", "OGG_HOTEL", "QT_ORDER"
            ));

    private static final Set<String> when_later = new HashSet<>(Arrays.asList("오후", "저녁", "밤"));
    private static HashMap<String, Integer> day_of_week = new HashMap<>();

    public ExtractRule1(JSONObject json) {
        this.json_result = json;

        day_of_week.put("일요일", 1);
        day_of_week.put("월요일", 2);
        day_of_week.put("화요일", 3);
        day_of_week.put("수요일", 4);
        day_of_week.put("목요일", 5);
        day_of_week.put("금요일", 6);
        day_of_week.put("토요일", 7);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getParsedLoc(){
        return Final_loc;
    }

    public Calendar getParsedTime(){
        return final_time;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////

    private int integer_from_string(String str) {
        return Integer.parseInt(str.replaceAll("[^0-9]+", ""));
    }

    public void analysisCode() {
        try {
            final_time.setTime(new Date());


            // JSONObject : { ... }
            JSONObject return_object = json_result.getJSONObject("return_object");
            //System.out.println(return_object + "\n\n");

            // JSONArray : [ ... ]
            JSONArray sentences = return_object.getJSONArray("sentence");

            int sentences_length = sentences.length();
            boolean is_later = false;          // 시간에서 "오후", "밤", "저녁" 등의 단어가 나올 시 true로 바뀜

            int i, j, k;
            // ImageToText에 들어온 문장의 개수만큼 분석함
            for (i = 0; i < sentences_length; i++) {
                JSONObject sentence = sentences.getJSONObject(i);
                int base_hour = 0;

                System.out.println("text" + sentence.getString("text"));

                // 개체명 분석 시작점

                JSONArray NE = sentence.getJSONArray("NE");
                int NE_LENGTH = NE.length();
                for (j = 0; j < NE_LENGTH; j++) {
                    String type = NE.getJSONObject(j).getString("type");
                    String text = NE.getJSONObject(j).getString("text");

                    // 시간 정보 (시간)
                    if (TTA_TIME_TI.contains(type)) {
                        String[] token_list = text.split(" ");
                        int token_length = token_list.length;
                        for (k = 0; k < token_length; k++) {
                            String cur_token = token_list[k];
                            if (when_later.contains(cur_token)) {
                                is_later = true;
                                base_hour += 12;
                            } else { // 오후, 오전 등의 개념이 아닐때(기본값은 오전임)
                                try {
                                    if (cur_token.contains("시")) {
                                        int hour = integer_from_string(cur_token);
                                        if (hour == 12 && is_later) {
                                            // 오후 12시인 경우 더해주면 안됨
                                            final_time.set(Calendar.HOUR_OF_DAY, 12); // 12시로 맞춰줌
                                        } else {
                                            base_hour += hour;
                                            final_time.set(Calendar.HOUR_OF_DAY, base_hour); // (base_hour)로 맞춰줌
                                        }
                                    } else if (cur_token.contains("시간") && // 3시간 "뒤"에 만나자
                                            (k != token_length - 1 && (token_list[k + 1].equals("뒤") || (token_list[k + 1].equals("후"))))) {
                                        // 3시간 뒤에, 3시간 후에 만나자
                                        final_time.add(Calendar.HOUR, integer_from_string(cur_token));
                                    } else if (cur_token.contains("분")) {
                                        // 3시간 30분 뒤에 만나자, 50분 뒤에 만나자...
                                        if (k != token_length - 1 && (token_list[k + 1].equals("뒤") || (token_list[k + 1].equals("후")))) {
                                            final_time.add(Calendar.MINUTE, integer_from_string(cur_token));
                                        }
                                        // 30분에 만나자
                                        else {
                                            final_time.set(Calendar.MINUTE, integer_from_string(cur_token));
                                        }
                                    }
                                } catch (java.lang.NullPointerException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    // 시간 정보 (날짜)
                    else if (TTA_TIME_DT.contains(type)) {
                        String[] token_list = text.split(" ");
                        int token_length = token_list.length;
                        for (k = 0; k < token_length; k++) {
                            String cur_token = token_list[k];
                            if ((cur_token.equals("이번주")  && (k != token_length - 1 && day_of_week.containsKey(token_list[k + 1])))) { // 이번주 x요일에 만나자
                                // 해당되는 주가 올때까지 이동함
                                int dow = final_time.get(Calendar.DAY_OF_WEEK); // 현재 요일을 가져옴
                                int isSame = day_of_week.get(token_list[k + 1]);
                                if (dow < isSame) { // 이동해야 하는 요일이 앞에 있다면(화, 수, 목, 금, 토)
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, 1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            break;
                                        }
                                    }
                                } else if (isSame == 1) { // 일요일
                                    final_time.set(Calendar.DAY_OF_WEEK, 1);
                                    final_time.add(Calendar.DATE, 7);
                                }
                            }
                            else if (cur_token.equals("이번") && (k != token_length - 1 && token_list[k + 1].equals("주")) &&
                                    (k != token_length - 2 && day_of_week.containsKey(token_list[k + 2]))) {
                                // 해당되는 주가 올때까지 이동함
                                int dow = final_time.get(Calendar.DAY_OF_WEEK); // 현재 요일을 가져옴
                                int isSame = day_of_week.get(token_list[k + 2]);
                                if (dow < isSame) { // 이동해야 하는 요일이 앞에 있다면(화, 수, 목, 금, 토)
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, 1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            break;
                                        }
                                    }
                                } else if (isSame == 1) { // 일요일
                                    final_time.set(Calendar.DAY_OF_WEEK, 1);
                                    final_time.add(Calendar.DATE, 7);
                                }
                            }
                            // 다음주 x요일에 만나자. 다음 주 x요일에 만나자.
                            else if ((cur_token.equals("다음주") && (k != token_length - 1 && day_of_week.containsKey(token_list[k + 1])))) {
                                int dow = final_time.get(Calendar.DAY_OF_WEEK); // 현재 요일을 가져옴
                                int isSame = day_of_week.get(token_list[k + 1]);

                                if (dow < isSame) { // 이동해야 하는 요일이 앞에 있다면
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, 1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            final_time.add(Calendar.DATE, 7);
                                            break;
                                        }
                                    }
                                } else if (dow == isSame) { // 이동해야 하는 요일이 바로 7일 뒤라면
                                    final_time.add(Calendar.DATE, 7);
                                } else {
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, -1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            final_time.add(Calendar.DATE, 7);
                                            break;
                                        }
                                    }
                                }
                            }
                            else if ((cur_token.equals("다음") && (k != token_length - 1 && token_list[k + 1].equals("주")) &&
                                    (k != token_length - 2 && day_of_week.containsKey(token_list[k + 2])))) {
                                int dow = final_time.get(Calendar.DAY_OF_WEEK); // 현재 요일을 가져옴
                                int isSame = day_of_week.get(token_list[k + 2]);

                                if (dow < isSame) { // 이동해야 하는 요일이 앞에 있다면
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, 1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            final_time.add(Calendar.DATE, 7);
                                            break;
                                        }
                                    }
                                } else if (dow == isSame) { // 이동해야 하는 요일이 바로 7일 뒤라면
                                    final_time.add(Calendar.DATE, 7);
                                } else {
                                    while (true) {
                                        final_time.add(Calendar.DAY_OF_WEEK, -1);
                                        if (final_time.get(Calendar.DAY_OF_WEEK) == isSame) {
                                            final_time.add(Calendar.DATE, 7);
                                            break;
                                        }
                                    }
                                }
                            }
                            // contains(월요일, 화요일, 수요일 ....)
                            // 일요일 = 1, 월요일 = 2, 화요일 = 3, ......, 토요일 = 7
                            else if (day_of_week.containsKey(cur_token)) {
                                final_time.setFirstDayOfWeek(day_of_week.get(cur_token));
                            } else if (cur_token.contains("주")) {
                                try {
                                    // 3주 뒤에 만나자, 3주 후에 만나자
                                    if (k != token_length - 1 && (token_list[k + 1].equals("뒤") || token_list[k + 1].equals("후"))) {
                                        // 3주 뒤라면, 3 * 7을 해서 21일을 더해야함
                                        final_time.add(Calendar.DATE, 7 * integer_from_string(cur_token));
                                    }
                                } catch (java.lang.NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } else if (cur_token.contains("월")) { // 6월에, 7월에
                                try {
                                    final_time.set(Calendar.MONTH, integer_from_string(cur_token) - 1);
                                } catch (java.lang.NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } else if (cur_token.contains("일") && (!cur_token.contains("내일"))) {
                                if (cur_token.contains("내일")) {
                                    System.out.println("내일");
                                    final_time.add(Calendar.DATE, 1);
                                } else {
                                    try {
                                        // 4일 "뒤"에
                                        if (k != token_length - 1 && (token_list[k + 1].equals("뒤") || token_list[k + 1].equals("후"))) {
                                            final_time.add(Calendar.DATE, integer_from_string(cur_token));
                                        }
                                        // 4일에
                                        else {
                                            final_time.set(Calendar.DATE, integer_from_string(cur_token));
                                        }
                                    } catch (java.lang.NullPointerException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }

                // 개체명 분석 종료점

                // 의미역 분석 시작점

                JSONArray SRL = sentence.getJSONArray("SRL");

                int SRLArray_length = SRL.length();
                for (j = 0; j < SRLArray_length; j++) {
                    JSONArray args = SRL.getJSONObject(j).getJSONArray("argument");
                    int args_length = args.length();

                    for (k = 0; k < args_length; k++) {
                        JSONObject analysis_text = args.getJSONObject(k);
                        String type = analysis_text.getString("type");
                        String text = analysis_text.getString("text");

                        if (type.equals("ARGM-LOC")) {
                            String[] token_list = text.split(" ");
                            StringBuilder sb = new StringBuilder();
                            if (token_list.length > 1) {
                                String last_token = token_list[token_list.length - 1];
                                if (last_token.equals("앞") || last_token.equals("뒤") || last_token.equals("옆") || last_token.equals("근처")) {
                                    sb.append(token_list[0]);
                                    for (int index = 1; index < token_list.length - 1; index++) {
                                        sb.append(" ").append(token_list[index]);
                                    }

                                    text = sb.toString();
                                }
                            }
                            Final_loc = text;
                        }
                    }
                }

                // 의미역 분석 종료점

                SimpleDateFormat yyyy = new SimpleDateFormat("yyyy", Locale.KOREA); // 년
                SimpleDateFormat MM = new SimpleDateFormat("MM", Locale.KOREA); // 월
                SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.KOREA); // 일
                SimpleDateFormat HH = new SimpleDateFormat("HH", Locale.KOREA); // 시간
                SimpleDateFormat mm = new SimpleDateFormat("mm", Locale.KOREA); // 분

                System.out.println("\n\n**************************************************");
                System.out.println(final_time.getTime());
                System.out.println("년 : " + yyyy.format(final_time.getTime()));
                System.out.println("월 : " + MM.format(final_time.getTime()));
                System.out.println("일 : " + dd.format(final_time.getTime()));
                System.out.println("시 : " + HH.format(final_time.getTime()));
                System.out.println("분 : " + mm.format(final_time.getTime()));
                System.out.println("**************************************************");

                System.out.println("\n\n\n");
                if (Final_loc != null) {
                    System.out.println("최종 장소 정보");
                    System.out.println(Final_loc);
                }
            }
        } catch (org.json.JSONException e) {
            System.out.println("JSONException error in Save_jsonFiles function");
            e.printStackTrace();
        }
    }


}
