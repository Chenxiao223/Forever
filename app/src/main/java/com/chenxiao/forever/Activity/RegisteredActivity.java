package com.chenxiao.forever.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chenxiao.forever.Util.ToastUtils;
import com.chenxiao.forever.forever.R;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;

import butterknife.BindView;
import butterknife.OnClick;
import me.leefeng.promptlibrary.PromptDialog;

public class RegisteredActivity extends BaseActivity {
    @BindView(R.id.et_username)
    EditText et_username;
    @BindView(R.id.et_user_pwd)
    EditText et_user_pwd;
    @BindView(R.id.et_user_pwd_agin)
    EditText et_user_pwd_agin;
    @BindView(R.id.tv_login)
    TextView tv_login;
    @BindView(R.id.tv_regist)
    TextView tv_regist;

    private PromptDialog promptDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_registered);
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.tv_regist, R.id.tv_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_login:
                startActivity(new Intent(RegisteredActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.tv_regist:
                final String userName = et_username.getText().toString().trim();
                final String password = et_user_pwd.getText().toString().trim();
                final String password_agin = et_user_pwd_agin.getText().toString().trim();
                if (password.equals(password_agin)) {
                    promptDialog = new PromptDialog(mContext);
                    promptDialog.showLoading(getResources().getString(R.string.Is_the_registered));
                    new Thread(new Runnable() {
                        public void run() {
                            try {

                                EMClient.getInstance().createAccount(userName, password);//同步方法
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.showShort(mContext, R.string.Rregistered_successfully);
                                        promptDialog.dismiss();
                                        finish();
                                    }
                                });
                            } catch (final HyphenateException e) {
                                RegisteredActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                    if (!LoginActivity.this.isFinishing())
//                                        pd.dismiss();
                                        promptDialog.dismiss();
                                        int errorCode = e.getErrorCode();
                                        if (errorCode == EMError.NETWORK_ERROR) {
                                            ToastUtils.showShort(mContext, R.string.network_anomalies);
                                        } else if (errorCode == EMError.USER_ALREADY_EXIST) {
                                            ToastUtils.showShort(mContext, R.string.User_already_exists);
                                        } else if (errorCode == EMError.USER_AUTHENTICATION_FAILED) {
                                            ToastUtils.showShort(mContext, R.string.registration_failed_without_permission);
                                        } else if (errorCode == EMError.USER_ILLEGAL_ARGUMENT) {
                                            ToastUtils.showShort(mContext, R.string.illegal_user_name);
                                        } else {
                                            ToastUtils.showShort(mContext, R.string.Registration_failed);
                                        }
                                    }
                                });
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } else {
                    ToastUtils.showShort(mContext, R.string.activity_regist_text);
                }
                break;
        }
    }

}
