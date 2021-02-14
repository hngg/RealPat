package com.gegepad.modtrunk.media.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public class RecorderMuxer implements ICodecFrame{
    private static final String TAG = "RecorderMuxer";

    private MediaMuxer mMediaMuxer;
    private BaseVideoEncoder mVideoEncoder;
    private EGLContext mEGLContext;

    private int videoTrackIndex = -1;
    private int audioTrackIndex = -1;

    private boolean mMuxerStarted;

    public RecorderMuxer(String filePath) {
        try {
            mMediaMuxer = new MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initVideo(Context context, int textureId, EGLContext eglCont, int width, int height)
    {
        mEGLContext = eglCont;
        mVideoEncoder = new BaseVideoEncoder(context, textureId);
        mVideoEncoder.SetCodecCall(this);
        mVideoEncoder.initEncoder(mEGLContext,
                width,//相机的宽和高是相反的
                height,
                width*height*4
        );
        mMuxerStarted = false;
    }

    public void initAudio(int channel, int sample, int bit)
    {

    }

    public void startRecorder()
    {
        //important,start after MediaMuxer.addTrack
        //mMediaMuxer.start();
        mVideoEncoder.startEncodec();
    }

    public void stopRecorder()
    {
        if(mMuxerStarted) {
            mMediaMuxer.stop();
            mMuxerStarted = false;
        }

        mVideoEncoder.stopEncodec();
    }

    public void release()
    {
        mMediaMuxer.release();
        mMediaMuxer = null;
    }

    @Override
    public int onVideoCodec(ByteBuffer buffer, MediaCodec.BufferInfo buffInfo) {
        if(mMuxerStarted && mMediaMuxer!=null && videoTrackIndex!=-1) {
            mMediaMuxer.writeSampleData(videoTrackIndex, buffer, buffInfo);
            Log.d(TAG, "writeSampleData size:"+buffInfo.size);
        }else
            Log.e(TAG, "onVideoCodec size:"+buffInfo.size);//eglSwapBuffersWithDamageKHR:1370 error 300d (EGL_BAD_SURFACE)
        return 0;
    }

    @Override
    public int onVideoChange(MediaFormat format) {
        if(mMediaMuxer != null) {
            videoTrackIndex = mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
            mMuxerStarted = true;
        }
        return 0;
    }

    @Override
    public int onAudioCodec() {
        return 0;
    }


}
