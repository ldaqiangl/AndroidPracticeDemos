package com.dysania.eventbusdemo;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by DysaniazzZ on 2016/11/3.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        配置当一个事件没有订阅者订阅时不打印日志和发送无订阅者的事件
//        EventBus eventBus = EventBus.builder()
//                .logNoSubscriberMessages(false)
//                .sendNoSubscriberEvent(false)
//                .build();

//        配置当订阅者收到事件的方法有异常时是否抛出
//        EventBus eventBus = EventBus.builder().throwSubscriberException(true).build();

        //配置默认的EventBus实例(installDefaultEventBus方法只能调用一次，后面的都会抛出异常）
        EventBus.builder().throwSubscriberException(BuildConfig.DEBUG).installDefaultEventBus();
    }
}
