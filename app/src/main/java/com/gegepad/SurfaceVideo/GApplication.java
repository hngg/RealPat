package com.gegepad.SurfaceVideo;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.gegepad.service.RealTimeService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import com.lnlyj.multistream.stream.ShuntClient;
//import com.serenegiant.usb.USBMonitor;


public class GApplication extends Application {

	String TAG = "GApplication";
	static String ROOTPATH = "ModuleTest";

    public static int SCREEN_WIDTH 	= -1;
    public static int SCREEN_HEIGHT = -1;
    public static float DIMEN_RATE 	= -1.0F;
    public static int DIMEN_DPI 	= -1;
	public static GApplication mContext = null;

    @Override
    public void onCreate() {
        // 程序创建的时候执行
        Log.d(TAG, "onCreate");
        mContext = this;
        getScreenSize();

        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) 
        	isExist(getRootPath());

//        Intent serviceIntent = new Intent(this, RealTimeService.class);
//        startService(serviceIntent);
        DeviceServiceManager.instance();

        super.onCreate();
    }

    /**
     * 初始化屏幕宽高
     */
    public void getScreenSize()
    {
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(dm);
        DIMEN_RATE 		= dm.density / 1.0F;
        DIMEN_DPI 		= dm.densityDpi;
        SCREEN_WIDTH 	= dm.widthPixels;
        SCREEN_HEIGHT 	= dm.heightPixels;
        if (SCREEN_WIDTH > SCREEN_HEIGHT) {
            int t = SCREEN_HEIGHT;
            SCREEN_HEIGHT = SCREEN_WIDTH;
            SCREEN_WIDTH = t;
        }
        Log.e(TAG, "dimen_rate:"+DIMEN_RATE+" dimen_dpi:"+DIMEN_DPI
                +" screen_w:"+SCREEN_WIDTH + "screen_h:"+SCREEN_HEIGHT);
    }

    public static void Assets2Sd(Context context, String fileAssetPath, String fileSdPath){
        //测试把文件直接复制到sd卡中 fileSdPath完整路径
        File file = new File(fileSdPath);
        if(file.exists()){
            file.delete();
        }

        try {
            copyBigDataToSD(context, fileAssetPath, fileSdPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyBigDataToSD(Context context, String fileAssetPath, String strOutFileName) throws IOException
    {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(fileAssetPath);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    public static synchronized String getAppName() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    mContext.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return mContext.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRootPath() {
        return Environment.getExternalStorageDirectory().toString();//+"/"+"hyt";//getAppName();
    }

    /**
     *
     * @param path 文件夹路径
     */
    public static void isExist(String path) {
        File file = new File(path);
        //判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        // TODO Auto-generated method stub
//        Intent serviceIntent = new Intent(this, RealTimeService.class);
//        stopService(serviceIntent);
        Log.d(TAG, "onTerminate");
        super.onTerminate();
    }
    
    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        Log.d(TAG, "onLowMemory");
        super.onLowMemory();
    }
    
    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        Log.d(TAG, "onTrimMemory");
        super.onTrimMemory(level);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

}


