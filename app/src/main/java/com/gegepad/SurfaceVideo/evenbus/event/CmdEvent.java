package com.gegepad.SurfaceVideo.evenbus.event;

/**
 * Created 20180427.
 */

public class CmdEvent {
    private int mCmd;

    public CmdEvent(int cmd) {
        this.mCmd = cmd;
    }

    public int getCmd() {
        return mCmd;
    }
}
