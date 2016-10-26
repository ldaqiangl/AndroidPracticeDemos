package com.dysania.autoscrollbannerdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Dysania on 2016/10/26.
 */
public class MainActivity extends AppCompatActivity {

    private ViewPager mVpMainBanner;
    private Handler mHandler = new Handler();
    //广告条的图片资源
    private int[] mImgRes = new int[]{R.mipmap.banner_01, R.mipmap.banner_02, R.mipmap.banner_03};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mVpMainBanner = (ViewPager) findViewById(R.id.vp_main_banner);
        //ViewPager和Fragment一起使用，有专门的FragmentPagerAdapter和FragmentStatePagerAdapter供Fragment和ViewPager使用
        mVpMainBanner.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        autoScroll();
    }

    private void autoScroll() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int currentItem = mVpMainBanner.getCurrentItem();
                currentItem++;
                mVpMainBanner.setCurrentItem(currentItem);
                mHandler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        //返回Fragment的视图
        @Override
        public Fragment getItem(int position) {
            //注意这里要重新设置position的值以防止数组角标越界
            position %= mImgRes.length;
            BannerFragment bannerFragment = new BannerFragment();
            bannerFragment.setImgId(mImgRes[position]);
            return bannerFragment;
        }

        //返回条目的数量
        @Override
        public int getCount() {
            //return mImgRes.length;
            return Integer.MAX_VALUE;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

}
