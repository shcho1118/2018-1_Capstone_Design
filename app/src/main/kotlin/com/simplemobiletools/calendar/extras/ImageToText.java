package com.simplemobiletools.calendar.extras;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.simplemobiletools.calendar.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ImageToText {
    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;
    private static final String TAG = ImageToText.class.getSimpleName();
    private static Context context;
    private Class targetClass;

    public ImageToText(Context context, Class targetClass) {
        this.context = context;
        this.targetClass = targetClass;
    }

    private static void Save_jsonFiles(String json) {
        try {
            Date today = new Date(); // 현재 시간을 가리키는 변수
            SimpleDateFormat now = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss_a"); // 새로운 포맷으로 현재 시간을 출력함
            TimeZone tz;
            tz = TimeZone.getTimeZone("Asia/Seoul");
            now.setTimeZone(tz);

            String fileName = now.format(today) + "_analysis_code.json";
            String dirPath = context.getFilesDir() + "/" + fileName;
            FileWriter writeFile = new FileWriter(dirPath);

            System.out.println("path name : " + dirPath);

            writeFile.write(json);
            writeFile.flush();
            writeFile.close();

            InputStream Is = new FileInputStream(dirPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(Is, "UTF-8"));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    // ( 8 )
    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        String json_code = "";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null) {
            // texts의 첫번째 인덱스에 저장된 값을 가져옴
            json_code = analysisStrings(texts.get(0).getDescription());
        } else {
            System.out.println("nothing");
        }

        return json_code;
    }

    // ( 9 )
    private static String analysisStrings(String text) {
        System.out.println("AnalysisStrings 함수 진입");
        text = text.replace('\n', '.');

        StringBuilder sb = new StringBuilder();
        HttpURLConnection myConnection = null;

        // 언어 분석 API를 제공하는 URL
        try {
            URL homePage = new URL("http://aiopen.etri.re.kr:8000/WiseNLU");
            myConnection = (HttpURLConnection) homePage.openConnection();

            // Set some headers to inform server about the type of the content
            myConnection.setRequestMethod("POST"); // ETRI API는 POST 방식으로 호출해야함
            myConnection.setRequestProperty("Accept", "application/json");
            myConnection.setRequestProperty("Content-type", "application/json");

            myConnection.setDoOutput(true); // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션
            myConnection.setDoInput(true); // InputStream으로 서버로 부터 응답을 받겠다는 옵션

            myConnection.connect();

            // Create JSONObject here
            JSONObject jsonObject = new JSONObject();
            JSONObject Inner_object = new JSONObject();
            Inner_object.put("analysis_code", "srl");
            Inner_object.put("text", text);

            jsonObject.put("access_key", "3d8e966a-55d2-4022-bcf7-c6c78a6915d7");
            jsonObject.put("argument", Inner_object);

            OutputStreamWriter out = new OutputStreamWriter(myConnection.getOutputStream());
            out.write(jsonObject.toString());
            out.close();

            int HttpResult = myConnection.getResponseCode();
            InputStream inputstream = myConnection.getInputStream();

            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        inputstream, "UTF-8"));

                String result;
                while ((result = br.readLine()) != null) {
                    sb.append(result + "\n");
                }
                System.out.println(sb.toString());
                br.close();

                String string = sb.toString();
                JSONObject jsonObj = new JSONObject(string);

                return jsonObj.toString(4);
            } else {
                System.out.println("HTTP_NOT_OK");
                System.out.println(myConnection.getResponseMessage());
            }
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException error");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException error");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("JSONException error");
            e.printStackTrace();
        } finally {
            if (myConnection != null) {
                myConnection.disconnect();
            }
        }

        return "Error\n";
    }

    // ( 5 )
    public void callCloudVision(final Bitmap bitmap) {
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LabelDetectionTask(prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
        }
    }

    // ( 6 )
    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest) throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = context.getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);
                        String sig = PackageManagerUtils.getSignature(context.getPackageManager(), packageName);
                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("TEXT_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    // ( 7 )
    private static class LabelDetectionTask extends AsyncTask<Object, Void, String> {
        private Vision.Images.Annotate mRequest;

        LabelDetectionTask(Vision.Images.Annotate annotate) {
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);
            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " + e.getMessage());
            }

            return "Cloud Vision API request failed. Check logs for details.";
        }

        @Override
        protected void onPostExecute(String result) { // 백그라운드 스레드의 작업을 완료한 후 실행하는 부분
            Save_jsonFiles(result);
        }
    }
}
