package com.simplemobiletools.calendar.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.simplemobiletools.calendar.extras.ImageToText;
import com.simplemobiletools.calendar.services.ProjectorService;
import com.theartofdev.edmodo.cropper.CropImage;

public class CropActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CropImage.activity(Uri.fromFile(getFileStreamPath(ProjectorService.IMAGE_NAME))).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                        this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG)
                        .show();
                try {
                    Bitmap cropped = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getUri());
                    ImageToText imageToText = new ImageToText(this, EventActivity.class);
                    imageToText.callCloudVision(cropped);
                }
                catch (Exception e){

                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
