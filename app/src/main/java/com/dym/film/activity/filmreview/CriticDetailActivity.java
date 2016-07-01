package com.dym.film.activity.filmreview;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.CriticFilmReviewAdapter;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

public class CriticDetailActivity extends BaseViewCtrlActivity
{
    public final static String TAG = "CriticDetailVC";

    public final static String KEY_CRITIC_DATA = "criticID";
    private CriticDetailViewController mController = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        translucentStatusBar();

        mController = new CriticDetailViewController();
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);

        mController = new CriticDetailViewController();
    }

    public class CriticDetailViewController extends BaseContentViewController
    {
        /**
         * 数据
         */
        private int intCurrentPage = 0;
        private final static int PAGE_SIZE = 20;

        private NetworkManager mNetworkManager = NetworkManager.getInstance();

        private NetworkManager.CriticRespModel mCritic = null;

        private NetworkManager.CriticDetailRespModel mCriticDetail = null;

        private boolean mIsRefreshingOrLoadingMore = false;

        /**
         * Recycler View 相关
         */
        private LoadMoreRecyclerView mRecyclerView;

        private CriticFilmReviewAdapter mRecyclerAdapter;

        private ExRcvAdapterWrapper mAdapterWrapper;

        private LinearLayoutManager mLinearLayoutManager = null;

        /**
         * Header 相关
         */
        private View mHeaderView = null;

        private TextView mCriticNameText = null;

        private ImageView mCriticAvatar = null;

        private TextView mCriticSummary = null;

        /**
         * Title Bar 相关
         */
        private ImageView mFollowBtn = null;

        private TextView mCriticTitleText = null;


        private View mCustomStatusBar = null;

        public CriticDetailViewController()
        {
            super(true);
            initialize();
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_critic_detail;
        }

        protected void initialize()
        {
            setFinishView(R.id.backImageBtn);
            setOnClickListener(R.id.criticTitle);

            mCritic = (NetworkManager.CriticRespModel) CommonManager.getData(CriticDetailActivity.KEY_CRITIC_DATA);
            if (mCritic == null) {
                finish();
                return;
            }

            /**
             * 设置 title
             */
            mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.criticRecyclerView);
            mCriticTitleText = (TextView) findViewById(R.id.criticTitleText);
            mFollowBtn = (ImageView) findViewById(R.id.criticFollowBtn);
            mCustomStatusBar = findViewById(R.id.customStatusBarView);
            mCustomStatusBar.setAlpha(0);
            mCriticTitleText.setAlpha(0);

            mHeaderView = View.inflate(mActivity, R.layout.layout_critic_detail_header, null);
            mHeaderView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            /**
             * 设置 recycler view
             */
            mLinearLayoutManager = new LinearLayoutManager(mActivity);
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            mRecyclerView.setLinearLayoutManager(mLinearLayoutManager);

            // 设置ItemAnimator
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerAdapter = new CriticFilmReviewAdapter(mActivity, true, false);
            mAdapterWrapper = new ExRcvAdapterWrapper<>(mRecyclerAdapter, mLinearLayoutManager);
            mRecyclerView.setAdapter(mAdapterWrapper);
            mAdapterWrapper.setHeaderView(mHeaderView);

            // 设置固定大小
            mRecyclerView.setHasFixedSize(true);

            mRecyclerView.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener()
            {
                @Override
                public void onNeedLoadMore()
                {
                    loadMore();
                }
            });

            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {
                    super.onScrolled(recyclerView, dx, dy);
                    int scrollY = getScrollY();

                    float alpha = 0;
                    if (scrollY > 0) {
                        alpha = (float) ((scrollY - 200) / 200.00);
                    }
                    else {
                        alpha = 0;
                    }

                    if (!mHeaderView.isShown()) {
                        alpha = 1;
                    }

                    mCriticTitleText.setAlpha(alpha);
                    mCustomStatusBar.setAlpha(alpha);
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState)
                {
                    super.onScrollStateChanged(recyclerView, newState);
                }
            });

            refresh();
        }


        private synchronized void refresh()
        {
            if (mIsRefreshingOrLoadingMore) {
                return;
            }
            mIsRefreshingOrLoadingMore = true;

            findViewById(R.id.loadingProgress).setAlpha(1f);
            loadCriticDetail(mCritic.name, mCritic.avatar, mCritic.title, false);
            /**
             * 获取影评人详情
             */
            mNetworkManager.getCriticDetail(mCritic.criticID, new HttpRespCallback<NetworkManager.RespGetCriticDetail>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    //
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespGetCriticDetail resp = (NetworkManager.RespGetCriticDetail) msg.obj;
                    mCriticDetail = resp.critic;
                    if (mCriticDetail != null) {
                        loadCriticDetail(mCriticDetail.name, mCriticDetail.avatar, mCriticDetail.summary, mCriticDetail.followed == 1);
                    }
                }
            });

            /**
             * 获取影评人的影评数据
             */
            intCurrentPage = 0;
            mNetworkManager.getCriticFilmReviewList(mCritic.criticID, intCurrentPage, PAGE_SIZE, new HttpRespCallback<NetworkManager.RespGetFilmReview>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());
                    endRefresh(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                }

                @Override
                protected void runOnMainThread(Message msg)
                {
                    super.runOnMainThread(msg);
                    NetworkManager.RespGetFilmReview ms = (NetworkManager.RespGetFilmReview) msg.obj;
                    mRecyclerAdapter.setAll(ms.cinecisms);

                    mAdapterWrapper.setFooterView(mRecyclerView.getLoadMoreFooterController().getFooterView());

                    if (ms.cinecisms == null || ms.cinecisms.size() < PAGE_SIZE) {
                        endRefresh(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    }
                    else {
                        endRefresh(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                }
            });
        }

        private void endRefresh(int state)
        {
            mIsRefreshingOrLoadingMore = false;

            final View progress = findViewById(R.id.loadingProgress);
            ValueAnimator alpha = ValueAnimator.ofFloat(1, 0);
            alpha.setDuration(200);
            alpha.setInterpolator(new LinearInterpolator());
            alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator)
                {
                    float value = (float) valueAnimator.getAnimatedValue();
                    progress.setAlpha(value);
                }
            });
            alpha.start();

            switch (state) {
                case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                    mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                    if (!NetWorkUtils.isAvailable(mActivity)) {
                        Toast.makeText(mActivity, "没有网络", Toast.LENGTH_LONG).show();
                    }
                    mRecyclerView.setFooterClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (!NetWorkUtils.isAvailable(mActivity)) {
                                Toast.makeText(mActivity, "没有网络", Toast.LENGTH_LONG).show();
                                return;
                            }
                            refresh();
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
        }


        private synchronized void loadMore()
        {
            if (mIsRefreshingOrLoadingMore) {
                return;
            }
            mIsRefreshingOrLoadingMore = true;

            mNetworkManager.getCriticFilmReviewList(mCritic.criticID, intCurrentPage + 1, PAGE_SIZE, new HttpRespCallback<NetworkManager.RespGetFilmReview>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
                }

                @Override
                protected void runOnMainThread(Message msg)
                {
                    super.runOnMainThread(msg);
                    NetworkManager.RespGetFilmReview ms = (NetworkManager.RespGetFilmReview) msg.obj;

                    mRecyclerAdapter.appendAll(ms.cinecisms, true);
                    if (ms.cinecisms == null || ms.cinecisms.size() < PAGE_SIZE) {
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    }
                    else {
                        intCurrentPage += 1;
                        endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                }
            });
        }


        private void endLoadMore(int state)
        {
            mIsRefreshingOrLoadingMore = false;

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


        private void loadCriticDetail(String name, String avatar, String summary, boolean followed)
        {
            if (mCriticNameText == null) {
                mCriticNameText = (TextView) mHeaderView.findViewById(R.id.head_layout_comment_person_name);
            }
            mCriticNameText.setText(name);

            if (mCriticAvatar == null) {
                mCriticAvatar = (ImageView) mHeaderView.findViewById(R.id.head_layout_comment_iv_photo);
            }
            CommonManager.displayAvatar(avatar, mCriticAvatar);

            if (mCriticSummary == null) {
                mCriticSummary = (TextView) mHeaderView.findViewById(R.id.head_layout_comment_person_des);
            }
            mCriticSummary.setText(summary);

            mCriticTitleText.setText(mCritic.name);

            setupCriticFollowState(followed);
        }

        /**
         * 设置影评人的关注状态
         *
         * @param followed
         */
        private void setupCriticFollowState(boolean followed)
        {
            if (followed) {
                mFollowBtn.setImageResource(R.drawable.ic_follow_yes);
                mFollowBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // 取消关注
                        unFollowCritic();
                    }
                });
            }
            else {
                mFollowBtn.setImageResource(R.drawable.ic_follow_no);
                mFollowBtn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // 关注按钮
                        followCritic();
                    }
                });
            }
        }

        private void followCritic()
        {
            if (!UserInfo.isLogin) {
                CommonManager.startLoginActivity(mActivity);
                return;
            }

            long id = mCritic.criticID;
            if (mCriticDetail != null) {
                id = mCriticDetail.criticID;
            }

            NetworkManager.ReqFollowCritic req = new NetworkManager.ReqFollowCritic();
            req.criticID = id;

            mNetworkManager.followCritic(req, new HttpRespCallback<NetworkManager.RespFollowCritic>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    /**
                     * 如果在这个页面上进行了登录，返回后进行关注，
                     */
                    if (code == 27 && UserInfo.isLogin) {
                        if (mCriticDetail != null) {
                            mCriticDetail.followed = 1;
                        }

                        setupCriticFollowState(true);
                    }
                    Toast.makeText(mActivity, "关注失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    if (mCriticDetail != null) {
                        mCriticDetail.followed = 1;
                    }
                    setupCriticFollowState(true);
                }
            });
        }

        private void unFollowCritic()
        {
            long id = mCritic.criticID;
            if (mCriticDetail != null) {
                id = mCriticDetail.criticID;
            }

            NetworkManager.ReqUnFollowCritic req = new NetworkManager.ReqUnFollowCritic();
            req.criticID = id;

            mNetworkManager.unFollowCritic(req, new HttpRespCallback<NetworkManager.RespUnFollowCritic>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    Toast.makeText(mActivity, "取消关注失败", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    if (mCriticDetail != null) {
                        mCriticDetail.followed = 0;
                    }
                    setupCriticFollowState(false);
                }
            });
        }

        public int getScrollY()
        {
            View header = mAdapterWrapper.getHeaderWrapperView();
            if (header != null) {
                return Math.abs(mLinearLayoutManager.getDecoratedTop(header));
            }
            return 0;
        }

        @Override
        protected void onViewClicked(@NonNull View view)
        {
            switch (view.getId()) {
                case R.id.criticTitleText:
                    mRecyclerView.smoothScrollToPosition(0);
                    break;
            }
        }
    }
}
