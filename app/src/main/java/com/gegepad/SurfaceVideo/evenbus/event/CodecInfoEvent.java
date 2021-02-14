package com.gegepad.SurfaceVideo.evenbus.event;

/**
 * Created 20180427.
 */

public class CodecInfoEvent {
    private int mFrameRate;
    private int mBitRate;

    public CodecInfoEvent(int frameRate, int bitRate) {
        this.mFrameRate = frameRate;
        this.mBitRate = bitRate;
    }

    public int getFramerate() {
        return mFrameRate;
    }

    public int getBitrate() {
        return mBitRate;
    }
}
