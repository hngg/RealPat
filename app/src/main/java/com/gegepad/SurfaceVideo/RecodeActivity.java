package com.gegepad.SurfaceVideo;


import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.gegepad.SurfaceVideo.camera.FocusImageView;
import com.gegepad.SurfaceVideo.camera.ImageManager;
import com.gegepad.SurfaceVideo.camera.SensorControler;
import com.gegepad.SurfaceVideo.camera.ShutterButton;
import com.gegepad.SurfaceVideo.camera.ThumbnailController;
import com.gegepad.SurfaceVideo.camera.Util;
import com.gegepad.modtrunk.media.encodec.ICodecFrame;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class RecodeActivity extends AppCompatActivity implements  SurfaceHolder.Callback, ICodecFrame, View.OnClickListener, ShutterButton.OnShutterButtonListener {
    private static final String TAG = "RecodeActivity";

    final String YYYY_MM_DD_HH_MM_SS = "yyyyMMdd_HHmmss";

    private boolean mShowface = false, mRecording = false;

    private SurfaceView textureView;
    private Surface mSurface;
    int mWidth,mHeight;


    //camera focus
    private SensorControler mSensorControler;
    private FocusImageView mFocusImageView;

    //camera takephoto
    private ContentResolver mContentResolver;

    //button controler
    private ShutterButton mShutterButton;
    private ImageView mLastPictureButton;
    private ThumbnailController mThumbController;
    private ImageView mbtExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recode);

        textureView = (SurfaceView)findViewById(R.id.surface_view);
        textureView.getHolder().addCallback(this);

        initView();
    }

    private void initView()
    {
        mFocusImageView = findViewById(R.id.focusImageView);

        // Initialize shutter button.
        mShutterButton = (ShutterButton) findViewById(R.id.shutter_button);
        mShutterButton.setOnShutterButtonListener(this);
        mShutterButton.setVisibility(View.VISIBLE);

        mbtExit = findViewById(R.id.iv_exit);
        mbtExit.setOnClickListener(this);

        // Initialize last picture button.
        mLastPictureButton = findViewById(R.id.review_thumbnail);//(ImageView)
        mLastPictureButton.setOnClickListener(this);

        mContentResolver = getContentResolver();
        mThumbController = new ThumbnailController( getResources(), mContentResolver);
        Drawable draw = mThumbController.getThumbnailDrawble(ImageManager.getLastImageThumbPath(), 100, 100);
        mLastPictureButton.setImageDrawable(draw);

        initFocusView();
    }

    private void initFocusView()
    {
        if(mSensorControler==null)
            mSensorControler = new SensorControler(this);

        mSensorControler.setCameraFocusListener(
            new SensorControler.CameraFocusListener()
            {
                @Override
                public void onFocus() {
                    int screenWidth = GApplication.SCREEN_WIDTH;
                    Point point = new Point(screenWidth / 2, screenWidth / 2);
                    Log.e(TAG, "setCameraFocusListener");
                    onCameraFocus(point, false);
                }
            });
    }

    private void updateFocus()
    {
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        //Point point = new Point(mDisplayMetrics.widthPixels/2, mDisplayMetrics.heightPixels/2+50);
        DeviceServiceManager.instance().cameraUpdateFocus(mDisplayMetrics.widthPixels/2, mDisplayMetrics.heightPixels/2);
        //onFocus(point, autoFocusCallback);
        //mCameraOne.updateFocus(mDisplayMetrics.widthPixels/2, mDisplayMetrics.heightPixels/2);
    }

    /**
     * 相机对焦
     *
     * @param point
     * @param needDelay 是否需要延时
     */
    public void onCameraFocus(final Point point, boolean needDelay)
    {
        long delayDuration = needDelay ? 300 : 0;

//        mHandler.postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                if (!mSensorControler.isFocusLocked())
//                {
//                    if (mCameraOne.updateFocus(point.x, point.y, autoFocusCallback))
//                    {
//                        mSensorControler.lockFocus();
//                        mFocusImageView.startFocus(point);
//                    }
//                }
//            }
//        }, delayDuration);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width,int height) {
        mSurface = surfaceHolder.getSurface();
        mWidth = width;
        mHeight = height;

        if (mShowface == false) {
            DeviceServiceManager.instance().surfaceChanged(mSurface, mWidth, mHeight);
            updateFocus();
            mShowface = true;
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if(mShowface) {
            DeviceServiceManager.instance().surfaceDestroy();
            mShowface = false;
        }
    }

    public void recode1(View view) {
        if (mRecording == false) {
            //DeviceServiceManager.instance().st
            mRecording = true;
        } else {
            //stopRecode();
            mRecording = false;
        }
    }


    private void checkAnddeleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.review_thumbnail:
                viewLastImage();
                break;

            case R.id.iv_exit:
                finish();
                break;

            case R.id.btn_camera_switch:
                //switchCameraId();
                break;

            case R.id.btn_flashlight:
                //switchToVideoMode();
                break;
        }
    }

    private void viewLastImage()
    {
        if (mThumbController.isUriValid()) {
            Intent intent = new Intent(Util.REVIEW_ACTION, mThumbController.getUri());
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW, mThumbController.getUri());
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "review image fail", e);
                }
            }
        } else {
            Log.e(TAG, "Can't view last image.");
        }
    }

    @Override
    public int onVideoCodec(ByteBuffer buffer, MediaCodec.BufferInfo buffInfo) {
        Log.d(TAG, "size:"+ buffInfo.size);
        return 0;
    }

    @Override
    public int onVideoChange(MediaFormat format) {
        return 0;
    }

    @Override
    public int onAudioCodec() {
        return 0;
    }

    @Override
    public void onShutterButtonFocus(ShutterButton b, boolean pressed) {

    }

    @Override
    public void onShutterButtonClick(ShutterButton button) {
        switch (button.getId()) {
            case R.id.shutter_button:
                if (mRecording == false) {
                    SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
                    String date = format.format(new Date());
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM/Camera/GG_"+date+".mp4";
                    //checkAnddeleteFile(filePath);
                    DeviceServiceManager.instance().startRecode(filePath);
                    mRecording = true;
                    Log.e(TAG, "onShutterButtonClick start path:"+filePath);
                } else {
                    DeviceServiceManager.instance().stopRecode();
                    mRecording = false;
                    Log.e(TAG, "onShutterButtonClick stop");
                }

                break;
        }

    }
}
