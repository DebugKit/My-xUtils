package com.slht.volley;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

@ContentView(value = R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";

    RequestQueue queue = null;
    @ViewInject(value = R.id.imageView)
    private ImageView imageView;

    @ViewInject(value = R.id.networkImageView)
    private NetworkImageView networkImageView;
//    @ViewInject(value = R.id.button_get)
//    private Button button_get;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        x.view().inject(this);
        queue = Volley.newRequestQueue(this);

        networkImageView.setErrorImageResId(R.mipmap.ic_launcher);
        networkImageView.setDefaultImageResId(R.mipmap.ic_launcher);
        networkImageView.setImageUrl("http://pic1.nipic.com/2008-11-13/2008111384358912_2.jpg", new ImageLoader
                (queue, new BitmapCache()));
    }

    //get请求
    @Event(value = R.id.button_get, type = View.OnClickListener.class)
    private void onClick(View view) {
        String url = "http://www.baidu.com";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });
        queue.add(request);
    }

    //带参数的post请求
    @Event(value = R.id.button_post, type = View.OnClickListener.class)
    private void on2Click(View view) {
        String url = "http://192.168.31.111:8080/XMLServer/XMLServlet";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", "123");
                return params;
            }
        };
        queue.add(request);
    }

    @Event(value = {R.id.button_json, R.id.button_image, R.id.button_cache}, type = View.OnClickListener.class)
    private void onAllClick(View view) {
        switch (view.getId()) {
            case R.id.button_json:
                String url = "http://apistore.baidu.com/microservice/weather?cityid=101270101";
                JSONObject object = null;
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, object, new Response
                        .Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
                queue.add(request);
                break;
            case R.id.button_image:
                String url1 = "http://pic15.nipic.com/20110731/8022110_162804602317_2.jpg";
                ImageRequest request1 = new ImageRequest(url1, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(android.graphics.Bitmap response) {
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        imageView.setImageBitmap(response);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
                queue.add(request1);
                break;
            case R.id.button_cache:
                String url2 = "http://pic15.nipic.com/20110731/8022110_162804602317_2.jpg";
                BitmapCache bitmapCache = new BitmapCache();
                ImageLoader imageLoader = new ImageLoader(queue, bitmapCache);
                ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView, R.mipmap
                        .ic_launcher, R.mipmap.ic_launcher);
                imageLoader.get(url2, imageListener, 0, 240);
                break;
        }
    }

    private class BitmapCache implements ImageLoader.ImageCache {

        private LruCache<String, Bitmap> cache;
        private int maxSize = 5 * 1021 * 1024;

        public BitmapCache() {
            cache = new LruCache<String, Bitmap>(maxSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getRowBytes() * value.getHeight();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return cache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            cache.put(url, bitmap);
        }
    }
}
