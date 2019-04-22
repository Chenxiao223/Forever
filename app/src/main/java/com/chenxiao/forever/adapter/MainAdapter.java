package com.chenxiao.forever.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.chenxiao.forever.fragment.ContactsFragment;
import com.chenxiao.forever.fragment.HomePageFragment;
import com.chenxiao.forever.fragment.MeFragment;

public class MainAdapter extends FragmentPagerAdapter {
    public MainAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(position==0){
            return new HomePageFragment();
        }else if(position==1){
            return new ContactsFragment();
        }else{
            return new MeFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
