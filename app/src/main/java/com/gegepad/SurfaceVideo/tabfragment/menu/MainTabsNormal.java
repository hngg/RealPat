package com.gegepad.SurfaceVideo.tabfragment.menu;


import com.gegepad.SurfaceVideo.R;
import com.gegepad.SurfaceVideo.tabfragment.CameraTabFragment;
import com.gegepad.SurfaceVideo.tabfragment.HomeTabFragment;
import com.gegepad.SurfaceVideo.tabfragment.SettingTabFragment;

public enum MainTabsNormal {
    Home(0,"文件", R.drawable.selector_tab_home, HomeTabFragment.class),
    Classify(1,"相机", R.drawable.selector_tab_home, CameraTabFragment.class),
    ShoppingCar(2,"设置", R.drawable.selector_tab_home, SettingTabFragment.class);

    private int i;
    private String name;
    private int icon;
    private Class<?> cla;

     MainTabsNormal(int i, String name, int icon, Class<?> cla) {
        this.i = i;
        this.name = name;
        this.icon = icon;
        this.cla = cla;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public Class<?> getCla() {
        return cla;
    }

    public void setCla(Class<?> cla) {
        this.cla = cla;
    }
}
