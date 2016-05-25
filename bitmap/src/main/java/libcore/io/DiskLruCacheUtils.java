package libcore.io;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by LI on 2016/5/9.
 */
public class DiskLruCacheUtils {

    private static DiskLruCacheUtils diskLruCacheUtils;

    private DiskLruCache diskLruCache;
    private Context context;

    private DiskLruCacheUtils() {
    }

    public static DiskLruCacheUtils getInstance() {
        if (diskLruCacheUtils == null) {
            synchronized (DiskLruCacheUtils.class) {
                if (diskLruCacheUtils == null) {
                    diskLruCacheUtils = new DiskLruCacheUtils();
                }
            }
        }
        return diskLruCacheUtils;
    }

    /**
     * @param context
     * @param disk_cache_subdir 缓存地址
     * @param disk_cache_size   缓存字节 默认10M
     */
    public void open(Context context, String disk_cache_subdir, int disk_cache_size) {
        this.context = context;
        try {
            diskLruCache = DiskLruCache.open(getCacgeDir(disk_cache_subdir), getAppVersion(), 1, disk_cache_size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InputStream get(String url) {
        String key = hashKeyForDisk(url);
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null) {
                return snapshot.getInputStream(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void put(String url) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {
                String key = hashKeyForDisk(params[0]);
                DiskLruCache.Editor editor = null;
                BufferedOutputStream out = null;
                BufferedInputStream in = null;
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(params[0]);
                    conn = (HttpURLConnection) url.openConnection();
                    editor = diskLruCache.edit(key);
                    out = new BufferedOutputStream(editor.newOutputStream(0), 8 * 1024);
                    in = new BufferedInputStream(conn.getInputStream(), 8 * 1024);
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    while ((len = in.read(bytes)) != -1) {
                        out.write(bytes, 0, len);
                    }
                    editor.commit();//提交
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        editor.abort();//放弃写入
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
                return null;
            }
        }.execute(url);
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

    //获取程序的版本号
    private int getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    //获取缓存目录
    private File getCacgeDir(String disk_cache_subdir) {
        String cachePath = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment
                .isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() : context.getCacheDir()
                .getPath();
        return new File(cachePath + File.separator + disk_cache_subdir);
    }
}
