package com.chenxiao.forever.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.chaychan.library.BottomBarItem;
import com.chaychan.library.BottomBarLayout;
import com.chenxiao.forever.Util.ToastUtils;
import com.chenxiao.forever.adapter.MainAdapter;
import com.chenxiao.forever.forever.R;
import com.chenxiao.forever.view.NoScrollViewPager;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;

import butterknife.BindView;

public class MainActivity extends BaseActivity {
    @BindView(R.id.bbl)
    BottomBarLayout mBottomBarLayout;
    @BindView(R.id.iv_system)
    ImageView iv_system;

    private MainAdapter homePageAdapter = null;
    public NoScrollViewPager pager = null;
    //退出时的时间
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
        initView();
        initListener();
    }

    private void initView() {
        //设置系统栏颜色
        ImageView iv_system = (ImageView) findViewById(R.id.iv_system);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv_system.getLayoutParams();
        params.height = (int) getStatusBarHeight(this);//设置当前控件布局的高度

        pager = findViewById(R.id.viewpager);
        pager.setOffscreenPageLimit(3);//
        homePageAdapter = new MainAdapter(getSupportFragmentManager());
        pager.setAdapter(homePageAdapter);
        pager.setCurrentItem(0);
    }

    private void initListener() {
        mBottomBarLayout.setOnItemSelectedListener(new BottomBarLayout.OnItemSelectedListener() {
            @Override
            public void onItemSelected(BottomBarItem bottomBarItem, int previousPosition, int currentPosition) {
                if (currentPosition == 0) {
                    pager.setCurrentItem(0);
                }else if (currentPosition ==1){
                    pager.setCurrentItem(1);
                }else{
                    pager.setCurrentItem(2);
                }
            }
        });

    }

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        ToastUtils.showShort(mContext, R.string.mainactivity_text1);
                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                        ToastUtils.showShort(mContext, R.string.mainactivity_text2);
                    } else {
                        if (NetUtils.hasNetwork(MainActivity.this)) {
                            //连接不到聊天服务器
                            ToastUtils.showShort(mContext, R.string.mainactivity_text4);
                        } else {
                            //当前网络不可用，请检查网络设置
                            ToastUtils.showShort(mContext, R.string.mainactivity_text4);
                        }
                    }
                }
            });
        }
    }

    //对返回键进行监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            ToastUtils.showShort(this, "再按一次退出应用");
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
