package com.simplemobiletools.calendar.extras;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;

public class ExtractTimeLocation {
    private Context context;
    private Class targetClass;
    private JSONObject json_result;
    private final String TAG = ExtractTimeLocation.class.getSimpleName();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private String parsedLoc = "";
    private Calendar parsedTime = Calendar.getInstance();
    private String parsedDescription = "";
    private long parsedDuration = 0; // 분 단위
    private String TTA_TIME[] = {
            "DT_TOHERS", "DT_YEAR", "DT_MONTH", "DT_DAY",
            "TI_OTHERS", "TI_HOUR", "TI_MINUTE", "TI_SECOND"
    };
    private String TTA_LOC[] = {
            "LC_OTHERS", "LCP_COUNTRY", "LCP_PROVINCE", "LCP_COUNTY", "LCP_CITY",
            "LCP_CAPITALCITY", "LCG_RIVER", "LCG_OCEAN", "LCG_BAY", "LCG_MOUNTAIN",
            "LCG_ISLAND", "LCG_CONTINENT", "LC_TOUR", "LC_SPACE", "OG_OTHERS",
            "OGG_ECONOMY", "OGG_EDUCATION", "OGG_MILITARY", "OGG_MEDIA", "OGG_SPORTS",
            "OGG_ART", "OGG_MEDICINE", "OGG_RELIGION", "OGG_SCIENCE", "OGG_LIBRARY",
            "OGG_LAW", "OGG_POLITICS", "OGG_FOOD", "OGG_HOTEL", "QT_ORDER"
    };
    private String TTA_DURATION[] = {
            "DT_DURATION",
            "TI_DURATION"
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////


    public ExtractTimeLocation(Context context, Class targetClass, JSONObject json) {
        this.context = context;
        this.targetClass = targetClass;
        this.json_result = json;
    }

    public void analysisCode() {
        try {
            HashMap<String, String> loc = new HashMap<String, String>();
            HashMap<String, String> time = new HashMap<String, String>();
            HashMap<String, String> duration = new HashMap<String, String>();
            boolean isFind_loc = false;
            boolean isFind_time = false;

            JSONObject return_object = json_result.getJSONObject("return_object");
            JSONArray sentences = return_object.getJSONArray("sentence");
            int sentences_length = sentences.length();
            int i, j, k;

            for (i = 0; i < sentences_length; i++) {
                JSONObject sentence = sentences.getJSONObject(i);
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
                            parsedLoc += text;
                            isFind_loc = true;
                        }

                        if (type.equals("ARGM-TMP")) {
                            setParsedTime(text);
                            isFind_time = true;
                        }

                        if (type.equals("ARG1")) {
                            parsedDescription += text;
                        }
                    }

                    if(isFind_loc || isFind_time){
                        try{
                            parsedDescription += SRL.getJSONObject(j).getString("verb");
                        }
                        catch (JSONException e){
                            Log.e(TAG, "verb does not exist", e);
                        }
                        break;
                    }
                }

                int TTA_LOC_LENGTH = TTA_LOC.length;
                int TTA_TIME_LENGTH = TTA_TIME.length;
                int TTA_DURATION_LENGTH = TTA_DURATION.length;

                JSONArray NE = sentence.getJSONArray("NE");
                int NE_LENGTH = NE.length();

                for (j = 0; j < NE_LENGTH; j++) {
                    String type = NE.getJSONObject(j).getString("type");
                    String text = NE.getJSONObject(j).getString("text");

                    if (!isFind_loc) {
                        for (k = 0; k < TTA_LOC_LENGTH; k++) {
                            if (type.equals(TTA_LOC[k])) {
                                loc.put(type, text);
                                isFind_loc = true;
                            }
                        }

                        for (String key : loc.keySet()){
                            parsedLoc = parsedLoc + " " + loc.get(key);
                        }
                    }

                    if (!isFind_time) {
                        for (k = 0; k < TTA_TIME_LENGTH; k++) {
                            if (type.equals(TTA_TIME[k])) {
                                time.put(type, text);
                                isFind_time = true;
                            }
                        }

                        String timeStr = "";

                        for (String key : time.keySet()){
                            timeStr += time.get(key);
                        }

                        setParsedTime(timeStr);
                    }

                    for(k = 0; k < TTA_DURATION_LENGTH; k++) {
                        if(type.equals(TTA_DURATION[k])){
                            duration.put(type, text);
                        }
                    }

                    String durationStr = "";

                    for(String key : duration.keySet()){
                        durationStr += duration.get(key);
                    }

                    setParsedDuration(durationStr);
                }
            }

            Log.d(TAG, parsedLoc);
            Log.d(TAG, parsedTime.toString());
            Log.d(TAG, parsedDescription);
            Log.d(TAG, String.valueOf(parsedDuration));
        } catch (JSONException e) {
            Log.e(TAG, "analysisCode is not working properly", e);
        }
    }

    private int parseIntBefore(String key, String src) {
        String resultStr = "";

        if (src.contains(key)) {
            for (int i = src.lastIndexOf(key) - 1; i >= 0; i--) {
                if (src.charAt(i) == ' ') {
                    continue;
                } else if (src.charAt(i) >= '0' && src.charAt(i) <= '9') {
                    resultStr = src.charAt(i) + resultStr;
                } else {
                    break;
                }
            }
            if (resultStr.equals(""))
                return -1;
            else
                return Integer.parseInt(resultStr);
        } else {
            return -1;
        }
    }

    private void setParsedTime(String timeStr) {
        int year = parseIntBefore("년", timeStr);
        int month = parseIntBefore("월", timeStr);
        int day = parseIntBefore("일", timeStr);
        int hour = parseIntBefore("시", timeStr);
        int minute = parseIntBefore("분", timeStr);
        if (year > 0)
            parsedTime.set(Calendar.YEAR, year);
        if (month > 0)
            parsedTime.set(Calendar.MONTH, month);
        if (day > 0)
            parsedTime.set(Calendar.DATE, day);
        if (hour > 0)
            parsedTime.set(Calendar.HOUR, hour);
        if (minute > 0)
            parsedTime.set(Calendar.MINUTE, minute);
        if (timeStr.contains("반"))
            parsedTime.set(Calendar.MINUTE, 30);
    }

    private void setParsedDuration(String durationStr) {
        int year = parseIntBefore("년", durationStr);
        int month = parseIntBefore("월", durationStr);
        int day = parseIntBefore("일", durationStr);
        int hour = parseIntBefore("시", durationStr);
        int minute = parseIntBefore("분", durationStr);
        if (year > 0)
            parsedDuration += 525000 * year;
        if (month > 0)
            parsedDuration += 43200 * month;
        if (day > 0)
            parsedDuration += 1440 * day;
        if (hour > 0)
            parsedDuration += 60 * hour;
        if (minute > 0)
            parsedDuration += minute;
    }
}
