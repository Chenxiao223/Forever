package com.chenxiao.forever.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.chenxiao.forever.Util.ClickUtils;
import com.chenxiao.forever.adapter.ContactsAdapter;
import com.chenxiao.forever.bean.User;
import com.chenxiao.forever.forever.R;
import com.chenxiao.forever.view.SideBar;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContactsFragment extends Fragment {
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_IS_QR_CODE = "key_code";
    public static final String KEY_IS_CONTINUOUS = "key_continuous_scan";

    private final int CONTACTSLIST = 10001;
    private final int SCAN_CODE = 1002;

    private SideBar side_bar;
    private ListView list_view;
    private ArrayList<User> list = new ArrayList<>();
    private RxPermissions rxPermissions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    //当滑到当前碎片时调用该方法
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (ClickUtils.isFastClick()) {//防止快速切换而闪退
                try {
                    //这里与下拉刷新代码一样
                    list.clear();
                    initData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        rxPermissions = new RxPermissions(getActivity());
        initView();
        initData();
        initListener();
    }

    public void initView() {
        side_bar = getView().findViewById(R.id.side_bar);
        list_view = getView().findViewById(R.id.list_view);
    }

    public void initListener() {
        side_bar.setOnStrSelectCallBack(new SideBar.ISideBarSelectCallBack() {
            @Override
            public void onSelectStr(int index, String selectStr) {
                for (int i = 0; i < list.size(); i++) {
                    if (selectStr.equalsIgnoreCase(list.get(i).getFirstLetter())) {
                        list_view.setSelection(i); // 选择到首字母出现的位置
                        return;
                    }
                }
            }
        });

        getView().findViewById(R.id.iv_add_friend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(aBoolean -> {
                    if (aBoolean) {
                        Intent it = new Intent();
                        it.setClass(getContext(), CaptureActivity.class);
                        //返回一个二维码的信息
                        startActivityForResult(it, SCAN_CODE);
                    }
                });

            }
        });

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CODE ) {
            Bundle bundle = data.getExtras();
            //返回二维码扫描的信息
            final String result = bundle.get("result").toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().addContact(result, "");
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<String> usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    for (int i = 0; i < usernames.size(); i++) {
                        list.add(new User(usernames.get(i).toString()));
                    }
                    handler.sendEmptyMessage(CONTACTSLIST);
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONTACTSLIST:
                    Collections.sort(list); // 对list进行排序，需要让User实现Comparable接口重写compareTo方法
                    ContactsAdapter adapter = new ContactsAdapter(getContext(), list);
                    list_view.setAdapter(adapter);
                    break;
            }
        }
    };

}
