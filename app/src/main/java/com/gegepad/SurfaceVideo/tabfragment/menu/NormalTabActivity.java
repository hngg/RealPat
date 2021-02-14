package com.gegepad.SurfaceVideo.tabfragment.menu;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;

import com.gegepad.SurfaceVideo.R;
import com.gegepad.SurfaceVideo.tabfragment.PermissionUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 第一种实现：用FragmentTabHost实现底部导航栏
 */
public class NormalTabActivity extends AppCompatActivity
{
    private boolean isAllPermiGrant = false;
    private Timer serviceBindTimer;
    public final static String []permisionNeedToCheck = {
            PermissionUtil.PERMISSION_CAMERA,PermissionUtil.PERMISSION_CALL_PHONE,
            PermissionUtil.PERMISSION_READ_PHONE_STATE,PermissionUtil.PERMISSION_USE_SIP,
            PermissionUtil.PERMISSION_PROCESS_OUTGOING_CALLS};

    //以下两个是给fragment的触屏事件
    public interface MyTouchListener {
        public void onTouchEvent(MotionEvent event);
    }
    // 保存MyTouchListener接口的列表
    private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<NormalTabActivity.MyTouchListener>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_activity_normal);

        //判断当前设备版本号是否为4.4以上，如果是，则通过调用setTranslucentStatus让状态栏变透明
        //透明状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        if (serviceBindTimer == null) {
            serviceBindTimer = new Timer();
        }
        initFragmentTabHost();//userful

        serviceBindTimer.schedule(new NormalTabActivity.BindServiceTask(), 0, 100);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //add by yaojin
        serviceBindTimer.cancel();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    /**
     * 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法
     * @param listener
     */
    public void registerMyTouchListener(MyTouchListener listener) {
        myTouchListeners.add(listener);
    }

    /**
     * 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法
     * @param listener
     */
    public void unRegisterMyTouchListener(MyTouchListener listener) {
        myTouchListeners.remove( listener );
    }

    /**
     * 分发触摸事件给所有注册了MyTouchListener的接口
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for (MyTouchListener listener : myTouchListeners) {
            listener.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    class BindServiceTask extends TimerTask {

        @TargetApi(23)
        @Override
        public void run() {
            // TODO Auto-generated method stub
            boolean isgrant = PermissionUtil.isGrantPermissions(NormalTabActivity.this, Arrays.asList(permisionNeedToCheck));
            if (PermissionUtil.isGrantPermissions(NormalTabActivity.this,
                    Arrays.asList(permisionNeedToCheck))) {
                //Intent serviceIntent = new Intent(HomeTabFragment.this, TrunkingService.class);
                //bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
                this.cancel();
            }
        }
    }

    /**
     * 初始化FragmentTabHost
     */
    private void initFragmentTabHost() {
        //初始化tabHost
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        //将tabHost和FragmentLayout关联
        tabHost.setup(getApplicationContext(), getSupportFragmentManager(), R.id.fl_content);

        //去掉分割线
        if (Build.VERSION.SDK_INT > 10) {
            tabHost.getTabWidget().setShowDividers(0);
        }
        //添加tab和其对应的fragment
        MainTabsNormal[] tabs = MainTabsNormal.values();
        for (int i = 0; i < tabs.length; i++) {
            MainTabsNormal mainTabs = tabs[i];
            TabHost.TabSpec tabSpec = tabHost.newTabSpec(mainTabs.getName());

            View indicator = View.inflate(getApplicationContext(), R.layout.tab_indicator, null);
            TextView tv_indicator = (TextView) indicator.findViewById(R.id.tv_indicator);
            Drawable drawable = getApplicationContext().getResources().getDrawable(mainTabs.getIcon());

            tv_indicator.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            tv_indicator.setText(mainTabs.getName());
            tabSpec.setIndicator(indicator);
            tabHost.addTab(tabSpec, mainTabs.getCla(), null);
        }
    }
}
