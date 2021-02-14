package com.gegepad.SurfaceVideo.evenbus.event;

/**
 * Created 20180427.
 */

public class FileRecvEvent {
	private int mSockid, mLen, mCmd;

    public FileRecvEvent(int sock, int cmd, int len) {
        mCmd 	= cmd;
        mLen 	= len;
        mSockid = sock;
    }

    public int getSock() {
        return mSockid;
    }
    public int getLen() {
        return mLen;
    }
    public int getCmd() {
        return mCmd;
    }
}
