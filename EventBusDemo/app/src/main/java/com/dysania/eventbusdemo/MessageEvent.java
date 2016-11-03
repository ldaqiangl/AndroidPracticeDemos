package com.dysania.eventbusdemo;

/**
 * Created by DysaniazzZ on 2016/11/3.
 * Step1: Define events
 * 定义要传递的事件，里面可以包含要传递的具体信息
 */

public class MessageEvent {

    public final String mMessage;

    public MessageEvent(String message) {
        this.mMessage = message;
    }
}
