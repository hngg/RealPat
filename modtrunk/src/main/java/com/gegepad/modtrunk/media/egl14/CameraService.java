package com.gegepad.modtrunk.media.egl14;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;

import com.gegepad.aidl.IEgl14Service;

public class CameraService extends Service {
    private static final String TAG = "CameraService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final byte[] mNv21Data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private CameraOverlap cameraOverlap;

    private DummyEGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;



    //binder define
    public final IEgl14Service.Stub binder = new IEgl14Service.Stub() {

        @Override
        public void surfaceChanged(int type, Surface surface, int width, int height) throws RemoteException {
            cameraOverlap = new CameraOverlap(CameraService.this);
            mEglUtils = new DummyEGLUtils();
            mEglUtils.createWindowSurface(surface);
            mFramebuffer = new GLFramebuffer();
            cameraOverlap.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    synchronized (mNv21Data) {
                        System.arraycopy(data, 0, mNv21Data, 0, data.length);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("================","mNv21Data就是camera的图像数据");
                        }
                    });
                }
            });
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mEglUtils.initEGL();
                            mFramebuffer.initFramebuffer();
                            cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
                        }
                    });

                }
            });
        }

        @Override
        public void surfaceDestroy(int type) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    cameraOverlap.release();
                    mFramebuffer.release();
                    mEglUtils.release();

                }
            });
        }

        @Override
        public void startRecorder(String filepath) throws RemoteException {

        }

        @Override
        public void stopRecorder() throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("DrawFaceThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mHandlerThread.quit();
    }
}