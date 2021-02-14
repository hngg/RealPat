// IEgl14Service.aidl
package com.gegepad.aidl;

// Declare any non-default types here with import statements

interface IEgl14Service {
    void surfaceDestroy(int type);

    void surfaceChanged(int type, in Surface surface, int width, int height);

    void startRecorder(String filepath);

    void stopRecorder();
}
