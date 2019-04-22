package com.chenxiao.forever.Activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chenxiao.forever.Util.ToastUtils;
import com.chenxiao.forever.adapter.EMMessageListenerAdapter;
import com.chenxiao.forever.adapter.MessageListAdapter;
import com.chenxiao.forever.adapter.TextWatcherAdapter;
import com.chenxiao.forever.app.Constant;
import com.chenxiao.forever.forever.R;
import com.chenxiao.forever.utils.ThreadUtils;
import com.chenxiao.forever.view.ChatPresenter;
import com.chenxiao.forever.view.ChatPresenterImpl;
import com.chenxiao.forever.view.ChatView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class ChatActivity extends BaseActivity implements ChatView {
    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.edit)
    EditText mEdit;
    @BindView(R.id.send)
    TextView mSend;

    private ChatPresenter mChatPresenter;
    private String mUserName;
    private MessageListAdapter mMessageListAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chat);
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        //设置系统栏颜色
        ImageView iv_system = (ImageView) findViewById(R.id.iv_system);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv_system.getLayoutParams();
        params.height = (int) getStatusBarHeight(this);//设置当前控件布局的高度

        mChatPresenter = new ChatPresenterImpl(this);
        mBack.setVisibility(View.VISIBLE);
        mUserName = getIntent().getStringExtra(Constant.Extra.USER_NAME);
        String title = String.format(getString(R.string.chat_with), mUserName);
        mTitle.setText(title);
        mEdit.setOnEditorActionListener(mOnEditorActionListener);
        mEdit.addTextChangedListener(mTextWatcher);

        initRecyclerView();

        EMClient.getInstance().chatManager().addMessageListener(mEMMessageListener);
        mChatPresenter.loadMessages(mUserName);
    }

    private void initRecyclerView() {
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageListAdapter = new MessageListAdapter(this, mChatPresenter.getMessages());
        mRecyclerView.setAdapter(mMessageListAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    @OnClick({R.id.back, R.id.send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.send:
                sendMessage();
                break;
        }
    }

    private void sendMessage() {
        mChatPresenter.sendMessage(mUserName, mEdit.getText().toString().trim());
        hideKeyBoard();
        mEdit.getText().clear();
    }

    private TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        }
    };

    private TextWatcherAdapter mTextWatcher = new TextWatcherAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            mSend.setEnabled(s.length() != 0);
        }
    };


    @Override
    public void onStartSendMessage() {
        updateList();
    }

    private void updateList() {
        mMessageListAdapter.notifyDataSetChanged();
        smoothScrollToBottom();
    }

    @Override
    public void onSendMessageSuccess() {
        hideProgress();
//        toast(getString(R.string.send_success));
        updateList();
    }

    @Override
    public void onSendMessageFailed() {
        hideProgress();
        ToastUtils.showShort(mContext, R.string.send_failed);
    }

    @Override
    public void onMessagesLoaded() {
//        toast(getString(R.string.load_data_success));
        mMessageListAdapter.notifyDataSetChanged();
        scrollToBottom();
    }

    @Override
    public void onMoreMessagesLoaded(int size) {
//        toast(getString(R.string.load_more_data_success));
        mMessageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(size);
    }

    @Override
    public void onNoMoreData() {
//        toast(getString(R.string.no_more_data));
    }


    private EMMessageListenerAdapter mEMMessageListener = new EMMessageListenerAdapter() {
        @Override
        public void onMessageReceived(final List<EMMessage> list) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final EMMessage emMessage = list.get(0);
                    if (emMessage.getUserName().equals(mUserName)) {
                        mChatPresenter.makeMessageRead(mUserName);
                        mMessageListAdapter.addNewMessage(emMessage);
                        smoothScrollToBottom();
                    }
                }
            });
        }
    };

    private void smoothScrollToBottom() {
        mRecyclerView.smoothScrollToPosition(mMessageListAdapter.getItemCount() - 1);
    }

    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(mMessageListAdapter.getItemCount() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(mEMMessageListener);
    }

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItemPosition == 0) {
                    mChatPresenter.loadMoreMessages(mUserName);
                }
            }
        }
    };
}
