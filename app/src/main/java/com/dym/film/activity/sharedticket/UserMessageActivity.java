package com.dym.film.activity.sharedticket;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/20
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.MessageRecyclerAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.UserMessage;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.receiver.JPushMessageReceiver;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.MixUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import cn.bingoogolapple.androidcommon.adapter.BGAOnItemChildClickListener;

/**
 * 推送给用户的消息页面
 */
public class UserMessageActivity extends BaseViewCtrlActivity
{

    private UserMessageViewController mController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        translucentStatusBar();

        mController = new UserMessageViewController();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onDestroy()
    {
        if (mController != null) {
            mController.onDestroy();
        }
        super.onDestroy();
    }

    public class UserMessageViewController extends BaseContentViewController
    {
        private final static int PAGE_LIMIT = 20;

        private SimpleBroadcastReceiver mBroadcastReceiver = new SimpleBroadcastReceiver();

        private SwipeRefreshLayout mSwipeLayout = null;

        private LoadMoreRecyclerView mRecyclerView = null;
        private ExRcvAdapterWrapper mAdapterWrapper = null;
        //    private UserMessageRecyclerAdapter mMessageAdapter = null;
        private MessageRecyclerAdapter mAdapter = null;

        private int mCurrentPage = 0;
        private boolean mIsRefreshingOrLoadingMore = false;

        public UserMessageViewController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_user_message;
        }

        protected void initialize()
        {
            // 注册广播
            IntentFilter filter = new IntentFilter();
            filter.addAction(JPushMessageReceiver.ACTION_MESSAGE_RECEIVED);
            mActivity.registerReceiver(mBroadcastReceiver, filter);

            // 初始化title
            findViewById(R.id.backButtonImage).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });

            mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
            mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    //loadMore();
                }
            });


            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);

            mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.messageRecyclerView);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setLinearLayoutManager(layoutManager);
            mAdapter = new MessageRecyclerAdapter(mRecyclerView);
            mAdapter.setOnItemChildClickListener(new BGAOnItemChildClickListener()
            {
                private boolean mIsRemoving = false;
                @Override
                public synchronized void onItemChildClick(ViewGroup viewGroup, View view,final int position)
                {

                    if (position < 0 || position >= mAdapter.getItemCount()) {
                        return;
                    }
                    switch (view.getId()) {
                        case R.id.trashLayout: {
                            if (mIsRemoving) {
                                return;
                            }
                            mIsRemoving = true;
                            MixUtils.showProgressDialog(mActivity, "", true);
                            UserMessage message = mAdapter.getItem(position);

                            NetworkManager.ReqDeleteMessage req = new NetworkManager.ReqDeleteMessage();
                            req.messageID = message.getMsgID();

                            NetworkManager.getInstance().deleteMessage(req, new HttpRespCallback<NetworkManager.RespDeleteMessage>()
                            {
                                @Override
                                public void onRespFailure(int code, String msg)
                                {
                                    mIsRemoving = false;
                                    MixUtils.dismissProgressDialog();
                                    Toast.makeText(mActivity, "删除失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                protected void runOnMainThread(Message msg)
                                {
                                    mIsRemoving = false;
                                    MixUtils.dismissProgressDialog();
                                    mAdapter.removeItem(position);
                                }
                            });

                            break;
                        }
                        case R.id.mainLayout: {
                            /**
                             * 打开消息详情
                             */
                            final UserMessage message = mAdapter.getItem(position);

                            // 标记为已读
                            if (message.getReaded() != 1) {
                                NetworkManager.ReqReadMessage req = new NetworkManager.ReqReadMessage();
                                req.msgID = message.getMsgID();
                                NetworkManager.getInstance().readedMessage(req, new HttpRespCallback<NetworkManager.RespReadMessage>()
                                {
                                    @Override
                                    public void onRespFailure(int code, String msg)
                                    {
                                        //
                                    }

                                    @Override
                                    public void runOnMainThread(Message msg)
                                    {
                                        message.setReaded(1);
                                        mAdapter.notifyItemChanged(position);
                                    }
                                });
                            }

                            JPushMessageReceiver.processNotificationOpen(UserMessageActivity.this, message);
                            break;
                        }
                    }
                }
            });

            mAdapterWrapper = new ExRcvAdapterWrapper<>(mAdapter, mRecyclerView.getLayoutManager());
            mRecyclerView.setAdapter(mAdapterWrapper);

            mRecyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener()
            {
                @Override
                public void onNeedLoadMore()
                {
                    loadMore();
                }
            });

            CommonManager.setRefreshingState(mSwipeLayout, true);
            loadMore();
        }

        private synchronized void loadMore()
        {
            if (mIsRefreshingOrLoadingMore) {
                return;
            }
            mIsRefreshingOrLoadingMore = true;


            /**
             * 从服务器获取消息
             */
            NetworkManager.getInstance().getUserMessageList(mCurrentPage, PAGE_LIMIT, new HttpRespCallback<NetworkManager.RespUserMessage>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespUserMessage resp = (NetworkManager.RespUserMessage) msg.obj;
                    if (resp.messages != null) {
                        for (NetworkManager.UserMessageModel rmsg : resp.messages) {
                            UserMessage message = rmsg.toUserMessage();
                            mAdapter.addLastItem(message);
                        }
                    }

                    int size = resp.messages == null ? 0 : resp.messages.size();
                    if (size < PAGE_LIMIT) {
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    }
                    else {
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                }
            });
        }

        private void endLoadMore(int state)
        {
            mIsRefreshingOrLoadingMore = false;
            CommonManager.setRefreshingState(mSwipeLayout, false);
            mSwipeLayout.setEnabled(false);

            mRecyclerView.loadMoreFinished(state);
            switch (state) {
                case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                    mRecyclerView.setFooterClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            loadMore();
                        }
                    });
                    break;

                case LoadMoreRecyclerView.LOAD_MORE_NO_MORE:
                    if (mCurrentPage == 0 && mAdapter.getItemCount() > 0) {
                        mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());
                    }
                    break;

                case LoadMoreRecyclerView.LOAD_MORE_SUCCESS:
                    if (mCurrentPage == 0) {
                        mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());
                    }
                    mCurrentPage += 1;
                    break;
            }
            checkMessageCount();
        }

        private void checkMessageCount()
        {
            if (mAdapter.getItemCount() == 0) {
                findViewById(R.id.noMessageText).setVisibility(View.VISIBLE);
            }
            else {
                findViewById(R.id.noMessageText).setVisibility(View.INVISIBLE);
            }
        }

        public void onDestroy()
        {
            mActivity.unregisterReceiver(mBroadcastReceiver);
        }


        /**
         * 监听消息
         */
        private class SimpleBroadcastReceiver extends BroadcastReceiver
        {

            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (JPushMessageReceiver.ACTION_MESSAGE_RECEIVED.equals(action)) {
                    UserMessage message = (UserMessage) intent.getSerializableExtra(JPushMessageReceiver.KEY_USER_MESSAGE);

                    if (message != null) {
                        mAdapter.addItem(0, message);
                    }
                }
            }
        }

    }
}
