package com.gegepad.SurfaceVideo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.gegepad.modtrunk.database.DataSetting;
import com.gegepad.modtrunk.medialib.NativeVideoRtc;
import com.gegepad.modtrunk.network.NetUtils;

public class VideoRecvActivity extends Activity implements SurfaceHolder.Callback {

    private String TAG = VideoRecvActivity.class.getSimpleName();
    private final int width 	= 1280;//1920;//
    private final int height 	= 720;//1080;//

    private SurfaceHolder mHolder = null;
    private NativeVideoRtc videoRtc = new NativeVideoRtc();

    private DataSetting mDataSetting 	= new DataSetting(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_recv);

        SurfaceView sfv_video = (SurfaceView) findViewById(R.id.sfv_video);
        mHolder = sfv_video.getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        String codec= mDataSetting.readData(DataSetting.CODECTYPE);
        String addr = mDataSetting.readData(DataSetting.DADDR);
        String port = mDataSetting.readData(DataSetting.DPORT);
        int type = Integer.parseInt(codec);
        if(type!=2)//if not h265, now is h264
            type = 1;

        Log.e(TAG, "codecType recv:"+type);
        videoRtc.startRecvRender(NetUtils.getIPAddress(this), 38123, addr, Integer.parseInt(port), holder.getSurface(), type);
        //videoRtc.startCommRtc(NetUtils.getIPAddress(this), 12123, addr, 12123);
    }

    void onCommRtc(byte[] buffer, int size) {
        Log.e(TAG, "onCommRtc size:" +size);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        videoRtc.stopRecvRender();
        //videoRtc.stopCommRtc();
    }

}
