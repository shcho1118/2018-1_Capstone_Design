package com.simplemobiletools.calendar.services;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.simplemobiletools.calendar.activities.CropActivity;
import com.simplemobiletools.calendar.extras.ImageTransmogrifier;

import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectorService extends Service {
    public static final String EXTRA_RESULT_CODE = "resultCode";
    public static final String EXTRA_RESULT_INTENT = "resultIntent";
    public static final String IMAGE_NAME = "ORIGINAL_IMAGE.png";
    static final int VIRT_DISPLAY_FLAGS =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY |
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    final private HandlerThread handlerThread = new HandlerThread(getClass().getSimpleName(),
            android.os.Process.THREAD_PRIORITY_BACKGROUND);
    private MediaProjection projection;
    private VirtualDisplay vdisplay;
    private Handler handler;
    private AtomicReference<Bitmap> latestBmp = new AtomicReference<Bitmap>();
    private MediaProjectionManager mgr;
    private WindowManager wmgr;
    private ImageTransmogrifier it;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        wmgr = (WindowManager) getSystemService(WINDOW_SERVICE);

        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            Log.e("ProjectorService", "Waiting is failed", e);
        }

        projection =
                mgr.getMediaProjection(i.getIntExtra(EXTRA_RESULT_CODE, -1),
                        (Intent) i.getParcelableExtra(EXTRA_RESULT_INTENT));

        it = new ImageTransmogrifier(this);

        MediaProjection.Callback cb = new MediaProjection.Callback() {
            @Override
            public void onStop() {
                vdisplay.release();
            }
        };

        vdisplay = projection.createVirtualDisplay("andprojector",
                it.getWidth(), it.getHeight(),
                getResources().getDisplayMetrics().densityDpi,
                VIRT_DISPLAY_FLAGS, it.getSurface(), null, handler);
        projection.registerCallback(cb, handler);

        return (START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        try {
            FileOutputStream png = openFileOutput(IMAGE_NAME, MODE_PRIVATE);
            latestBmp.get().compress(Bitmap.CompressFormat.PNG, 100, png);
            png.close();
            Intent cropActivity = new Intent(this, CropActivity.class);
            startActivity(cropActivity);
        } catch (Exception e) {
            Log.e("ProjectorService", "Capture is failed", e);
        } finally {
            Toast.makeText(this, "Capture is done", Toast.LENGTH_LONG).show();
        }
        projection.stop();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        ImageTransmogrifier newIt
                = new ImageTransmogrifier(this);

        if (newIt.getWidth() != it.getWidth() ||
                newIt.getHeight() != it.getHeight()) {
            ImageTransmogrifier oldIt = it;

            it = newIt;
            vdisplay.resize(it.getWidth(), it.getHeight(),
                    getResources().getDisplayMetrics().densityDpi);
            vdisplay.setSurface(it.getSurface());

            oldIt.close();
        }
    }

    public WindowManager getWindowManager() {
        return (wmgr);
    }

    public Handler getHandler() {
        return (handler);
    }

    public void updateImage(Bitmap original) {
        latestBmp.set(original);
        stopSelf();
    }
}
