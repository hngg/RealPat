package com.gegepad.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

import com.gegepad.aidl.IRealTimeService;
import com.gegepad.modtrunk.media.RealTextureCamera;


public class RealTimeService extends Service {
    private static final String TAG = RealTimeService.class.getName();

    private Listener callback = null;
    private RealTextureCamera mRealCamera;
    private BinderWrapper wrapBinder = new BinderWrapper();

    public class BinderWrapper extends Binder {
        //return the big object
        public RealTimeService getService() {
            return RealTimeService.this;
        }

        public Binder getBinder()
        {
            return binder;
        }
    }

    //binder define
    public final IRealTimeService.Stub binder = new IRealTimeService.Stub() {

        @Override
        public void setSurface(Surface surface, int type) throws RemoteException {

        }

        @Override
        public void surfaceChanged(int type, Surface surface, int width, int height) throws RemoteException {
            if(mRealCamera!=null) {
                mRealCamera.localSurfaceChanged(surface, width, height);
                mRealCamera.startStream();
            }
            callback.onComeData(null, 100);
            Log.d(TAG, "RealTimeService width:" + width + " height:" + height + " camera:"+mRealCamera);
        }

        @Override
        public void surfaceDestroy(int type) throws RemoteException {
            if(mRealCamera!=null) {
                mRealCamera.stopStream();
                mRealCamera.localSurfaceDestroyed();
                Log.d(TAG, "RealTimeService surfaceDestroy");
            }
        }

        @Override
        public void startRecorder(String filepath) throws RemoteException {
            if(mRealCamera!=null) {
                mRealCamera.startRecode(filepath, 0, 0, 0);
            }
        }

        @Override
        public void stopRecorder() throws RemoteException {
            if(mRealCamera!=null) {
                mRealCamera.stopRecode();
            }
        }

        @Override
        public int cameraUpdateFocus(int fWidth, int fHeight) throws RemoteException {
            if(mRealCamera!=null) {
                mRealCamera.cameraUpdateFocus(fWidth, fHeight);
            }
            return 0;
        }
    };

    //execute at first start time
    @Override
    public void onCreate() {
        super.onCreate();
        mRealCamera = new RealTextureCamera(this);
        Log.d(TAG, "RealTimeService onCreate done.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "服务已经启动", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "服务已经停止", Toast.LENGTH_LONG).show();
    }

    //execute at first start time
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "RealTimeService Default TrunkingService returned");
        return wrapBinder;
    }

    public void setCallback(Listener callback) {
        this.callback = callback;
    }

    public interface Listener {
        //realtime databuffer
        void onComeData(byte[]buffer, int size);
    }

}