package com.gegepad.SurfaceVideo.evenbus.subscribe;


import com.gegepad.SurfaceVideo.evenbus.event.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by
 */

public class EventThread extends Thread{

    public EventThread(){
        EventBus.getDefault().register(this);
    }

    public void unregister(){
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void run() {
       while (true){ }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void  onEventBackgroundThread(MessageEvent event){
        System.out.println("onEventBackgroundThread::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onEventAsync(MessageEvent event){
        System.out.println("onEventAsync::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMain(MessageEvent event){
        System.out.println("onEventMain::"+" "+Thread.currentThread().getName());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventPosting(MessageEvent event){
        System.out.println("onEventPosting::"+" "+Thread.currentThread().getName());
    }

}
