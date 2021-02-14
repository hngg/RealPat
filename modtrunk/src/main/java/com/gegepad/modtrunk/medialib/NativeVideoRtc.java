package com.gegepad.modtrunk.medialib;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

@SuppressLint("NewApi")
public class NativeVideoRtc
{
    private static String TAG = NativeVideoRtc.class.getName();

    public NativeVideoRtc()
    {
    }

    static
    {
        try
        {
            System.loadLibrary("VideoRtc");
            Log.e(TAG, "loadLibrary VideoRtc suss");
        }
        catch(Throwable e)
        {
            Log.e(TAG, "loadLibrary==:"+e.toString());
        }
    }

     void onCommRtc(byte[] buffer, int size) {
        Log.e(TAG, "onCommRtc size:" +size);
    }

    public native int startRecvRender(String localAddr, int localPort, String remoteAddr, int remotePort, Surface surface, int codecType);
    public native int stopRecvRender();

    public native int startSendVideo(String localAddr, int localPort, String remoteAddr, int remotePort, int codecType);
    public native int stopSendVideo();
    public native int sendSampleData(ByteBuffer byteBuf, int offset, int size, long presentationTimeUs,  int flags);

    public native int startCommRtc(String localAddr, int localPort, String remoteAddr, int remotePort);
    public native int stopCommRtc();
    public native int sendCommData(ByteBuffer byteBuf, int size);
}

