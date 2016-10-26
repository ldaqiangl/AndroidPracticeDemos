package com.dysania.autoscrollbannerdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by Dysania on 2016/10/26.
 */

public class BannerFragment extends Fragment {

    private int mImgId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_banner, container, false);
        ImageView ivBannerItem = (ImageView) view.findViewById(R.id.iv_banner_item);
        ivBannerItem.setImageResource(mImgId);
        return view;
    }

    public void setImgId(int imgId) {
        this.mImgId = imgId;
    }

}
