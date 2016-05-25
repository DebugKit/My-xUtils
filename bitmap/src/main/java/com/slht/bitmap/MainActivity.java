package com.slht.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;

import libcore.io.DiskLruCacheUtils;

public class MainActivity extends AppCompatActivity {


    private static final String DISK_CACHE_SUBDIR = "temp";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10;
    float density;
    private ImageView imageView;
    private Button button;

    private LruCacheUtils lruCacheUtils;

    private String url = "http://attach.bbs.miui.com/forum/201502/08/140500hd76c7tp01p6vp9w.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        button = (Button) findViewById(R.id.button);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        density = metrics.density;

//        lruCacheUtils = LruCacheUtils.getInstance();
        lruCacheUtils = LruCacheUtils.getInstance();
        lruCacheUtils.open(this, DISK_CACHE_SUBDIR, DISK_CACHE_SIZE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = lruCacheUtils.getBitmapFromLruCache(url);
                if (bitmap == null) {
                    InputStream is = lruCacheUtils.getDiskCache(url);
                    if (is == null) {
                        lruCacheUtils.putCache(url, new LruCacheUtils.SuccessCallBack<Bitmap>() {
                            @Override
                            public void response(Bitmap entity) {
                                Toast.makeText(MainActivity.this, "网络获取", Toast.LENGTH_SHORT).show();
                                imageView.setImageBitmap(entity);
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "磁盘缓存中获取", Toast.LENGTH_SHORT).show();
                        bitmap = BitmapFactory.decodeStream(is);
                        lruCacheUtils.addBitmapToLruCache(url, bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "内存缓存中获取", Toast.LENGTH_SHORT).show();
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        lruCacheUtils.flush();
    }

    @Override
    protected void onStop() {
        super.onStop();
        lruCacheUtils.close();
    }
}
