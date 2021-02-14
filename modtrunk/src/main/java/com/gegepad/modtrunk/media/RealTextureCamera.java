package com.gegepad.modtrunk.media;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.gegepad.modtrunk.database.DataSetting;
import com.gegepad.modtrunk.media.devices.CameraEglSurfaceView;
import com.gegepad.modtrunk.media.devices.CameraOneHelper;
import com.gegepad.modtrunk.media.encodec.BaseVideoEncoder;
import com.gegepad.modtrunk.media.encodec.ICodecFrame;
import com.gegepad.modtrunk.media.encodec.RecorderMuxer;
import com.gegepad.modtrunk.medialib.NativeVideoRtc;
import com.gegepad.modtrunk.network.NetUtils;

import java.nio.ByteBuffer;

public class RealTextureCamera implements  ICodecFrame {
    private static final String TAG = RealTextureCamera.class.getName();

    private CameraEglSurfaceView mCameraEglSurface;
    private BaseVideoEncoder videoEncodeStream;
    private RecorderMuxer mRecorderMuxer;

    private Context mContex;

    private int mCodecType = 1;//here change the codec type:h264(1) or h265(2)
    DataSetting mDataSetting;
    private NativeVideoRtc videoRtc = new NativeVideoRtc();

    public RealTextureCamera(Context cont)
    {
         mContex = cont;
         mDataSetting 	= new DataSetting(mContex);
    }

    public void startStream()
    {
        if(mContex!=null) {
            videoEncodeStream = new BaseVideoEncoder(mContex, mCameraEglSurface.getTextureId());
            videoEncodeStream.SetCodecCall(this);
            //Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "testRecode.mp4",
            videoEncodeStream.initEncoder(mCameraEglSurface.getEglContext(),
                    mCameraEglSurface.getCameraPrivewHeight(),//480,//相机的宽和高是相反的
                    mCameraEglSurface.getCameraPrivewWidth(),//640
                    2000000
            );

            String addr = mDataSetting.readData(DataSetting.DADDR);
            String port = mDataSetting.readData(DataSetting.DPORT);
            String codec= mDataSetting.readData(DataSetting.CODECTYPE);

            videoRtc.startSendVideo(NetUtils.getIPAddress(mContex), 38123, addr, Integer.parseInt(port), mCodecType);

            videoEncodeStream.startEncodec();
        }
    }

    public void stopStream()
    {
        if(videoEncodeStream!=null) {
            videoEncodeStream.stopEncodec();
            videoEncodeStream = null;
        }
        videoRtc.stopSendVideo();
    }

    public void startRecode(String filePath, int samplerate, int bit, int channels) {
        if(mContex!=null) {
                mRecorderMuxer = new RecorderMuxer(filePath);
                mRecorderMuxer.initVideo(mContex, mCameraEglSurface.getTextureId(), mCameraEglSurface.getEglContext(),
                        mCameraEglSurface.getCameraPrivewHeight(), mCameraEglSurface.getCameraPrivewWidth());
                mRecorderMuxer.startRecorder();
        }
    }

    public void stopRecode()
    {
        if(mRecorderMuxer!=null) {
            mRecorderMuxer.stopRecorder();
            mRecorderMuxer.release();
            mRecorderMuxer = null;
        }
    }

    public void localSurfaceChanged(Surface surface, int width, int height) {
        if(mCameraEglSurface==null)
            mCameraEglSurface = new CameraEglSurfaceView(mContex);
        mCameraEglSurface.localSurfaceChanged(surface, width, height);
    }

    public void localSurfaceDestroyed() {
        if(mCameraEglSurface!=null) {
            mCameraEglSurface.localSurfaceDestroyed();
            mCameraEglSurface = null;
        }
    }

    void remoteSurfaceChanged(Surface surface, int width, int height) {

    }

    void remoteSurfaceDestroyed() {

    }


    @Override
    public int onVideoCodec(ByteBuffer buffer, MediaCodec.BufferInfo buffInfo) {
        if(videoEncodeStream!=null)
            videoRtc.sendSampleData(buffer, buffInfo.offset, buffInfo.size, buffInfo.presentationTimeUs, buffInfo.flags);

        byte[] outData = new byte[buffInfo.size];
        buffer.get(outData);
        Log.e(TAG, "mysent " + buffInfo.size + " bytes  flag=" + buffInfo.flags + " ftype:"+(outData[4]&0x1F) );
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

    /////////////////////////////////////////////////////////camera operate////////////////////////////////////////////////////////////////
    public void cameraUpdateFocus(int fWidth, int fHeight)
    {
        if(mCameraEglSurface!=null) {
            CameraOneHelper camera = mCameraEglSurface.getCameraHelper();
            if(camera!=null)
                camera.updateFocus(fWidth, fHeight);
        }
    }

    public void cameraUpdateZoom()
    {
    }

    public void cameraOpenFlash(boolean flash)
    {

    }

    public void cameraSwitch()
    {

    }

}

