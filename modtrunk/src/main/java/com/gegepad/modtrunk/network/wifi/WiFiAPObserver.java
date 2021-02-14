package com.gegepad.modtrunk.network.wifi;

import java.util.HashSet;

/**
 * @ClassName:  WiFiAPObserver   
 * @Description:  Listenning the state of wifi hot
 * @author: jajuan.wang  
 * @date:   2015-06-09 00:58  
 * version:1.0.0
 */
public class WiFiAPObserver implements WiFiAPListener {
	
	/**
	 * the set to save all registed listener
	 */
	private HashSet<WiFiAPListener> listenerSet = new HashSet<WiFiAPListener>();
	
	/**
	 * add wiFiAPListener
	 * @param wiFiAPListener
	 */
	public void addWiFiAPListener(WiFiAPListener wiFiAPListener) {
		if (!listenerSet.contains(wiFiAPListener)) {
			listenerSet.add(wiFiAPListener);
		}
	}
	
	/**
	 * remove the wiFiAPListener
	 * @param wiFiAPListener
	 */
	public void removeWiFiAPListener(WiFiAPListener wiFiAPListener) {
		if (listenerSet.contains(wiFiAPListener)) {
			listenerSet.remove(wiFiAPListener);
		}
	}
	
	/**
	 * remove all WiFiAPListener
	 */
	public void clearWiFiAPListener() {
		listenerSet.clear();
	}

	@Override
	public void stateChanged(int state) {
		for (WiFiAPListener wiFiAPListener : listenerSet) {
			wiFiAPListener.stateChanged(state);
		}
	}

}
