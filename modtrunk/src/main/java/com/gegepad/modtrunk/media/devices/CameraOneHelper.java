package com.gegepad.modtrunk.media.devices;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.gegepad.modtrunk.media.util.DisplayUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraOneHelper {
    private SurfaceTexture surfaceTexture;
    private Camera mCamera;
    private static final String TAG = "CameraHelper";


    private int screenW, screenH;
    private int mPreviewWidth;
    private int mPreviewHeight;

    public CameraOneHelper(Context context) {
        screenW = DisplayUtil.getScreenW(context);
        screenH = DisplayUtil.getScreenH(context);
    }

    public void startCamera(SurfaceTexture surfaceTexture, int cameraId, int preWidth, int preHeight) {
        this.surfaceTexture = surfaceTexture;
        startCamera(cameraId, preWidth, preHeight);
    }

    private void startCamera(int cameraId, int previewWidth, int previewHeight) {
        try {
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewTexture(surfaceTexture);

            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//            parameters.setPreviewFormat(ImageFormat.NV21);

//            List<String> focusModes = parameters.getSupportedFocusModes();
//            //设置对焦模式
//            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO))
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            Camera.Size size = CameraUtil.findBestSizeValue(parameters.getSupportedPictureSizes(), screenW, screenH, 0.1f);
            parameters.setPictureSize(size.width, size.height);

            mPreviewWidth  = previewWidth;
            mPreviewHeight = previewHeight;
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            Log.w(TAG, "camera preview width:"+mPreviewWidth + " height:"+mPreviewHeight + " picture width:"+size.width + " height:"+size.height);

            // Give the camera a hint that we're recording video.  This can have a big
            // impact on frame rate.
            parameters.setRecordingHint(true);

            //parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);

            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopCameraAndRelease() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void changeCameraId(int cameraId) {
        stopCameraAndRelease();
        startCamera(cameraId, mPreviewWidth, mPreviewHeight);
    }

    public void autoFocus() {
        if (mCamera != null) {
            mCamera.autoFocus(null);
        }
    }


    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback()
    {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //聚焦之后根据结果修改图片
            if (success) {
            } else {
                //聚焦失败显示的图片，由于未找到合适的资源，这里仍显示同一张图片
            }
        }
    };

    public boolean updateFocus(int pixW, int pixH)
    {
        return updateFocus(mCamera, pixW, pixH, autoFocusCallback);
    }

    public boolean updateFocus(Camera camera, int pixW, int pixH, Camera.AutoFocusCallback callback)
    {
        if (camera == null) {
            return false;
        }

        Point point = new Point(pixW, pixH);

        Camera.Parameters parameters = null;
        try {
            parameters = camera.getParameters();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //不支持设置自定义聚焦，则使用自动聚焦，返回

        if(Build.VERSION.SDK_INT >= 14) {

            if (parameters.getMaxNumFocusAreas() <= 0) {
                //focus
                try {
                    camera.autoFocus(callback);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            Log.i(TAG, "onCameraFocus:" + point.x + "," + point.y);

            //定点对焦
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            int left = point.x - 300;
            int top = point.y - 300;
            int right = point.x + 300;
            int bottom = point.y + 300;
            left = left < -1000 ? -1000 : left;
            top = top < -1000 ? -1000 : top;
            right = right > 1000 ? 1000 : right;
            bottom = bottom > 1000 ? 1000 : bottom;
            areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
            parameters.setFocusAreas(areas);
            try {
                //本人使用的小米手机在设置聚焦区域的时候经常会出异常，看日志发现是框架层的字符串转int的时候出错了，
                //目测是小米修改了框架层代码导致，在此try掉，对实际聚焦效果没影响
                camera.setParameters(parameters);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
                return false;
            }
        }

        //focus
        try {
            camera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }
}
