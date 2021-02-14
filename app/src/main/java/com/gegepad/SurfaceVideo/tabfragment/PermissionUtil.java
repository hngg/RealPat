package com.gegepad.SurfaceVideo.tabfragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by y26342 on 2017/8/16.
 */

@TargetApi(23)
public class PermissionUtil {

    private static final String THIS_FILE = "PermissionUtils";
    /*
    permission string
     */
    public final static String PERMISSION_ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    public final static String PERMISSION_ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public final static String PERMISSION_CAMERA = "android.permission.CAMERA";
    public final static String PERMISSION_CALL_PHONE = "android.permission.CALL_PHONE";
    public final static String PERMISSION_GET_ACCOUNTS = "android.permission.GET_ACCOUNTS";
    public final static String PERMISSION_PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
    public final static String PERMISSION_RECORD_AUDIO = "android.permission.RECORD_AUDIO";
    public final static String PERMISSION_READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public final static String PERMISSION_USE_SIP = "android.permission.USE_SIP";
    public final static String PERMISSION_WRITE_CALL_LOG = "android.permission.WRITE_CALL_LOG";
    public final static String PERMISSION_WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public final static String PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    public final static int CODE_PERMISSION_ALL = 1000;

    public final static String[] permiArray = {PERMISSION_ACCESS_COARSE_LOCATION,
            PERMISSION_ACCESS_FINE_LOCATION, PERMISSION_CAMERA,
            PERMISSION_CALL_PHONE, PERMISSION_GET_ACCOUNTS,
            PERMISSION_PROCESS_OUTGOING_CALLS, PERMISSION_RECORD_AUDIO,
            PERMISSION_READ_PHONE_STATE, PERMISSION_USE_SIP,
            PERMISSION_WRITE_CALL_LOG, PERMISSION_WRITE_EXTERNAL_STORAGE};

    @TargetApi(23)
    public static boolean isAllPermisionGrant(Activity activity) {
        for (int i = 0; i < permiArray.length; i++) {
            if (activity.checkSelfPermission(permiArray[i]) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public final static List<String> permiStrList = Arrays.asList(permiArray);

    public static boolean isCompatible(int apiLevel) {
        return Build.VERSION.SDK_INT >= apiLevel;
    }

    @TargetApi(23)
    public static List<String> getUngrantPermissions(Activity activity, List<String> permissionList) {
        if (activity == null || permissionList.isEmpty()) {
            return null;
        }

        List<String> permissionsNeedToRequest = new ArrayList<>();
        if (isCompatible(Build.VERSION_CODES.M)) {
            for (int i = 0; i < permissionList.size(); i++) {
                if (activity.checkSelfPermission(
                        permissionList.get(i)) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeedToRequest.add(permissionList.get(i));
                }
            }
        }
        return permissionsNeedToRequest;
    }

    public static boolean isGrantPermissions(Activity activity, List<String> permissionList) {
        if (activity == null || permissionList.isEmpty()) {
            return false;
        }

        if (isCompatible(Build.VERSION_CODES.M)) {
            for (int i = 0; i < permissionList.size(); i++) {
                if (activity.checkSelfPermission(
                        permissionList.get(i)) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void requestAllUngrantPermissions(Activity activity, List<String> ungrantPermissionsList) {
        if (activity == null || ungrantPermissionsList.isEmpty()) {
            return;
        }
        if (isCompatible(Build.VERSION_CODES.M)) {
            activity.requestPermissions(ungrantPermissionsList.toArray(new String[0]), CODE_PERMISSION_ALL);
        }
    }

    @TargetApi(23)
    public static void requestPermission(final Activity activity, final String perssionStr) {
        if (TextUtils.isEmpty(perssionStr) || activity == null) {
            return;
        }
        if (isCompatible(Build.VERSION_CODES.M)) {
            if (activity.checkSelfPermission(perssionStr) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(
                        new String[]{perssionStr}, permiStrList.indexOf(perssionStr));
            }
        }
    }


    public static void doPermissionTaskBeforeBindService(final Activity activity, final int requestCode, Timer timer, final boolean getPermissionState,
                                                         final ServiceConnection connection, final Intent serviceIntent, final String[] permisionNeedToCheck) {
        if (timer == null) timer = new Timer();
        final TimerTask task = new TimerTask() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void run() {
                if(PermissionUtil.isGrantPermissions(activity, Arrays.asList(permisionNeedToCheck)))
                {
                    if(activity!= null&&serviceIntent!=null&&connection!=null){
                        activity.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
                        this.cancel();
                    }
                }
                else{
                    if(getPermissionState){
                        this.cancel();
                    }
                    else{
                        activity.requestPermissions(permisionNeedToCheck, requestCode);
                    }
                }
            }
        };
        timer.schedule(task, 0, 1000);

    }

    public static Intent setIntent(String name, Intent serviceIntent) {
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        serviceIntent.putExtras(bundle);
        return serviceIntent;
    }

    public static String getNameFromIntent(Intent intent) {
        Bundle data = intent.getExtras();
        return data.getString("name");
    }

    public interface GetSetPermissionResultState {
        abstract boolean getPermissionResultState();

        abstract void setPermissionResultState(boolean isGetPermissionResult);
    }



    public static boolean isCameraGranted() {
        try {
            Camera.open().release();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isCameraGranted(Context context) {
        if (context!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(PermissionUtil.PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED);
        }else {
            return isCameraGranted();
        }
    }
}
