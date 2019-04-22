package com.chenxiao.forever.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chenxiao.forever.Util.CustomDialog;
import com.chenxiao.forever.Util.ToastUtils;
import com.chenxiao.forever.forever.R;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;

import butterknife.BindView;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class LoginActivity extends BaseActivity {
    @BindView(R.id.et_username)
    EditText et_username;
    @BindView(R.id.et_user_pwd)
    EditText et_user_pwd;
    @BindView(R.id.tv_regist)
    TextView tv_regist;
    private PromptDialog promptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_login);
        super.onCreate(savedInstanceState);

    }


    @OnClick({R.id.login_tv, R.id.tv_regist})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_regist:
                startActivity(new Intent(LoginActivity.this, RegisteredActivity.class));
                break;
            case R.id.login_tv:
                final CustomDialog customDialog = new CustomDialog(mContext);
                customDialog.setTitle("消息提示");
                customDialog.setMessage("你爱敏敏吗？");
                customDialog.setYesOnclickListener("YES", new CustomDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        //这里是确定的逻辑代码，别忘了点击确定后关闭对话框
                        login();
                        customDialog.dismiss();
                    }
                });
                customDialog.setNoOnclickListener("NO", new CustomDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        customDialog.dismiss();
                    }
                });
                customDialog.show();


                break;
        }
    }

    public void login() {
        final String userName = et_username.getText().toString().trim();
        final String password = et_user_pwd.getText().toString().trim();
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            promptDialog = new PromptDialog(mContext);
            promptDialog.showLoading(getResources().getString(R.string.Is_landing));
            EMClient.getInstance().login(userName, password, new EMCallBack() {//回调
                @Override
                public void onSuccess() {
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            promptDialog.dismiss();
                            finish();
                        }
                    });
                }

                @Override
                public void onProgress(int progress, String status) {

                }

                @Override
                public void onError(int code, final String message) {
                    Log.i("main", "登录聊天服务器失败！");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            promptDialog.dismiss();
                            ToastUtils.showShort(mContext, R.string.Login_failed + message);
                        }
                    });
                }
            });
        } else {
            ToastUtils.showShort(mContext, R.string.activity_login_text);
        }
    }

}
