package com.chenxiao.forever.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.chenxiao.forever.adapter.ConversationAdapter;
import com.chenxiao.forever.adapter.EMMessageListenerAdapter;
import com.chenxiao.forever.forever.R;
import com.chenxiao.forever.view.ConversationPresenter;
import com.chenxiao.forever.view.ConversationPresenterImpl;
import com.chenxiao.forever.view.ConversationView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

import java.util.List;

import butterknife.BindView;

public class HomePageFragment extends BaseFragment implements ConversationView {
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private ConversationAdapter mConversationAdapter;
    private ConversationPresenter mConversationPresenter;

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_homepage;
    }

    @Override
    protected void init() {
        super.init();
        mConversationPresenter = new ConversationPresenterImpl(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mConversationAdapter = new ConversationAdapter(getContext(), mConversationPresenter.getConversations());
        mRecyclerView.setAdapter(mConversationAdapter);

        mConversationPresenter.loadAllConversations();
        EMClient.getInstance().chatManager().addMessageListener(mEMMessageListenerAdapter);
    }

    private EMMessageListenerAdapter mEMMessageListenerAdapter = new EMMessageListenerAdapter() {

        @Override
        public void onMessageReceived(List<EMMessage> list) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    mConversationPresenter.loadAllConversations();
                    mRecyclerView.scrollToPosition(0);
                }
            });
        }
    };


    @Override
    public void onAllConversationsLoaded() {
//        toast(getString(R.string.load_conversations_success));
        mConversationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        mConversationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(mEMMessageListenerAdapter);
    }

}
