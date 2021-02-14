package com.gegepad.SurfaceVideo.evenbus.event;

/**
 * Created on 20180502
 */

/**
 * 自定义类
 */
public class DataEvent {

    private String mData;
    private int mCmd;

    public String getData() {
        return mData;
    }
    
    public int getCmd(){
    	return mCmd;
    }

    public DataEvent(int cmd, String data) {
    	mCmd 	= cmd;
        mData 	= data;
    }
}
