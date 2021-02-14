package com.gegepad.modtrunk.media.encodec;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class AudioEncoder  {
    private static final String TAG = AudioEncoder.class.getName();
    private MediaCodec mEncodec;
    private MediaCodec.BufferInfo mBufferInfo;
    private AudioRecord mAudioRecord;
    private boolean mStopped;

    private ICodecFrame mCodecFrame;
    private int mBufferSizeInBytes;

    // 输入源 麦克风
    private final static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    // 采样率 44.1kHz，所有设备都支持
    private final static int SAMPLE_RATE = 44100;
    // 通道 单声道，所有设备都支持
    private final static int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    // 精度 16 位，所有设备都支持
    private final static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // 通道数 单声道
    private static final int CHANNEL_COUNT = 1;
    // 比特率
    private static final int BIT_RATE = 96000;

    public void createAudio() {
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(AudioEncoder.SAMPLE_RATE, AudioEncoder.CHANNEL_CONFIG, AudioEncoder.AUDIO_FORMAT);
        if (mBufferSizeInBytes <= 0) {
            throw new RuntimeException("AudioRecord is not available, minBufferSize: " + mBufferSizeInBytes);
        }
        Log.i(TAG, "createAudioRecord minBufferSize: " + mBufferSizeInBytes);

        mAudioRecord = new AudioRecord(AudioEncoder.AUDIO_SOURCE, AudioEncoder.SAMPLE_RATE, AudioEncoder.CHANNEL_CONFIG, AudioEncoder.AUDIO_FORMAT, mBufferSizeInBytes);
        int state = mAudioRecord.getState();
        Log.i(TAG, "createAudio state: " + state + ", initialized: " + (state == AudioRecord.STATE_INITIALIZED));
    }

    public void createMediaCodec() throws IOException {
//        MediaCodecInfo mediaCodecInfo = CodecUtils.selectCodec(MediaFormat.MIMETYPE_AUDIO_AAC);
//        if (mediaCodecInfo == null) {
//            throw new RuntimeException(MediaFormat.MIMETYPE_AUDIO_AAC + " encoder is not available");
//        }
//        Log.i(TAG, "createMediaCodec: mediaCodecInfo " + mediaCodecInfo.getName());

        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, SAMPLE_RATE, CHANNEL_COUNT);
        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);

        mEncodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        mEncodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void start() throws IOException {
        Log.d(TAG, "start() called with: outFile = ["  + "]");
        mStopped = false;
        mEncodec.start();
        mAudioRecord.startRecording();
        byte[] buffer = new byte[mBufferSizeInBytes];
        ByteBuffer[] inputBuffers = mEncodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mEncodec.getOutputBuffers();
        try {
            while (!mStopped) {
                int readSize = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                if (readSize > 0) {
                    int inputBufferIndex = mEncodec.dequeueInputBuffer(-1);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                        inputBuffer.clear();
                        inputBuffer.put(buffer);
                        inputBuffer.limit(buffer.length);
                        mEncodec.queueInputBuffer(inputBufferIndex, 0, readSize, 0, 0);
                    }

                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = mEncodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        byte[] chunkAudio = new byte[bufferInfo.size + 7];// 7 is ADTS size

                        outputBuffer.get(chunkAudio, 7, bufferInfo.size);
                        outputBuffer.position(bufferInfo.offset);

                        mEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mEncodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                } else {
                    Log.w(TAG, "read audio buffer error:" + readSize);
                    break;
                }
            }
        } finally {
            Log.i(TAG, "released");
            mAudioRecord.stop();
            mAudioRecord.release();
            mEncodec.stop();
            mEncodec.release();
        }
    }

    public void SetCodecCall(ICodecFrame codec)
    {
        mCodecFrame = codec;
    }

    public void startEncodec() {
        mEncodec.start();
    }

    public void stopEncodec() {
        mEncodec.stop();
    }

    public void release()
    {
        mEncodec.release();
        mEncodec = null;
    }

}
