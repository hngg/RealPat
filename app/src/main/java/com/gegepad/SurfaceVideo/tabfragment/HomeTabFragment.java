package com.gegepad.SurfaceVideo.tabfragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.gegepad.SurfaceVideo.GApplication;
import com.gegepad.SurfaceVideo.QRCodePopWin;
import com.gegepad.SurfaceVideo.R;
import com.gegepad.SurfaceVideo.RecodeActivity;
import com.gegepad.SurfaceVideo.ScalingScannerActivity;
import com.gegepad.SurfaceVideo.VideoRecvActivity;
import com.gegepad.SurfaceVideo.evenbus.event.CmdEvent;
import com.gegepad.SurfaceVideo.tabfragment.menu.NormalTabActivity;
import com.gegepad.modtrunk.network.hotspot.APManager;
import com.gegepad.modtrunk.network.hotspot.DefaultFailureListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


//import com.wy.mptt.UsbCameraActivity;

/**
 * 首页
 */

public class HomeTabFragment extends Fragment implements View.OnClickListener, APManager.OnSuccessListener
{
    private static final String TAG = "HomeTabFragment";

    static public Context context;

    private View view;
    private Context mContext;
    private Button encode_native = null, start_client = null, start_comm = null, stop_comm = null;

    //eventbus
    private boolean mbEventBusRegist = false;

    //hotspot
    private QRCodePopWin mQrcodePopWin 	= null;
    private APManager apManager;
    private String mQRcodeStr;


    public interface MyOnTouchListener {
        public boolean onTouch(MotionEvent ev);
    }
    private ArrayList<MyOnTouchListener> onTouchListeners = new ArrayList<MyOnTouchListener>(
            10);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mContext = getActivity();

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
                view = View.inflate(mContext, R.layout.tab_fragment_home, null);
        }

        apManager = APManager.getApManager(mContext);

        encode_native = (Button)view.findViewById(R.id.encode_native);
        encode_native.setOnClickListener(this);
        start_client = (Button)view.findViewById(R.id.start_client);
        start_client.setOnClickListener(this);
        start_comm = (Button)view.findViewById(R.id.start_comm);
        start_comm.setOnClickListener(this);
        stop_comm = (Button)view.findViewById(R.id.stop_comm);
        stop_comm.setOnClickListener(this);


        if(mbEventBusRegist==false) {
            EventBus.getDefault().register(this);
            mbEventBusRegist = true;
        }

        NormalTabActivity.MyTouchListener myTouchListener = new NormalTabActivity.MyTouchListener() {
            @Override
            public void onTouchEvent(MotionEvent event) {
                // 处理手势事件
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 手指压下屏幕
                    case MotionEvent.ACTION_DOWN:
                        Log.e(TAG, "onTouch......");
                        if(mQrcodePopWin!=null) {
                            mQrcodePopWin.dismiss();
                            mQrcodePopWin = null;
                        }
//                        else
//                        {
//                            if(apManager.isWifiApEnabled())
//                                showPopWin(mQRcodeStr);
//                        }
                        break;
                }
            }
        };
        // 将myTouchListener注册到分发列表
        ((NormalTabActivity)this.getActivity()).registerMyTouchListener(myTouchListener);

        return view;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mbEventBusRegist) {
            EventBus.getDefault().unregister(this);
            mbEventBusRegist = false;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(CmdEvent event) {
        if (event != null) {
            //startActivity(new Intent().setClass(mContext, ServerRealVideoSendActivity.class));
            System.out.println("onEventMainThread:"+event.getCmd()+" "+Thread.currentThread().getName());
        } else {
            System.out.println("event:"+event);
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            }
        }
    };

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch(v.getId())
        {
            case R.id.encode_native:
                intent.setClass(mContext, ScalingScannerActivity.class);//UsbCameraActivity EncodeNativeMotionActivity EncodeNativeActivity CameraCaptureActivity
                startActivity(intent);
                break;

            case R.id.start_client:
//                intent.setClass(mContext, VideoRtcActivity.class);//ClientRealVideoRecvActivity
//                startActivity(intent);
                if(apManager.isWifiApEnabled())
                {
                    apManager.disableWifiAp();
                    if(mQrcodePopWin!=null)
                    {
                        mQrcodePopWin.dismiss();
                        mQrcodePopWin = null;
                    }
                }
                else {
                    apManager.turnOnHotspot(mContext,
                            this,
                            new DefaultFailureListener(getActivity())
                    );
                }
                break;

            case R.id.start_comm:
                intent.setClass(mContext, RecodeActivity.class);//ClientRealVideoRecvActivity
                startActivity(intent);
                break;
            case R.id.stop_comm:
                intent.setClass(mContext, VideoRecvActivity.class);//ClientRealVideoRecvActivity
                startActivity(intent);
                break;
        }
    }

    private void showPopWin(String qrcode, String qrtxt)
    {
        if(mQrcodePopWin==null)
        {
            int pw = (int) (GApplication.SCREEN_WIDTH*0.8);
            int ph = (int) (GApplication.SCREEN_WIDTH*0.8);
            mQrcodePopWin = new QRCodePopWin(getActivity(), onClickListener, pw, ph);
            mQrcodePopWin.showAtLocation(getActivity().findViewById(R.id.main_view), Gravity.CENTER, 0, 0);

            mQrcodePopWin.createAndShowQR(getActivity(), qrcode, qrtxt,pw-50, ph-50);
        }
    }

    @Override
    public void onSuccess(@NonNull String ssid, @NonNull String password) {
        mQRcodeStr = "http://greatwit.com?"+"name="+ssid+"&"+"ps="+password;
        showPopWin(mQRcodeStr, ssid+"\n"+password);
    }

}
