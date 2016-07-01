package com.dym.film.activity.filmreview;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.search.SearchActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.AllCriticRecyclerAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */
@Deprecated
public class AllCriticActivity extends BaseViewCtrlActivity
{
    public final static String TAG = "AllFRVCtrl";

    private AllCriticViewController mViewController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mViewController = new AllCriticViewController();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    public class AllCriticViewController extends BaseContentViewController
    {
        private final static int PAGE_LIMIT_NUM = 20;

        private NetworkManager mNetworkManager = NetworkManager.getInstance();

//        private AllCriticDataManager mDataManager = new AllCriticDataManager();

        /**
         * 标题
         */
        private TextView mTitleTextView = null;

        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        /**
         * 列表
         */
        private LoadMoreRecyclerView mRecyclerView = null;
        private ExRcvAdapterWrapper mAdapterWrapper = null;
        private AllCriticRecyclerAdapter mCriticAdapter = null;

        /**
         * 当前影评的页
         */
        private int mCurrentPageIndex = 0;
        private boolean mIsRefreshingOrLoadMore = false;
        protected HttpRespCallback<NetworkManager.RespGetCritic> mGetCriticCallback = new HttpRespCallback<NetworkManager.RespGetCritic>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                endRefresh(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespGetCritic resp = (NetworkManager.RespGetCritic) msg.obj;

                mTitleTextView.setText(String.valueOf("影评人 " + "(" + String.valueOf(resp.critics.sum) + ")"));

                int size = 0;
                if (resp.critics != null &&
                        resp.critics.list != null && !resp.critics.list.isEmpty()) {
                    size = resp.critics.list.size();
                    mCriticAdapter.setAll(resp.critics.list);

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
        protected HttpRespCallback<NetworkManager.RespGetCritic> mLoadMoreCriticCallback = new HttpRespCallback<NetworkManager.RespGetCritic>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespGetCritic resp = (NetworkManager.RespGetCritic) msg.obj;

                ArrayList<NetworkManager.CriticRespModel> list = resp.critics.list;

                mCriticAdapter.appendAll(list, false);
                if (list == null || list.size() < PAGE_LIMIT_NUM) {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                }
                else {
                    mCurrentPageIndex += 1;
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                }
            }
        };
        /**
         * 加载更多回调
         */
        protected LoadMoreRecyclerView.LoadMoreListener mLoadMoreListener = new LoadMoreRecyclerView.LoadMoreListener()
        {
            @Override
            public void onNeedLoadMore()
            {
                //Loge(TAG, "Can Load More...");
                loadMore();
            }
        };

        public AllCriticViewController()
        {
            super(true);
            initialize();
        }

        protected void initialize()
        {
            setFinishView(R.id.backButtonImage);

            mTitleTextView = (TextView) findViewById(R.id.filmReviewerTitle);

            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
            mSwipeRefreshLayout.setDistanceToTriggerSync(CommonManager.DEFAULT_REFRESH_DISTANCE);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    refresh();
                }
            });

            mCriticAdapter = new AllCriticRecyclerAdapter(mActivity);
            LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
            mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.allFilmReviewerList);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setLinearLayoutManager(layoutManager);
            mRecyclerView.setLoadMoreListener(mLoadMoreListener);
            mAdapterWrapper = new ExRcvAdapterWrapper<>(mCriticAdapter, layoutManager);
            mRecyclerView.setAdapter(mAdapterWrapper);

            CommonManager.setRefreshingState(mSwipeRefreshLayout, true);
            refresh();


            findViewById(R.id.fragment_filmAuthor_search).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent it = new Intent(mActivity, SearchActivity.class);
                    it.putExtra(SearchActivity.SearchTYPE, SearchActivity.AUTHOR);
                    mActivity.startActivity(it);
                }
            });
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
            mNetworkManager.getCriticList(0, PAGE_LIMIT_NUM, mGetCriticCallback);
        }

        private void endRefresh(int state)
        {
            mIsRefreshingOrLoadMore = false;
            CommonManager.setRefreshingState(mSwipeRefreshLayout, false);

            switch (state) {
                case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                    mRecyclerView.setFooterClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (mIsRefreshingOrLoadMore) {
                                return;
                            }
                            CommonManager.setRefreshingState(mSwipeRefreshLayout, true);
                        }
                    });
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                    break;

                case LoadMoreRecyclerView.LOAD_MORE_NO_MORE:
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    break;

                default:
                    mRecyclerView.setFooterClickListener(null);
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    break;
            }
        }

        private synchronized void loadMore()
        {
            if (mIsRefreshingOrLoadMore) {
                return;
            }
            mIsRefreshingOrLoadMore = true;

            mNetworkManager.getCriticList(mCurrentPageIndex + 1, PAGE_LIMIT_NUM, mLoadMoreCriticCallback);
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
                            mRecyclerView.startLoadMore();
                        }
                    });
                    break;

                default:
                    mRecyclerView.setFooterClickListener(null);
                    break;
            }
            mRecyclerView.loadMoreFinished(state);
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_all_critic;
        }
    }

}
