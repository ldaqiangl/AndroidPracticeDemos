package com.dysania.okhttpdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OkHttpDemo";
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final String GET_URL = "https://publicobject.com/helloworld.txt";
    private static final String POST_URL = "https://api.github.com/markdown/raw";
    private static final String AUTH_URL = "https://publicobject.com/secrets/hellosecret.txt";

    OkHttpClient mOkHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initOkHttpClient();
    }

    private void initOkHttpClient() {
        //创建OkHttpClient对象
        mOkHttpClient = new OkHttpClient.Builder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic("jesse", "password1");    //the credential can be refresh_token in your project
                        if(credential.equals(response.request().header("Authorization"))) {
                            return null;    // If we already failed with these credentials, don't retry. You can log out or do something else before return.
                    }
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                })
                .build();
    }

    //发送GET请求
    public void httpGetClick(View view) {
        //创建一个Request对象
        final Request request = new Request.Builder()
                //.header(String name, String value)
                .url(AUTH_URL)
                .build();
        //创建Call对象
        Call call = mOkHttpClient.newCall(request);
        //请求加入调度（注意这里是子线程，不能直接刷新UI）
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //response.body().string()      返回字符串
                //response.body().bytes()       返回二进制数组
                //response.body().byteStream()  返回inputStreamF
                Log.d(TAG, "onResponse: " + response.code() + "\t" + response.body().string());
            }
        });
    }

    //发送Post请求
    public void httpPostClick(View view) {
        String content = "" + "Releases\n" + "--------\n" + "\n" + " * _1.0_ May 6, 2013\n" + " * _1.1_ June 15, 2013\n" + " * _1.2_ August 11, 2013\n";
        RequestBody body = RequestBody.create(MEDIA_TYPE_MARKDOWN, content);
        Request request = new Request.Builder()
                .url(POST_URL)
                .post(body)
                .build();
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse: " + response.code() + "\t" + response.body().string());
            }
        });
    }

}
