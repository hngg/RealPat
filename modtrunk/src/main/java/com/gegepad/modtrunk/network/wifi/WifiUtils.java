package com.gegepad.modtrunk.network.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:  WifiHotUtil   
 * @Description:  打印日志信息WiFi热点工具
 * @author: jajuan.wang  
 * @date:   2015-05-28 15:12  
 * version:1.0.0
 */   
public class WifiUtils
{  
    public static final String TAG = "WifiUtils";  
      
    public static final int WIFICIPHER_NOPASS 	= 1;
    public static final int WIFICIPHER_WEP 	= 2;
    public static final int WIFICIPHER_WPA 	= 3;
    
    private WifiManager mWifiManager 		= null;  
    //private WifiP2pManager mWifiP2PManager  = null;
    
    private Context mContext = null;  
    public WifiUtils(Context context) 
    {
    	mContext = context;
    	mWifiManager 	= (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }
    
    private String intToIp(int i) {
  	  return (i & 0xFF ) + "." +
  	  ((i >> 8 ) & 0xFF) + "." +
  	  ((i >> 16 ) & 0xFF) + "." +
  	  ( i >> 24 & 0xFF) ;
    }
    
    /////////////////////////////////////////////wifi///////////////////////////////////
    
    public boolean isWifiEnable()
    {
        return mWifiManager.isWifiEnabled();
    }

    public boolean setWifiEnabled(boolean able)
    {
    	return mWifiManager.setWifiEnabled(able);
    }
    
    public List<ScanResult> getScanResults()
    {
    	return mWifiManager.getScanResults();
    }
    
    public String getWifiIp()
    {
    	  WifiInfo wi = mWifiManager.getConnectionInfo();
    	  //获取32位整型IP地址
    	  int ipAdd=wi.getIpAddress();
    	  //把整型地址转换成“*.*.*.*”地址
    	  String ip=intToIp(ipAdd);
    	  return ip;
    }
    
    @SuppressWarnings("deprecation")
	public boolean isWifiConnected()
    {
    	ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
    	return mWifi.isConnected();  
    }
    
    public void wifiConnect(String wssid, String pass, int type)
    {
    	WifiConfiguration config = isExsits(wssid);
        if (config == null)
        {
            if (type != WIFICIPHER_NOPASS) 
            {//需要密码
               config = createWifiInfo(wssid, pass, type);  
            } else {
               config = createWifiInfo(wssid, "", type);
            }
        } 
        connect(config);
    }
    
    public boolean wifiDisconnect()
    {
    	return mWifiManager.disconnect();
    }
    
    public WifiInfo getConnectionInfo()
    {
    	return mWifiManager.getConnectionInfo();
    }
    
    //
    private int connect(WifiConfiguration config) 
    {
        int wcgID = mWifiManager.addNetwork(config);
        if(mWifiManager.enableNetwork(wcgID, true))
        	return wcgID;//-1 failed
        else
        	return -2;
    }
    
    public String getGateWayIpAddress() 
    {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.gateway;//
            return intToIp(address);
        }
        return null;
    }
    
    private WifiConfiguration createWifiInfo(String SSID, String password, int type) 
    {
        Log.w("AAA", "SSID = " + SSID + "password " + password + "type ="
                + type);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "\"" + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (type == WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }
    
    /**
     * 判断当前wifi是否有保存
     *
     * @param SSID
     * @return
     */
    private WifiConfiguration isExsits(String SSID) 
    {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs != null)
        for (WifiConfiguration existingConfig : existingConfigs) 
        {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) 
            {
                return existingConfig;
            }
        }
        return null;
    }
    
    /**
     * 获取连接到热点上的手机ip
     *
     * @return
     */
    public ArrayList<String> getConnectedIP() 
    {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) 
            {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    if(ip.length()>6)
                    	connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }
    
    public String getDestAddr(){
		String addr = "";
    	if (isWifiApEnabled()){
    		ArrayList<String> strArr = getConnectedIP();
    		addr = strArr.get(0);
    		Log.i(TAG, "connected wifi addr:"+addr);
    	}
    	else
    		addr = "192.168.43.1";
    	return addr;
	}
    
    ////////////////////////////////////////////////wifi ap//////////////////////////////////////////
    //判断热点是否打开
    public boolean isWifiApEnabled() 
    {   
        try 
        {  
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");  
            method.setAccessible(true);  
            return (Boolean) method.invoke(mWifiManager);  
        } catch (NoSuchMethodException e) 
        {  
            e.printStackTrace();  
        } catch (Exception e) 
        {  
            e.printStackTrace();  
        }  
        return false;  
    }
    
    public String getWifiHotspotSSID()
    {
    	String ssid = "";
	    try {
	        //拿到getWifiApConfiguration()方法
	        Method method = mWifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
	        //调用getWifiApConfiguration()方法，获取到 热点的WifiConfiguration
	        WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);
	        ssid = configuration.SSID;
	      } catch (NoSuchMethodException e) {
	        e.printStackTrace();
	      } catch (InvocationTargetException e) {
	        e.printStackTrace();
	      } catch (IllegalAccessException e) {
	        e.printStackTrace();
	      }
	    return ssid;
    }
    
    /**
     * 创建Wifi热点
     */
    public boolean createWifiHotspot(String ssid, String key) 
    {
        Log.d(TAG, "create wifi hotspot ____________________________________1");
		////wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
		setWifiEnabled(false);
        Log.d(TAG, "create wifi hotspot ____________________________________2");
        WifiConfiguration config = new WifiConfiguration();
        config.SSID 		= ssid;
        config.preSharedKey = key;
        config.hiddenSSID 	= true;
        Log.d(TAG, "create wifi hotspot ____________________________________3");
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);//开放系统认证
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
        Log.d(TAG, "create wifi hotspot ____________________________________4");
        //通过反射调用设置热点
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                Method configMethod = mWifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
            Log.d(TAG, "create wifi hotspot ____________________________________5");
                //boolean isConfigured = (Boolean) configMethod.invoke(mWifiManager, config);
            Log.d(TAG, "create wifi hotspot ____________________________________6");
                Method method = mWifiManager.getClass().getMethod("startSoftAp", WifiConfiguration.class);
                Log.d(TAG, "create wifi hotspot ____________________________________7");
                //返回热点打开状态
                return (Boolean) method.invoke(mWifiManager, config);
            }else {
                Method method = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);

                boolean enable = (Boolean) method.invoke(mWifiManager, config, true);
                return enable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 关闭WiFi热点
     */
    public void destroyWifiHotspot() 
    {
        try {
            Method method 				= mWifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config 	= (WifiConfiguration) method.invoke(mWifiManager);
            Method method2 				= mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(mWifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取开启便携热点后自身热点IP地址
     * @param
     * @return
     */
    public String getHotspotLocalIpAddress() 
    {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        if(dhcpInfo != null) {
            int address = dhcpInfo.serverAddress;//gateway
            return intToIp(address);
        }
        return null;
    }
    
    public String getLocalIpAddress() 
    {  
        //检查wifi是否开启  
        WifiInfo wifiinfo = mWifiManager.getConnectionInfo();  
        int ip = wifiinfo.getIpAddress();  
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >>  
                24) & 0xFF);  
    }  
    
    /** 
     * 设置手机的移动数据 
     * 要么手机root过，要么应用程序是系统签名，不然控制不了 
     */  
    public static boolean setMobileDataState(Context context, boolean enabled) {
        try {  
            ConnectivityManager connectivityManager =  
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
            Method setMobileDataEnabl;  
            try {  
                setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod  
                        ("setMobileDataEnabled", boolean.class);  
                setMobileDataEnabl.invoke(connectivityManager, enabled);  
  
                return true;  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
            System.out.println("移动数据设置错误: " + e.toString());  
            return false;  
        }  
        return false;  
    }  
  
    /** 
     * 返回手机移动数据的状态 
     * 
     * @param pContext 
     * @param arg      默认填null 
     * @return true 连接 false 未连接 
     */  
    public static boolean getMobileDataState(Context pContext, Object[] arg) {  
        try {  
  
            ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext  
                    .getSystemService(Context.CONNECTIVITY_SERVICE);  
  
            Class ownerClass = mConnectivityManager.getClass();  
  
            Class[] argsClass = null;  
            if (arg != null) {  
                argsClass = new Class[1];  
                argsClass[0] = arg.getClass();  
            }  
  
            Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);  
  
            Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);  
  
            return isOpen;  
  
        } catch (Exception e) {  
            // TODO: handle exception  
  
            System.out.println("得到移动数据状态出错");  
            return false;  
        }  
  
    }
}

