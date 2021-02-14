package com.gegepad.modtrunk.media.encodec;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

public interface ICodecFrame {
    int onVideoCodec(ByteBuffer buffer, MediaCodec.BufferInfo buffInfo);
    int onVideoChange(MediaFormat format);
    int onAudioCodec();
}
