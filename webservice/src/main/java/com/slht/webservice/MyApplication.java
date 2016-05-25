package com.slht.webservice;

import android.app.Application;

import org.xutils.x;

/**
 * Created by LI on 2016/5/3.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//初始化xUtils

    }
}
