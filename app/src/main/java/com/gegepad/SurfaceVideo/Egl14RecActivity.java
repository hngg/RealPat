package com.gegepad.SurfaceVideo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.gegepad.aidl.IEgl14Service;
import com.gegepad.modtrunk.media.egl14.CameraService;

public class Egl14RecActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    private static final String TAG = "Egl14RecActivity";

    private boolean mShowface = false, mRecording = false;
    int mWidth,mHeight;

    Button recButton, showButton;
    private SurfaceView textureView;
    private Surface mSurface;

    private IEgl14Service service;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            service = IEgl14Service.Stub.asInterface(arg1);

            Log.d(TAG, "onServiceConnected service:"+service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "onServiceDisconnected");
            service = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl14rec);

        if (service == null) {
            Intent serviceIntent = new Intent(this, CameraService.class);
            startService(serviceIntent);
            Log.d(TAG, "bindService");
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        }

        recButton = (Button)findViewById(R.id.recode);
        showButton = (Button)findViewById(R.id.show);
        textureView = (SurfaceView)findViewById(R.id.surface_view);
        recButton.setOnClickListener(this);
        showButton.setOnClickListener(this);
        textureView.getHolder().addCallback(this);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        unbindService(connection);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurface = surfaceHolder.getSurface();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.show:
                if (mShowface == false) {
                    try {
                        if(service!=null) {
                            service.surfaceChanged(1, mSurface, mWidth, mHeight);
                        }
                        Log.d(TAG, "onServiceConnected surfaceChanged service:"+service);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    showButton.setText("停止渲染");
                    mShowface = true;
                } else {
                    //stopRecode();
                    try {
                        if(service!=null) {
                            service.surfaceDestroy(1);
                        }
                        Log.d(TAG, "onServiceConnected surfaceChanged service:"+service);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    showButton.setText("开始渲染");
                    mShowface = false;
                }
                break;
        }
    }
}
