package com.chenxiao.forever.app;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.app.TaskStackBuilder;

import com.chenxiao.forever.Activity.ChatActivity;
import com.chenxiao.forever.adapter.EMMessageListenerAdapter;
import com.chenxiao.forever.forever.R;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;

import java.util.List;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

public class ForeverApp extends Application {
    public static Context applicationContext;
    private int mDuanSound;
    private int mYuluSound;
    private SoundPool mSoundPool;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        initSoundPool();
        EMOptions options = new EMOptions();
        // 默认添加好友时，是不需要验证的，改成需要验证
        options.setAcceptInvitationAlways(false);
        // 是否自动将消息附件上传到环信服务器，默认为True是使用环信服务器上传下载，如果设为 false，需要开发者自己处理附件消息的上传和下载
//        options.setAutoTransferMessageAttachments(true);
        // 是否自动下载附件类消息的缩略图等，默认为 true 这里和上边这个参数相关联
//        options.setAutoDownloadThumbnail(true);
        //初始化
        EMClient.getInstance().init(applicationContext, options);
        //在做打包混淆时，关闭debug模式，避免消耗不必要的资源
        EMClient.getInstance().setDebugMode(true);
        EMClient.getInstance().chatManager().addMessageListener(mEMMessageListenerAdapter);

    }

    private void initSoundPool() {
        mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mDuanSound = mSoundPool.load(this, R.raw.duan, 1);
        mYuluSound = mSoundPool.load(this, R.raw.yulu, 1);
    }

    private EMMessageListenerAdapter mEMMessageListenerAdapter = new EMMessageListenerAdapter() {

        @Override
        public void onMessageReceived(List<EMMessage> list) {
            if (isForeground()) {
                mSoundPool.play(mDuanSound, 1, 1, 0, 0, 1);
            } else {
                mSoundPool.play(mYuluSound, 1, 1, 0, 0, 1);
                showNotification(list.get(0));
            }
        }
    };

    public boolean isForeground() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        if (runningAppProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo info :runningAppProcesses) {
            if (info.processName.equals(getPackageName()) && info.importance == IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    private void showNotification(EMMessage emMessage) {
        String contentText = "";
        if (emMessage.getBody() instanceof EMTextMessageBody) {
            contentText = ((EMTextMessageBody) emMessage.getBody()).getMessage();
        }

        Intent chat = new Intent(this, ChatActivity.class);
        chat.putExtra(Constant.Extra.USER_NAME, emMessage.getUserName());
        PendingIntent pendingIntent = TaskStackBuilder.create(this)
                .addParentStack(ChatActivity.class)
                .addNextIntent(chat)
                .getPendingIntent(1, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.head))
                .setSmallIcon(R.mipmap.head)
                .setContentTitle(getString(R.string.newnotify))
                .setContentText(contentText)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1, notification);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
