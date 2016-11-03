package com.dysania.eventbusdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by DysaniazzZ on 2016/11/3.
 */

public class BaseActivity extends AppCompatActivity {

    public Context mContext;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        //Step 2: Prepare Subscribers
        EventBus.getDefault().register(mContext);
    }

    //EventBus3以后的方法名可以任意，是通过注解订阅事件的
    //ThreadMode.MAIN 无论在哪个线程post的事件，都是在UI线程运行。如果post事件的线程就是UI线程，就会直接运行该事件。
    //ThreadMode.ASYNC 无论在哪个线程post的事件，都是在一个线程池中运行。可以做一些耗时操作，比如连接网络等。
    //ThreadMode.BACKGROUND 无论在哪个线程post的事件，都是在后台线程运行。如果post事件的线程就是后台线程，就会直接运行该事件。由于是使用一个单独的子线程依次传递所有的事件，应该避免阻塞子线程。
    //ThreadMode.POSTING 在哪个线程post的事件，就在哪个线程运行，这个是默认的。
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "onMessageEvent: " + getClass().getSimpleName());
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(mContext);
    }
}
