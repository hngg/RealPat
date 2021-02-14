package com.gegepad.modtrunk.media.render;

public interface IRender {
    void onSurfaceCreated();

    void onSurfaceChanged(int width, int height);

    void onDrawFrame();
}
