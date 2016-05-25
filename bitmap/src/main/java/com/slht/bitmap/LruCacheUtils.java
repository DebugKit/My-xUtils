package com.slht.bitmap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import libcore.io.DiskLruCache;

/**
 * Created by LI on 2016/5/9.
 */
public class LruCacheUtils {

    private static LruCacheUtils lruCacheUtils;

    private DiskLruCache mDiskLruCache;//磁盘缓存
    private LruCache<String, Bitmap> mLruCache;//内存缓存
    private Context context;

    private LruCacheUtils() {
    }

    public static LruCacheUtils getInstance() {
        if (lruCacheUtils == null) {
            synchronized (LruCacheUtils.class) {
                if (lruCacheUtils == null) {
                    lruCacheUtils = new LruCacheUtils();
                }
            }
        }
        return lruCacheUtils;
    }


    /**
     * 打开缓存
     *
     * @param context
     * @param disk_cache_subdir
     * @param disk_cache_size
     */
    public void open(Context context, String disk_cache_subdir, int disk_cache_size) {
        try {
            this.context = context;
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = am.getMemoryClass();
            int lruCacheMemorySize = 1024 * 1024 * memoryClass / 8;
            mLruCache = new LruCache<String, Bitmap>(lruCacheMemorySize);
            mDiskLruCache = DiskLruCache.open(getCacheDir(disk_cache_subdir), getAppVersion(), 1, disk_cache_size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 缓存进内存和磁盘中
     *
     * @param url
     * @param callBack
     */
    public void putCache(String url, final SuccessCallBack callBack) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                String key = hashKeyForDisk(params[0]);
                DiskLruCache.Editor editor = null;
                HttpURLConnection conn = null;
                ByteArrayOutputStream baos = null;
                BufferedInputStream is = null;
                Bitmap bitmap = null;
                try {
                    URL url = new URL(params[0]);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(1000 * 20);
                    conn.setConnectTimeout(1000 * 20);
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        is = new BufferedInputStream(conn.getInputStream());
                        baos = new ByteArrayOutputStream();

                        byte[] bytes = new byte[1024];
                        int len = -1;
                        while ((len = is.read(bytes)) != -1) {
                            baos.write(bytes, 0, len);
                        }
                    }

                    if (baos != null) {
                        bitmap = decodeSampleBitmapFromStrem(baos.toByteArray(), 240, 120);
                        addBitmapToLruCache(params[0], bitmap);
                        editor = mDiskLruCache.edit(key);
                        //位图压缩后输出(压缩格式、质量（100表示不压缩，30表示压缩70%）、输出流)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, editor.newOutputStream(0));
                        editor.commit();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (editor != null) {
                            editor.abort();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (baos != null) {
                        try {
                            baos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    conn.disconnect();
                }

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                callBack.response(bitmap);
            }
        }.execute(url);
    }

    /**
     * 获取磁盘缓存
     *
     * @param url
     * @return
     */
    public InputStream getDiskCache(String url) {
        String key = hashKeyForDisk(url);
        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                return snapshot.getInputStream(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取计算后的位图
     *
     * @param bytes
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap decodeSampleBitmapFromStrem(byte[] bytes, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        return bitmap;
    }

    /**
     * 添加到内存缓存
     *
     * @param url
     * @param bitmap
     */
    public void addBitmapToLruCache(String url, Bitmap bitmap) {
        String key = hashKeyForDisk(url);
        if (getBitmapFromLruCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    /**
     * 获取内存缓存
     *
     * @param url
     * @return
     */
    public Bitmap getBitmapFromLruCache(String url) {
        String key = hashKeyForDisk(url);
        return mLruCache.get(key);
    }

    /**
     * 计算采样比例
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    private String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 获取app版本
     *
     * @return
     */
    private int getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 获取缓存目录
     *
     * @param disk_cache_subdir
     * @return
     */
    private File getCacheDir(String disk_cache_subdir) {
        String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment
                .isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir()
                .getPath();

        return new File(cachePath + File.separator + disk_cache_subdir);
    }

    /**
     * 关闭磁盘缓存
     */
    public void close() {
        if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 刷新磁盘缓存
     */
    public void flush() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //回调接口
    public interface SuccessCallBack<T> {
        void response(T entity);
    }
}
