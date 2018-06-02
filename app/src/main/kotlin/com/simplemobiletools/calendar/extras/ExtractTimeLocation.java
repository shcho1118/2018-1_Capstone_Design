package com.simplemobiletools.calendar.extras;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class ExtractTimeLocation {
    private Context mContext;
    private Class mClass;
    private JSONObject mJSONObject;
    private Bitmap mBitmap;
    private static final String TAG = ExtractTimeLocation.class.getSimpleName();

    public ExtractTimeLocation(Context mContext, Class mClass, JSONObject mJSONObject, Bitmap mBitmap){
        this.mContext = mContext;
        this.mClass = mClass;
        this.mJSONObject = mJSONObject;
        this.mBitmap = mBitmap;
    }

    public void generateNewEvent(){
        ExtractRule1 extractRule1 = new ExtractRule1(mJSONObject);
        ExtractRule2 extractRule2 = new ExtractRule2(mJSONObject);
        String parsedLoc = null;
        Calendar parsedTime = null;
        String parsedDescription = null;
        long parsedDuration = 0;

        extractRule1.analysisCode();
        extractRule2.analysisCode();
        parsedLoc = extractRule1.getParsedLoc();
        if(parsedLoc == null) parsedLoc = extractRule2.getParsedLoc();
        parsedTime = extractRule1.getParsedTime();
        parsedDescription = extractRule2.getParsedDescription();
        parsedDuration = extractRule2.getParsedDuration();

        Log.d(TAG, "parsedLoc is " + parsedLoc);
        Log.d(TAG, "parsedTime is " + parsedTime.getTime().toString());
        Log.d(TAG, "parsedDescription is " + parsedDescription);
        Log.d(TAG, "parsedDuration is " + parsedDuration);

        Intent targetActivity = new Intent(mContext, mClass);
        targetActivity.setAction(Intent.ACTION_INSERT);
        long startTs = parsedTime.getTimeInMillis();
        long endTs = startTs + parsedDuration * 60 * 1000;
        targetActivity.putExtra("title", parsedLoc + " " + parsedDescription);
        targetActivity.putExtra("beginTime", startTs);
        targetActivity.putExtra("endTime", endTs);
        targetActivity.putExtra("eventLocation", parsedLoc);
        targetActivity.putExtra("description", parsedDescription);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        targetActivity.putExtra("croppedImage", byteArray);
        mContext.startActivity(targetActivity);
    }
}
