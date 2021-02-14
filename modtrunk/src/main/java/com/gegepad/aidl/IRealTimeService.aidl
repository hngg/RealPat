// IRealTimeService.aidl
package com.gegepad.aidl;

// Declare any non-default types here with import statements

interface IRealTimeService {
    void setSurface(in Surface surface, int type);

    void surfaceDestroy(int type);

    void surfaceChanged(int type, in Surface surface, int width, int height);

    void startRecorder(String filepath);

    void stopRecorder();

    int cameraUpdateFocus(int fWidth, int fHeight);
}
