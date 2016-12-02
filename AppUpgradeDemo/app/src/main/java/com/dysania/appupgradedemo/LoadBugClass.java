package com.dysania.appupgradedemo;

/**
 * Created by DysaniazzZ on 2016/12/1.
 */

public class LoadBugClass {

    public static String getBugString() {
        BugClass bugClass = new BugClass();
        return bugClass.bug();
    }
}
