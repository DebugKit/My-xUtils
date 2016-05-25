package com.slht.webview;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);
        handler = new Handler();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyObject(), "demo");
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class MyObject {
        @JavascriptInterface
        public void clickOnAndroid() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:myfun()");
                }
            });
        }
    }
}
