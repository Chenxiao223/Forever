package com.chenxiao.forever.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chenxiao.forever.Activity.MainActivity;
import com.chenxiao.forever.forever.R;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.util.DateUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SendMessageItemView extends RelativeLayout {

    @BindView(R.id.send_message)
    TextView mSendMessage;
    @BindView(R.id.send_message_progress)
    ImageView mSendMessageProgress;
    @BindView(R.id.timestamp)
    TextView mTimestamp;
    @BindView(R.id.avatar)
    CircleImageView avatar;

    public SendMessageItemView(Context context) {
        this(context, null);
    }

    public SendMessageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_send_message_item, this);
        ButterKnife.bind(this, this);
        if (!TextUtils.isEmpty(getHeadPath())) {
            Glide.with(getContext()).load(getHeadPath()).thumbnail(0.5f).into(avatar);
        }
    }

    public String getHeadPath() {
        SharedPreferences shared = MainActivity.activity.getSharedPreferences("logininfo", Context.MODE_PRIVATE);
        return shared.getString("headpath", "");
    }

    public void bindView(EMMessage emMessage, boolean showTimestamp) {
        updateTimestamp(emMessage, showTimestamp);
        updateMessageBody(emMessage);
        updateSendingStatus(emMessage);
    }

    private void updateTimestamp(EMMessage emMessage, boolean showTimestamp) {
        if (showTimestamp) {
            mTimestamp.setVisibility(VISIBLE);
            String time = DateUtils.getTimestampString(new Date(emMessage.getMsgTime()));
            mTimestamp.setText(time);
        } else {
            mTimestamp.setVisibility(GONE);
        }
    }

    private void updateMessageBody(EMMessage emMessage) {
        EMMessageBody body = emMessage.getBody();
        if (body instanceof EMTextMessageBody) {
            mSendMessage.setText(((EMTextMessageBody) body).getMessage());
        } else {
            mSendMessage.setText(getContext().getString(R.string.no_text_message));
        }
    }

    private void updateSendingStatus(EMMessage emMessage) {
        switch (emMessage.status()) {
            case INPROGRESS:
                mSendMessageProgress.setVisibility(VISIBLE);
                mSendMessageProgress.setImageResource(R.drawable.send_message_progress);
                AnimationDrawable drawable = (AnimationDrawable) mSendMessageProgress.getDrawable();
                drawable.start();
                break;
            case SUCCESS:
                mSendMessageProgress.setVisibility(GONE);
                break;
            case FAIL:
                mSendMessageProgress.setImageResource(R.mipmap.msg_error);
                break;
        }
    }
}
