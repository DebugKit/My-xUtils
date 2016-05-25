package com.slht.my_xutils;

import android.app.Application;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

/**
 * Created by LI on 2016/5/3.
 */
public class MyApplication extends Application {
    private DbManager.DaoConfig daoConfig;

    public DbManager.DaoConfig getDaoConfig() {
        return daoConfig;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);//初始化xUtils

        daoConfig = new DbManager.DaoConfig().setDbName("mydb")//创建数据库名称
                .setDbVersion(1)//数据库版本
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {

                    }
                });//数据库更新操作

//        DbManager dbManager = x.getDb(daoConfig);
//
//        User user = new User("xiaoming@139.com", "xiaoming");
//
//        try {
//            dbManager.save(user);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }

    }
}
