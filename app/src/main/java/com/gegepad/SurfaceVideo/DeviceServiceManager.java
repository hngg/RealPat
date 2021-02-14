package com.gegepad.SurfaceVideo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Surface;

import com.gegepad.aidl.IRealTimeService;
import com.gegepad.service.RealTimeService;


public class DeviceServiceManager {
    private static final String TAG = "DeviceServiceManager";

    static DeviceServiceManager instance = null;
    public static DeviceServiceManager instance() {
        if(instance==null)
            instance = new DeviceServiceManager();
        return instance;
    }

    RealTimeService.BinderWrapper wrapBinder;
    private IRealTimeService servStub;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if(binder==null) {
                Log.e(TAG, "onServiceConnected binder is null");
                return;
            }

            wrapBinder = (RealTimeService.BinderWrapper)binder;
            servStub = IRealTimeService.Stub.asInterface(wrapBinder.getBinder());
            RealTimeService selfServive = wrapBinder.getService();

            selfServive.setCallback(new RealTimeService.Listener() {
                @Override
                public void onComeData(byte[]buffer, int size) {
                    // TODO Auto-generated method stub
                    System.out.println("====size===="+size);
                }
            });
            Log.d(TAG, "onServiceConnected service:" + servStub);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            servStub = null;
        }
    };

    DeviceServiceManager()
    {
        Context curCont = GApplication.mContext;
        if (servStub == null) {
            Intent serviceIntent = new Intent(curCont, RealTimeService.class);
            startForegroundService(curCont, serviceIntent);
            Log.d(TAG, "bindService");
            curCont.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    public static void startForegroundService(Context context, Intent intent)
    {
//        if (Build.VERSION.SDK_INT >= 26)
//            context.startForegroundService(intent);
//        else
            context.startService(intent);
    }

    void surfaceChanged(Surface surface, int width, int height)
    {
        try {
            if(servStub!=null) {
                servStub.surfaceChanged(1, surface, width, height);
            }
            Log.d(TAG, "onServiceConnected surfaceChanged service:"+servStub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void surfaceDestroy()
    {
        try {
            if(servStub!=null) {
                servStub.surfaceDestroy(1);
            }
            Log.d(TAG, "onServiceConnected surfaceChanged service:"+servStub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startRecode(String filePath)
    {
        try {
            if(servStub!=null) {
                servStub.startRecorder(filePath);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void stopRecode()
    {
        try {
            if(servStub!=null) {
                servStub.stopRecorder();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int cameraUpdateFocus(int fWidth, int fHeight){
        try {
            if(servStub!=null) {
                return servStub.cameraUpdateFocus(fWidth, fHeight);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }
}


