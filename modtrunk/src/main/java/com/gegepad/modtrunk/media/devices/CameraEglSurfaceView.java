package com.gegepad.modtrunk.media.devices;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.gegepad.modtrunk.media.render.EglFboRender;
import com.gegepad.modtrunk.media.render.EglHelper;

import javax.microedition.khronos.egl.EGLContext;


public class CameraEglSurfaceView /*extends EglSurfaceView*/ implements EglFboRender.OnSurfaceListener
{
    static String TAG = CameraEglSurfaceView.class.getName();

    public final static int RENDERMODE_WHEN_DIRTY   = 0; //"脏"模式,一般情况下使用脏模式,这样可以有效降低cpu负载;测试结果表明,OpenGL真正绘图时一般会占到30%以上的cpu
    public final static int RENDERMODE_CONTINUOUSLY = 1; //自动模式

    private int textureId;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private CameraOneHelper mCameraHelper;
    private EglFboRender mRenderer;
    private EglHelper mEglHelper;
    public Surface mSurface;

    public CameraEglSurfaceView(Context context) {
        //setRenderMode(RENDERMODE_WHEN_DIRTY);

        mCameraHelper = new CameraOneHelper(context);
        mRenderer = new EglFboRender(context);
        mRenderer.setOnSurfaceListener(this);  //output surfacecrate notify and texture frame data

        if(mEglHelper==null)
            mEglHelper = new EglHelper();

        //setRender(render);
        previewAngle(context);
    }

    public void localSurfaceChanged(Surface surface, int width, int height)
    {
        if(mEglHelper==null || mRenderer==null)
            return;

        mSurface = surface;

        mEglHelper.initEgl(mSurface, mEglHelper.getEglContext());
        mRenderer.onSurfaceCreated();
        mRenderer.onSurfaceChanged(width, height);
    }

    public void localSurfaceDestroyed() {
        if (mCameraHelper != null) {
            mCameraHelper.stopCameraAndRelease();
            mCameraHelper = null;
        }

        if (mEglHelper != null) {
            mEglHelper.destoryEgl();
            mEglHelper = null;
        }

        if(mRenderer!=null)
        {
            mRenderer.resetMatirx();
        }

        mSurface = null;
        //mEglContext = null;
    }

    public EGLContext getEglContext() {
        if (mEglHelper != null) {
            return mEglHelper.getEglContext();
        }
        return null;
    }

    public CameraOneHelper getCameraHelper()
    {
        return mCameraHelper;
    }

    public int getCameraPrivewWidth(){
        return mCameraHelper.getPreviewWidth();
    }

    public int getCameraPrivewHeight(){
        return mCameraHelper.getPreviewHeight();
    }

    @Override   //come from EglFboRender.java createCameraRenderTexture callback
    public void onSurfaceCreate(SurfaceTexture surfaceTexture,int textureId) {
        mCameraHelper.startCamera(surfaceTexture, cameraId, 1280, 720);
        this.textureId = textureId;
        Log.e(TAG, "onSurfaceCreate__");
    }

    @Override
    public void onFrameComing(SurfaceTexture surfaceTexture) {
            requestRender();
    }

    public void requestRender() {

        if (mEglHelper != null) {
            mRenderer.onDrawFrame();
            mEglHelper.swapBuffers();
        }
    }

    public void onDestroy() {
        if (mCameraHelper != null) {
            mCameraHelper.stopCameraAndRelease();
        }
        Log.e(TAG, "onDestroy__");
    }


    public void previewAngle(Context context) {
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        mRenderer.resetMatirx();
        switch (angle) {
            case Surface.ROTATION_0:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(90, 0, 0, 1);
                    mRenderer.setAngle(180, 1, 0, 0);
                } else {
                    mRenderer.setAngle(90f, 0f, 0f, 1f);
                }
                break;

            case Surface.ROTATION_90:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(180, 0, 0, 1);
                    mRenderer.setAngle(180, 0, 1, 0);
                } else {
                    mRenderer.setAngle(90f, 0f, 0f, 1f);
                }
                break;

            case Surface.ROTATION_180:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(90f, 0.0f, 0f, 1f);
                    mRenderer.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mRenderer.setAngle(-90, 0f, 0f, 1f);
                }
                break;

            case Surface.ROTATION_270:
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mRenderer.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mRenderer.setAngle(0f, 0f, 0f, 1f);
                }
                break;
        }
    }

    public int getTextureId() {
        return textureId;
    }

}
