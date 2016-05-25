package com.slht.bitmap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by LI on 2016/5/6.
 */
public class BitmapUtils {
    private LruCache<String, Bitmap> lruCache;


    public BitmapUtils(Activity activity) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();//获取当前activity所占内存
        final int cacheSize = memoryClass * 1024 * 1024 / 8;//设置缓存大小
        lruCache = new LruCache<String, Bitmap>(cacheSize);
    }

    /**
     * 位图重新采样
     *
     * @param res
     * @param resId     资源Id
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return
     */
    public static Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//设置为true可以先读出位图的宽高而不写进内存
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 计算位图的采样比例
     *
     * @param options
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        //获取位图原始尺寸
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;//默认inSampleSize为1
        if (width > reqWidth || height > reqHeight) {
            if (width > height) {//采样比例按照尺寸小的计算，如果按照尺寸大的计算可能会出现图像失真等问题
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public Bitmap getBitmapFromLruCache(String key) {
        Log.d("BitmapUtils", key);
        return lruCache.get(key);
    }

    public void addBitmapToLruCache(String key, Bitmap bitmap) {
        if (getBitmapFromLruCache(key) == null) {
            lruCache.put(key, bitmap);
        }
    }
}
