package com.gegepad.SurfaceVideo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gegepad.SurfaceVideo.tabfragment.menu.NormalTabActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Create By Lnlyj on 2020/6/9
 **/
public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();
    }

    private void requestPermissions() {

        List<String> permissions = new ArrayList<>();

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.CAMERA)) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.RECORD_AUDIO)) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissions.size() == 0) {
            startActivity(new Intent(this, NormalTabActivity.class));//RecodeActivity NormalTabActivity
            finish();
        } else {
            requestPermissions(permissions.toArray(new String[0]), 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (PackageManager.PERMISSION_GRANTED != grantResults[i]) {
                list.add(permission);
            }
        }
        if (list.isEmpty()) {
            startActivity(new Intent(this, NormalTabActivity.class));//Egl14RecActivity
            finish();
        } else {
            Toast.makeText(this, "请授予权限", Toast.LENGTH_SHORT).show();
        }
    }
}
