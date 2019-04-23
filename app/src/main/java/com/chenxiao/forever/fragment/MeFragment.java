package com.chenxiao.forever.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chenxiao.forever.Activity.LoginActivity;
import com.chenxiao.forever.Activity.MainActivity;
import com.chenxiao.forever.Util.ToastUtils;
import com.chenxiao.forever.adapter.EMCallBackAdapter;
import com.chenxiao.forever.forever.R;
import com.chenxiao.forever.utils.ThreadUtils;
import com.hyphenate.chat.EMClient;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.vondear.rxtool.RxFileTool;
import com.vondear.rxtool.RxPhotoTool;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import me.leefeng.promptlibrary.PromptDialog;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static android.app.Activity.RESULT_OK;
import static com.vondear.rxtool.RxPhotoTool.GET_IMAGE_FROM_PHONE;

public class MeFragment extends BaseFragment {
    @BindView(R.id.tv_user_name)
    TextView tv_user_name;
    @BindView(R.id.iv_head)
    CircleImageView iv_head;
    private PromptDialog promptDialog;
    private RxPermissions rxPermissions;
    private SharedPreferences.Editor editors;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_me;
    }

    @Override
    protected void init() {
        super.init();
        promptDialog = new PromptDialog(getActivity());
        rxPermissions = new RxPermissions(this);
        tv_user_name.setText(getUserName());

        if (!TextUtils.isEmpty(getHeadPath())) {
            Glide.with(getContext()).load(getHeadPath()).thumbnail(0.5f).into(iv_head);
        }
    }

    public String getUserName() {
        SharedPreferences shared = getActivity().getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        return shared.getString("username", "");
    }

    @OnClick({R.id.ly_back_login, R.id.ly_set_head})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ly_back_login:
                promptDialog.showLoading(getResources().getString(R.string.Are_logged_out));
                EMClient.getInstance().logout(true, mEMCallBackAdapter);
                break;
            case R.id.ly_set_head:
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(aBoolean -> {
                    if (aBoolean) {
                        RxPhotoTool.openLocalImage(this);
                    }
                });
                break;
        }
    }

    private EMCallBackAdapter mEMCallBackAdapter = new EMCallBackAdapter() {

        @Override
        public void onSuccess() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                    ToastUtils.showShort(getContext(), R.string.check_result_logout_success);
                    startActivity(LoginActivity.class, true);
                    promptDialog.dismiss();
                }
            });
        }

        @Override
        public void onError(int i, String s) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                    ToastUtils.showShort(getContext(), R.string.check_result_logout_fail);
                    promptDialog.dismiss();
                }
            });
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case GET_IMAGE_FROM_PHONE://图库

                    if (null != data.getData()) {

                        File files = new File(RxPhotoTool.getImageAbsolutePath(getContext(), data.getData()));

                        Luban.with(getContext()).load(files).setTargetDir(RxFileTool.getSDCardPath()).filter(path -> !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"))).setCompressListener(new OnCompressListener() {
                            @Override
                            public void onStart() {
                                promptDialog.showLoading(getResources().getString(R.string.please_later));
                            }

                            @Override
                            public void onSuccess(File file) {

                                if (file.exists()) {
                                    String photo_path01 = file.getPath();
                                    headUrl(photo_path01);
                                    Glide.with(getContext()).load(photo_path01).thumbnail(0.5f).into(iv_head);
                                }
                                promptDialog.dismiss();
                            }

                            @Override
                            public void onError(Throwable e) {
                                promptDialog.dismiss();
//                                RxToast.error(e.getMessage());
                            }
                        }).launch();
                    }
                    break;
            }
        }
    }

    public void headUrl(String path) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        editors = sharedPreferences.edit();
        editors.putString("headpath", path);
        editors.commit();//提交
    }

    public String getHeadPath() {
        SharedPreferences shared = MainActivity.activity.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        return shared.getString("headpath", "");
    }

}
