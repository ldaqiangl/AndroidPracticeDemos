package com.dysania.appupgradedemo;

/**
 * Created by DysaniazzZ on 2016/12/1.
 */

public class BugClass {

    public String bug() {
        //制造一个bug：空指针
        String str = null;
        int length = str.length();
        return "This is a fixed bug class";
    }
}
