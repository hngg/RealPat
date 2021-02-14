package com.gegepad.modtrunk.media.encodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.gegepad.modtrunk.media.render.EglHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

public class BaseVideoEncoder implements Runnable, ITextureFrame {
    private static final String TAG = BaseVideoEncoder.class.getName();

    private Surface mSurface;
    private EGLContext mEGLContext;
    protected VideoEncodeRender mRender; //or Render class

    // TODO: these ought to be configurable as well
    private static String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 1;           // 5 seconds between I-frames
    private int width, height;
    private MediaCodec mVideoEncodec;
    private MediaCodec.BufferInfo mBufferInfo;
    private ICodecFrame mCodecFrame;//interface
    private EglHelper mEglHelper;
    private static final boolean VERBOSE = true;

    // ----- accessed by multiple threads -----
    private volatile EncoderHandler mHandler;
    private Object mReadyFence = new Object();      // guards ready/running
    private boolean mReady;
    private boolean mRunning;

    private static final int MSG_START_RECORDING = 0;
    private static final int MSG_STOP_RECORDING = 1;
    private static final int MSG_FRAME_AVAILABLE = 2;
    private static final int MSG_SET_TEXTURE_ID = 3;
    private static final int MSG_UPDATE_SHARED_CONTEXT = 4;
    private static final int MSG_QUIT = 5;



    public BaseVideoEncoder(Context context,int textureId) {
        mEglHelper = new EglHelper();

        mRender = new VideoEncodeRender(context, textureId, this);
    }

    public void SetCodecCall(ICodecFrame codec)
    {
        mCodecFrame = codec;
    }

    public void startEncodec() {
        if(mVideoEncodec!=null)
            mVideoEncodec.start();

        startRecording();
    }

    public void stopEncodec() {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_STOP_RECORDING));
        mHandler.sendMessage(mHandler.obtainMessage(MSG_QUIT));
    }

    public void initEncoder(EGLContext eglContext, int width, int height, int bitRate) {
        this.width = width;
        this.height = height;
        this.mEGLContext = eglContext;

        // h264
        initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height, bitRate);

        if (onStatusChangeListener != null) {
            onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.INIT);
        }

        Log.w(TAG, "initEncoder w:" + width + " h:" + height );
    }

    private void initVideoEncoder(String mineType, int width, int height, int bitRate) {
        try {
            MediaFormat videoFormat = MediaFormat.createVideoFormat(mineType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            if (VERBOSE) Log.d(TAG, "format: " + videoFormat);

            //设置压缩等级  默认是baseline
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
//                }
//            }

            mVideoEncodec = MediaCodec.createEncoderByType(mineType);
            mVideoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mBufferInfo = new MediaCodec.BufferInfo();
            mSurface = mVideoEncodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mBufferInfo = null;
            mSurface = null;
        }
    }

//    protected  void frameAvailable()
//    {
//        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE, null));
//    }

    public void handleStartCodec()
    {
        mEglHelper.initEgl(mSurface, mEGLContext);
        mRender.onSurfaceCreated();
        mRender.onSurfaceChanged(width, height);

        //first time need handle this function
        handleFrameDrawCodec();
        Log.d(TAG, "myegltest initEgl w:"+width + " h:"+height);
    }

    public void handleStopCodec()
    {
        getEncoder(true);//true for signal EndOfInputStream

        if(mVideoEncodec!=null) {
            mVideoEncodec.stop();
            mVideoEncodec.release();
            mVideoEncodec = null;
        }

        if (mEglHelper != null) {
            mEglHelper.destoryEgl();
            mEglHelper = null;
        }
    }

    public void handleFrameDrawCodec()
    {
        mRender.onDrawFrame();
        mEglHelper.swapBuffers();

        getEncoder(false);
        Log.d(TAG, "myegltest msgDraw .");
    }

    @Override
    public void onframeAvailable()
    {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_FRAME_AVAILABLE, null));
    }

    protected void startRecording() {
        synchronized (mReadyFence) {
            if (mRunning) {
                return;
            }

            mRunning = true;
            new Thread(this, "BaseVideoEncoder").start();

            while (!mReady) {
                try {
                    mReadyFence.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }

        Log.d(TAG, "Encoder: send MSG_START_RECORDING");
        mHandler.sendMessage(mHandler.obtainMessage(MSG_START_RECORDING, null));
    }

    @Override
    public void run() {
        // Establish a Looper for this thread, and define a Handler for it.
        Log.d(TAG, "Encoder thread prepare");
        Looper.prepare();
        synchronized (mReadyFence) {
            mHandler = new EncoderHandler(this);
            mReady = true;
            mReadyFence.notify();
        }
        Looper.loop();


        synchronized (mReadyFence) {
            mReady = mRunning = false;
            mHandler = null;
        }

        Log.d(TAG, "Encoder thread exiting");
    }

    private static class EncoderHandler extends Handler {
        private WeakReference<BaseVideoEncoder> mWeakEncoder;

        public EncoderHandler(BaseVideoEncoder encoder) {
            mWeakEncoder = new WeakReference<BaseVideoEncoder>(encoder);
        }
        @Override  // runs on encoder thread
        public void handleMessage(Message inputMessage)
        {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            BaseVideoEncoder encoder = mWeakEncoder.get();
            if (encoder == null) {
                Log.w(TAG, "EncoderHandler.handleMessage: encoder is null");
                return;
            }

            switch (what) {
                case MSG_START_RECORDING:
                    encoder.handleStartCodec();
                    break;
                case MSG_STOP_RECORDING:
                    encoder.handleStopCodec();
                    break;
                case MSG_FRAME_AVAILABLE:
                    encoder.handleFrameDrawCodec();
                    break;
                case MSG_SET_TEXTURE_ID:
                    //encoder.handleSetTexture(inputMessage.arg1);
                    break;
                case MSG_UPDATE_SHARED_CONTEXT:
                    //encoder.handleUpdateSharedContext((android.opengl.EGLContext) inputMessage.obj);
                    break;
                case MSG_QUIT:
                    Looper.myLooper().quit();
                    break;
                default:
                    throw new RuntimeException("Unhandled msg what=" + what);
            }
        }
    }

    public void getEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "getEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mVideoEncodec.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mVideoEncodec.getOutputBuffers();
        while (true) {
            int encoderStatus = mVideoEncodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mVideoEncodec.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                MediaFormat newFormat = mVideoEncodec.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);
                if(mCodecFrame!=null)
                    mCodecFrame.onVideoChange(newFormat);
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");

                    //may cause the encoder would not output sps and pps frame
                    //mBufferInfo.size = 0;//important for muxer recorder timestamp
                }

                if (mBufferInfo.size != 0) {
                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    if(mCodecFrame!=null)
                        mCodecFrame.onVideoCodec(encodedData, mBufferInfo);
//                    byte[] outData = new byte[mBufferInfo.size];
//                    encodedData.get(outData);
//                    try {
//                        outputStream.write(outData, 0, mBufferInfo.size);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }

                    Log.e(TAG, "mysent " + mBufferInfo.size + " bytes  ts=" + mBufferInfo.presentationTimeUs + " type:"+mBufferInfo.flags + " width:"+width);
                }

                mVideoEncodec.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }//


    private OnMediaInfoListener onMediaInfoListener;


    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }

    private OnStatusChangeListener onStatusChangeListener;

    public void setOnStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        this.onStatusChangeListener = onStatusChangeListener;
    }

    public interface OnStatusChangeListener {
        void onStatusChange(STATUS status);
        enum STATUS {
            INIT,
            START,
            END
        }
    }

}
