package com.dysania.appupgradedemo;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;

/**
 * Created by DysaniazzZ on 2016/12/1.
 */

public class MyApplication extends TinkerApplication {
    public MyApplication() {
        //参数1：tinkerFlags表示Tinker支持的类型，dex only、library only or all support，default: TINKER_ENABLE_ALL
        //参数2：delegateClassName Application代理类，这里填写自定义的ApplicationLike
        //参数3：loaderClassName Tinker的加载器，使用默认即可
        //参数4：tinkerLoadVerifyFlag加载dex或者lib是否验证md5，默认为false
        super(ShareConstants.TINKER_ENABLE_ALL, "com.dysania.appupgradedemo.MyApplicationLike", "com.tencent.tinker.loader.TinkerLoader", false);
    }
}
