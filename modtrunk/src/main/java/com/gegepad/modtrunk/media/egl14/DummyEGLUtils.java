package com.gegepad.modtrunk.media.egl14;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.view.Surface;

public class DummyEGLUtils {
    private static final int EGL_RECORDABLE_ANDROID = 0x3142;

    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext eglCtx = EGL14.EGL_NO_CONTEXT;
    private EGLDisplay eglDis = EGL14.EGL_NO_DISPLAY;
    EGLConfig[] configs = new EGLConfig[1];


    public void initEGL() {
        eglDis = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        EGL14.eglInitialize(eglDis, version, 0, version, 1);
        int confAttr[] = {
                EGL14.EGL_SURFACE_TYPE,EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE
        };

        int[] numConfigs = new int[1];
        EGL14.eglChooseConfig(eglDis, confAttr, 0, configs, 0, 1, numConfigs, 0);

        int ctxAttr[] = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,// 0x3098
                EGL14.EGL_NONE
        };
        eglCtx = EGL14.eglCreateContext(eglDis, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0);

//        int[] surfaceAttr = {
//                EGL14.EGL_WIDTH, 480,
//                EGL14.EGL_HEIGHT, 640,
//                EGL14.EGL_NONE
//        };
//        eglSurface = EGL14.eglCreatePbufferSurface(eglDis, configs[0],  surfaceAttr, 0);
//
//        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);
    }

    public EGLSurface createWindowSurface(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new RuntimeException("invalid surface: " + surface);
        }

        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(eglDis, configs[0], surface,
                surfaceAttribs, 0);
        if (eglSurface == null) {
            throw new RuntimeException("surface was null");
        }

        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);

        return eglSurface;
    }

    public EGLSurface createOffscreenSurface(int width, int height) {
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(eglDis, configs[0],
                surfaceAttribs, 0);
        if (eglSurface == null) {
            throw new RuntimeException("surface was null");
        }

        EGL14.eglMakeCurrent(eglDis, eglSurface, eglSurface, eglCtx);

        return eglSurface;
    }

    public EGLContext getContext() {
        return eglCtx;
    }

    public void swap() {
        EGL14.eglSwapBuffers(eglDis, eglSurface);
    }

    public void release() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(eglDis, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(eglDis, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
        }
        if (eglCtx != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(eglDis, eglCtx);
            eglCtx = EGL14.EGL_NO_CONTEXT;
        }
        if (eglDis != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(eglDis);
            eglDis = EGL14.EGL_NO_DISPLAY;
        }
    }

}

