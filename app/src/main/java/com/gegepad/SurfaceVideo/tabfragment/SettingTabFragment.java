package com.gegepad.SurfaceVideo.tabfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.gegepad.SurfaceVideo.R;
import com.gegepad.modtrunk.network.NetUtils;
import com.gegepad.modtrunk.database.DataSetting;


public class SettingTabFragment extends Fragment {
    private final String TAG = "SettingTabFragment";

    private Context context;
    private View view;
    private EditText serverAddr = null, serverPort = null, filePath = null, localIp = null;
    private DataSetting mDataSetting;
    private Spinner resoPinner, codecSpin;
    private ArrayAdapter<String> adapterReso, adapterCodec;
    private static final String[] resoArray={"CIF","VGA","720P","1080P","其他"}, codecArray={"H264","H265","其他"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        {
            //view.setText("你的血型是："+m[arg2]);
            mDataSetting.InsertOrUpdate(DataSetting.RESOINDEX, arg2+"");
        }
        public void onNothingSelected(AdapterView<?> arg0)
        { }
    }

    //使用数组形式操作
    class SpinnerSelectedListenerCodec implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
        {
            //view.setText("你的血型是："+m[arg2]);
            mDataSetting.InsertOrUpdate(DataSetting.CODECTYPE, (arg2+1)+"");
            Log.e(TAG, "codecType:"+(arg2+1));
        }
        public void onNothingSelected(AdapterView<?> arg0)
        { }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        if (view == null) {
                view = View.inflate(context, R.layout.tab_fragment_setting, null);
        }

        resoPinner = (Spinner)view.findViewById(R.id.resoSpin);
        adapterReso = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, resoArray);                 //设置下拉列表的风格
        adapterReso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                 //将adapter 添加到spinner中
        resoPinner.setAdapter(adapterReso);                 //添加事件Spinner事件监听
        resoPinner.setOnItemSelectedListener(new SpinnerSelectedListener());                 //设置默认值
        resoPinner.setVisibility(View.VISIBLE);

        codecSpin  = (Spinner)view.findViewById(R.id.codecSpin);
        adapterCodec = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, codecArray);                 //设置下拉列表的风格
        adapterCodec.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                 //将adapter 添加到spinner中
        codecSpin.setAdapter(adapterCodec);                 //添加事件Spinner事件监听
        codecSpin.setOnItemSelectedListener(new SpinnerSelectedListenerCodec());                 //设置默认值
        codecSpin.setVisibility(View.VISIBLE);

        mDataSetting 		= new DataSetting(context);
        serverAddr	 = view.findViewById(R.id.serverAddr);
        serverPort	 = view.findViewById(R.id.serverPort);
        filePath	 = view.findViewById(R.id.filePath);
        localIp		 = view.findViewById(R.id.localip);
        localIp.setText(NetUtils.getIPAddress(context));
        String addr  = mDataSetting.readData(DataSetting.DADDR);
        String port  = mDataSetting.readData(DataSetting.DPORT);
        String file  = mDataSetting.readData(DataSetting.DFILE);
        String reso  = mDataSetting.readData(DataSetting.RESOINDEX);
        String codec = mDataSetting.readData(DataSetting.CODECTYPE);
        if("".equals(addr) || addr==null) {
            String tmp = "192.168.1.18";
            mDataSetting.InsertOrUpdate(DataSetting.DADDR, tmp);
            serverAddr.setText(tmp);
        }
        else serverAddr.setText("" + addr);

        if("".equals(port) || port==null) {
            String tmp = "38123";
            mDataSetting.InsertOrUpdate(DataSetting.DPORT, tmp);
            serverPort.setText(tmp);
        }
        else serverPort.setText("" + port);

        if("".equals(file) || file==null) {
            String tmp = "/sdcard/tmp.h264";
            mDataSetting.InsertOrUpdate(DataSetting.DFILE, tmp);
            filePath.setText(tmp);
        }
        else filePath.setText("" + file);

        if("".equals(reso) || reso==null) {
            String tmp = "2";
            mDataSetting.InsertOrUpdate(DataSetting.RESOINDEX, tmp);
            resoPinner.setSelection(2, true);
        }else
        {
            resoPinner.setSelection(Integer.parseInt(reso), true);
        }

        if("".equals(codec) || codec==null) {
            String tmp = "1";//h264
            mDataSetting.InsertOrUpdate(DataSetting.CODECTYPE, tmp);
            codecSpin.setSelection(0, true);
        }else
        {
            int index = Integer.parseInt(codec);
            index--;
            if(index<0) index = 0;
            codecSpin.setSelection(index, true);
        }



        serverAddr.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                // TODO Auto-generated method stub
                Log.e(TAG, "emoteAddr: " + serverAddr.getText().toString() );
                String addr = serverAddr.getText().toString();
                mDataSetting.InsertOrUpdate(DataSetting.DADDR, addr);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        serverPort.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                // TODO Auto-generated method stub
                Log.e(TAG, "emotePort: " + serverPort.getText().toString() );
                String addr = serverPort.getText().toString();
                mDataSetting.InsertOrUpdate(DataSetting.DPORT, addr);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        filePath.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                // TODO Auto-generated method stub
                Log.e(TAG, "emotePort: " + filePath.getText().toString() );
                String addr = filePath.getText().toString();
                mDataSetting.InsertOrUpdate(DataSetting.DFILE, addr);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        return view;
    }
}
