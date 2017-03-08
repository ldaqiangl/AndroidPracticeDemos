package com.dysania.webviewjsdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Created by DysaniazzZ on 06/03/2017.
 */
public class MainActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView) findViewById(R.id.web_view);
        initWebView();
    }

    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        //启用Java代码调用JS代码
        webSettings.setJavaScriptEnabled(true);
        //启用JS代码调用Java代码
        mWebView.addJavascriptInterface(this, "injectedObject");      //包含暴露给JS调用方法的类，以及规定JS调用该对象方法时使用的别名
        mWebView.loadUrl("file:///android_asset/test.html");
    }

    public void javaCallJs(View view) {
        //无参数调用
        mWebView.loadUrl("javascript:javaCallJs()");
    }

    public void javaCallJsWithArgs(View view) {
        //带参数调用
        mWebView.loadUrl("javascript:javaCallJsWithArgs(" + " 'data from java' " + ")");
    }

    @JavascriptInterface
    public void jsCallJava() {
        Toast.makeText(this, "Js调用Java", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void jsCallJavaWithArgs(String arg0) {
        Toast.makeText(this, "Js调用Java并传递参数:" + arg0, Toast.LENGTH_SHORT).show();
    }
}
