package com.dym.film.activity.sharedticket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.StaggerTagSharedTicketAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.ShareTicketDialogViewController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.data.TagSharedTicketDataManager;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

/**
 * 晒票详情页
 */
public class TagSharedTicketActivity extends BaseViewCtrlActivity
{

    public final static String KEY_TAG = "tag";

    private TagSharedTicketViewController mViewController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mViewController = new TagSharedTicketViewController();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mViewController.onResume();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mViewController.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case ShareTicketDialogViewController.REQUEST_CODE_PICK_IMAGE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(uri);

                        startActivity(intent);
                        return;
                    }
                }

                break;

            case ShareTicketDialogViewController.REQUEST_CODE_CAPTURE_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = ShareTicketDialogViewController.getCameraImageUri();
                    if (imageUri != null) {
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
                        Intent intent = new Intent(this, TicketShareActivity.class);
                        intent.setData(imageUri);

                        startActivity(intent);
                    }
                }

                ShareTicketDialogViewController.onActivityFinished();
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public class TagSharedTicketViewController extends BaseContentViewController
    {
        private String mFilmTag = "";

        /**
         * 控制变量
         */
        private int mCurrentPageIndex = 0;

        private final static int PAGE_LIMIT_NUM = 20;

        private boolean mIsRefreshingOrLoadMore = false;

        private NetworkManager mNetworkManager = NetworkManager.getInstance();

        private TagSharedTicketDataManager mDataManager = TagSharedTicketDataManager.mInstance;

        // 排名
        private TextView mTagRankingText = null;
        // 好评率
        private TextView mTagSupportRateText = null;
        // 点评人数
        private TextView mTagReviewerNumText = null;

        /**
         * 刷新的Layout
         */
        private SwipeRefreshLayout mRefreshLayout = null;

        /**
         * 瀑布流
         */
        private LoadMoreRecyclerView mRecyclerView = null;

        private StaggeredGridLayoutManager mStaggerLayoutManager = null;

        private StaggerTagSharedTicketAdapter mStaggerAdapter = null;

        private ExRcvAdapterWrapper mAdapterWrapper = null;

        /**
         * 晒票的选项view控制
         */
        private ShareTicketDialogViewController mSelectDialogController = null;

        public TagSharedTicketViewController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_tag_shared_ticket;
        }

        protected void initialize()
        {
            mFilmTag = getIntentString(TagSharedTicketActivity.KEY_TAG);
            if (mFilmTag == null) {
                // 如果数据异常，启动主页面
                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);

                finish();
                return;
            }

            // 初始化title
            setOnClickListener(R.id.backButtonImage);
            setOnClickListener(R.id.shareTicketButton);
            TextView titleText = (TextView) findViewById(R.id.sharedTicketDetailTitle);
            titleText.setText(mFilmTag);

            // 初始化 Footer
            // 初始化刷新，加载更多控件
            mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
            mRefreshLayout.setDistanceToTriggerSync(CommonManager.DEFAULT_REFRESH_DISTANCE);
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    refresh();
                }
            });

            // 初始化LoadMoreRecyclerView
            mStaggerLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
            mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.sharedTicketRecyclerView);
            mRecyclerView.setLayoutManager(mStaggerLayoutManager);
            mRecyclerView.setStaggerLayoutManager(mStaggerLayoutManager);
            mRecyclerView.setLoadMoreListener(mLoadMoreListener);

            // 初始化晒票列表
            mStaggerAdapter = new StaggerTagSharedTicketAdapter(mActivity, mDataManager);
            mAdapterWrapper = new ExRcvAdapterWrapper<>(mStaggerAdapter, mStaggerLayoutManager);

            mRecyclerView.setAdapter(mAdapterWrapper);

            // 初始化tag排名票房等信息
            View header = View.inflate(mActivity, R.layout.layout_tag_shared_ticket_header, null);
            header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mTagRankingText = (TextView) header.findViewById(R.id.ticketRankingText);
            mTagSupportRateText = (TextView) header.findViewById(R.id.supportRatioText);
            mTagReviewerNumText = (TextView) header.findViewById(R.id.reviewerNumText);

            /**
             * 如果设置头部就需要多加一个数据
             */
            mAdapterWrapper.setHeaderView(header);

            startQueryFilmBoxInfo();

            CommonManager.setRefreshingState(mRefreshLayout, true);
            refresh();
        }

        private void startQueryFilmBoxInfo()
        {
            mNetworkManager.getFilmBoxInfo(mFilmTag, new HttpRespCallback<NetworkManager.RespFilmBoxInfo>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                }

                @Override
                public void runOnMainThread(Message message)
                {
                    NetworkManager.RespFilmBoxInfo info = (NetworkManager.RespFilmBoxInfo) message.obj;
                    if (info != null && info.boxInfo != null) {
                        mTagRankingText.setText(String.valueOf(info.boxInfo.rank));
                        mTagSupportRateText.setText(String.valueOf(info.boxInfo.supportRatio));
                        mTagReviewerNumText.setText(String.valueOf(info.boxInfo.followNum));
                    }
                }
            });
        }

        public void onResume()
        {
            if (mDataManager.needRefreshAll()) {
                refresh();
            }
        }

        public void onDestroy()
        {
            TagSharedTicketDataManager.mInstance.clear();
        }

        private synchronized void refresh()
        {
            if (mIsRefreshingOrLoadMore) {
                return;
            }
            mIsRefreshingOrLoadMore = true;
            /**
             * 请求数更新据
             */
            mCurrentPageIndex = 0;
            mNetworkManager.getTagSharedTicketList(mCurrentPageIndex, PAGE_LIMIT_NUM, mFilmTag, mTicketRefreshCallback);
        }

        private void endRefresh(int state)
        {
            mDataManager.setNeedRefreshAll(false);
            mIsRefreshingOrLoadMore = false;
            CommonManager.setRefreshingState(mRefreshLayout, false);

            switch (state) {
                case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                    if (!NetWorkUtils.isAvailable(mActivity)) {
                        Toast.makeText(mActivity, "没有网络", Toast.LENGTH_LONG).show();
                    }

                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);

                    mRecyclerView.setFooterClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (!NetWorkUtils.isAvailable(mActivity)) {
                                Toast.makeText(mActivity, "没有网络", Toast.LENGTH_LONG).show();
                                return;
                            }

                            CommonManager.setRefreshingState(mRefreshLayout, true);
                        }
                    });
                    break;

                case LoadMoreRecyclerView.LOAD_MORE_NO_MORE:
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    break;

                default:
                    mRecyclerView.setFooterClickListener(null);
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    break;
            }

            /**
             * 滑动到顶部
             */
            mRecyclerView.scrollToPosition(0);
        }

        private synchronized void loadMore()
        {
            if (mIsRefreshingOrLoadMore) {
                return;
            }
            mIsRefreshingOrLoadMore = true;

            mNetworkManager.getTagSharedTicketList(mCurrentPageIndex + 1, PAGE_LIMIT_NUM, mFilmTag, mTicketLoadMoreCallback);
        }


        private void endLoadMore(int state)
        {
            mIsRefreshingOrLoadMore = false;

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

                default:
                    mRecyclerView.setFooterClickListener(null);
                    break;
            }
            mRecyclerView.loadMoreFinished(state);
        }

        /**
         * 加载更多回调
         */
        protected LoadMoreRecyclerView.LoadMoreListener
                mLoadMoreListener = new LoadMoreRecyclerView.LoadMoreListener()
        {
            @Override
            public void onNeedLoadMore()
            {
                //Loge(TAG, "Can Load More...");
                loadMore();
            }
        };

        @Override
        protected void onViewClicked(@NonNull View view)
        {
            switch (view.getId()) {
                case R.id.backButtonImage:
                    finish();
                    break;

                case R.id.shareTicketButton:
                    // 启动晒票
                    MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.TICKET_SHOW);
                    if (mSelectDialogController == null) {
                        mSelectDialogController = new ShareTicketDialogViewController(mActivity);
                    }
                    mSelectDialogController.show();
                    break;
            }
        }



        protected HttpRespCallback<NetworkManager.RespSharedTicketList>
                mTicketLoadMoreCallback = new HttpRespCallback<NetworkManager.RespSharedTicketList>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                MixUtils.toastShort(mActivity, "加载更多失败");
            }

            @Override
            public void onRespSuccess(NetworkManager.RespSharedTicketList model, String body)
            {
                sendMessage(WHAT_HTTP_SUCCESS, model);
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                switch (msg.what) {
                    case WHAT_HTTP_SUCCESS:
                        NetworkManager.RespSharedTicketList
                                list = (NetworkManager.RespSharedTicketList) msg.obj;

                        int size = (list.stubs == null ? 0 : list.stubs.size());
                        mStaggerAdapter.appendAll(list.stubs, true);
                        mCurrentPageIndex += 1;

                        if (size < PAGE_LIMIT_NUM) {
                            //Loge(TAG, "Size: " + size);
                            endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                        }
                        else {
                            endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                        }

                        break;
                }
            }
        };

        protected HttpRespCallback<NetworkManager.RespSharedTicketList>
                mTicketRefreshCallback = new HttpRespCallback<NetworkManager.RespSharedTicketList>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                endRefresh(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);

                MixUtils.toastShort(mActivity, "刷新失败");
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespSharedTicketList
                        list = (NetworkManager.RespSharedTicketList) msg.obj;
                int size = (list.stubs == null ? 0 : list.stubs.size());
                mStaggerAdapter.setAll(list.stubs);
                if (mAdapterWrapper.getFooterView() == null) {
                    mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());
                }

                if (size < PAGE_LIMIT_NUM) {
                    endRefresh(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                }
                else {
                    endRefresh(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                }
            }
        };
    }

}
