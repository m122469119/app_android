package com.dym.film.controllers;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.dym.film.R;
import com.dym.film.activity.filmreview.AllCriticActivity;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.activity.search.SearchActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.adapter.AllFilmReviewRecyclerAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.ui.CircleImageView;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

/**
 * 控制影评页的view
 */
@Deprecated
public class FilmReviewViewController extends BaseViewController
{
    public final static String TAG = "FilmReviewCtrl";

    private NetworkManager mNetworkManager = NetworkManager.getInstance();

    private final static int PAGE_LIMIT_NUM = 20;
    private final static int LOOP_TIME = 3000;

    /**
     * 推荐影评人的banner
     */
    private ConvenientBanner mBannersLayout = null;
    private ArrayList<NetworkManager.BannerModel> mBanners = new ArrayList<>();


    /**
     * 总的影评人数
     */
    private TextView mSumFilmReviewerText = null;

    private View mFRLayout1 = null;
    private View mFRLayout2 = null;
    /**
     * 影评人的头像
     */
    private CircleImageView mFRAvatar1 = null;
    private CircleImageView mFRAvatar2 = null;

    /**
     * 影评人的名字
     */
    private TextView mFRName1 = null;
    private TextView mFRName2 = null;


    /**
     * 影评人的身份
     */
    private TextView mFRInfo1 = null;
    private TextView mFRInfo2 = null;

    /**
     * 影评列表
     */
    private SwipeRefreshLayout mRefreshLayout = null;
    private LoadMoreRecyclerView mRecyclerView = null;
    private AllFilmReviewRecyclerAdapter mFilmReviewAdapter = null;
    private ExRcvAdapterWrapper mAdapterWrapper = null;

    /**
     * 当前影评的页
     */
    private int mCurrentPageIndex = 0;
    private boolean mIsRefreshingOrLoadMore = false;

    private ArrayList<NetworkManager.CriticRespModel> mCritics = new ArrayList<>();

    private ExceptionLayoutViewController mExcepController = null;

    public FilmReviewViewController(@NonNull BaseViewCtrlActivity activity, View view)
    {
        super(activity, view);

        initializeFilmReviewView();
    }

    public void onDestroy()
    {
    }

    private void initializeFilmReviewView()
    {
        /**
         * 初始化 Header
         */
        View header = View.inflate(mActivity, R.layout.layout_film_review_header, null);
        header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mBannersLayout = (ConvenientBanner) header.findViewById(R.id.filmReviewBanner);
        initializeBanner();

//        mSumFilmReviewerText = (TextView) header.findView(R.id.fileReviewerNumber);
//
//        mFRLayout1 = header.findView(R.id.filmReviewerLayout1);
//        mFRAvatar1 = (CircleImageView) header.findView(R.id.filmReviewerAvatar1);
//        mFRName1 = (TextView) header.findView(R.id.filmReviewerName1);
//        mFRInfo1 = (TextView) header.findView(R.id.filmReviewerInfo1);
//
//        mFRLayout2 = header.findView(R.id.filmReviewerLayout2);
//        mFRAvatar2 = (CircleImageView) header.findView(R.id.filmReviewerAvatar2);
//        mFRName2 = (TextView) header.findView(R.id.filmReviewerName2);
//        mFRInfo2 = (TextView) header.findView(R.id.filmReviewerInfo2);
//
//        header.findView(R.id.moreButton).setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                MatStatsUtil.eventClick(mActivity,"critics_more","critics_more");
//                startAllFilmReviewerActivity();
//            }
//        });

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mRefreshLayout.setDistanceToTriggerSync(CommonManager.DEFAULT_REFRESH_DISTANCE);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!refresh()) {
                    CommonManager.setRefreshingState(mRefreshLayout, false);
                }
            }
        });

        /**
         * 初始化RecyclerView
         */
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mFilmReviewAdapter = new AllFilmReviewRecyclerAdapter(mActivity);

        mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.filmReviewList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLinearLayoutManager(mLayoutManager);
        mRecyclerView.setLoadMoreListener(mLoadMoreListener);

        mAdapterWrapper = new ExRcvAdapterWrapper<>(mFilmReviewAdapter, mLayoutManager);
        mAdapterWrapper.setHeaderView(header);

        mRecyclerView.setAdapter(mAdapterWrapper);

        /**
         * 开始请求数据
         */
        mExcepController = new ExceptionLayoutViewController(mActivity, new ExceptionLayoutViewController.ViewCallback()
        {
            @Override
            public void onExceptionViewClicked()
            {
                mExcepController.progress();
                refresh();
            }
        }, findViewById(R.id.exceptionPage));

        mExcepController.progress();
        findViewById(R.id.contentLoadingLayout).setVisibility(View.INVISIBLE);

        refresh();

        findViewById(R.id.fragment_filmReview_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it=new Intent(mActivity,SearchActivity.class);
                it.putExtra(SearchActivity.SearchTYPE, SearchActivity.REVIEW);
                mActivity.startActivity(it);
            }
        });

    }

    private synchronized void initializeBanner()
    {
        int height = CommonManager.DISPLAY_METRICS.widthPixels / 3;
        mBannersLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
        mBannersLayout.setPages(new CBViewHolderCreator<BannerViewHolder>()
        {
            @Override
            public BannerViewHolder createHolder()
            {
                return new BannerViewHolder();
            }
        }, mBanners).setPageIndicator(new int[]{R.drawable.icon_dot_white_normal, R.drawable.icon_dot_dark_pressed});

        mBannersLayout.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if (mBanners.size() <= 1) {
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mBannersLayout.stopTurning();
                        break;

                    case MotionEvent.ACTION_UP:
                        mBannersLayout.startTurning(LOOP_TIME);
                        break;
                }
                return false;
            }
        });
    }

    private class BannerViewHolder implements Holder<NetworkManager.BannerModel>
    {
        private ImageView mImageView = null;

        @Override
        public View createView(Context context)
        {
            mImageView = new ImageView(context);
            mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return mImageView;
        }

        @Override
        public void UpdateUI(final Context context, int position, final NetworkManager.BannerModel data)
        {
            String url = QCloudManager.urlImage2(data.img, CommonManager.dpToPx(400));
            CommonManager.displayImage(url, mImageView, R.drawable.ic_default_loading_img);
            mImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    CommonManager.processBannerClick(context, data.url);
                }
            });
        }
    }

    public synchronized boolean refresh()
    {
        if (mIsRefreshingOrLoadMore) {
            return false;
        }
        mIsRefreshingOrLoadMore = true;

        mNetworkManager.getBanners(2, new HttpRespCallback<NetworkManager.RespGetBanner>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespGetBanner resp = (NetworkManager.RespGetBanner) msg.obj;

                mBanners.clear();

                if (resp.banners != null) {
                    mBanners.addAll(resp.banners);
                }

                if (mBanners.size() <= 1) {
                    mBannersLayout.stopTurning();
                    mBannersLayout.setPageIndicator(new int[]{android.R.color.transparent, android.R.color.transparent});
                    mBannersLayout.setManualPageable(false);
                }
                else {
                    mBannersLayout.startTurning(LOOP_TIME);
                    mBannersLayout.setPageIndicator(new int[]{R.drawable.icon_dot_white_normal, R.drawable.icon_dot_dark_pressed});
                    mBannersLayout.setManualPageable(true);
                }

                mBannersLayout.notifyDataSetChanged();
            }
        });

        mNetworkManager.getCriticList(0, 2, new HttpRespCallback<NetworkManager.RespGetCritic>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //Loge(TAG, "Code: " + code + " Msg: " + msg);
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.RespGetCritic critic = (NetworkManager.RespGetCritic) msg.obj;
                mCritics.clear();
                if (critic == null || critic.critics == null) {
                    return;
                }
                mCritics.addAll(critic.critics.list);

                // 设置总数
                mSumFilmReviewerText.setText(String.valueOf(critic.critics.sum));

                LogUtils.e(TAG, "Size: " + mCritics.size());

                if (mCritics.size() > 0) {
                    final NetworkManager.CriticRespModel model = mCritics.get(0);

                    CommonManager.displayAvatar(model.avatar, mFRAvatar1);

                    mFRInfo1.setText(model.title);
                    mFRName1.setText(model.name);

                    mFRLayout1.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MatStatsUtil.eventClick(mActivity, "critics_recommend", "critics_recommend");
                            CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, model);
                            startCriticDetailActivity();
                        }
                    });
                }

                if (mCritics.size() > 1) {
                    final NetworkManager.CriticRespModel model = mCritics.get(1);

                    CommonManager.displayAvatar(model.avatar, mFRAvatar2);

                    mFRInfo2.setText(model.title);
                    mFRName2.setText(model.name);

                    mFRLayout2.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            MatStatsUtil.eventClick(mActivity, "critics_recommend", "critics_recommend");
                            CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, model);
                            startCriticDetailActivity();
                        }
                    });
                }
            }
        });

        mFilmReviewAdapter.refreshBanner();
        /**
         * 请求数更新据
         */
        mCurrentPageIndex = 0;
        mNetworkManager.getFilmReviewList(mCurrentPageIndex, PAGE_LIMIT_NUM, mGetFilmReviewCallback);

        return true;
    }

    private void endRefresh(int state)
    {
        mIsRefreshingOrLoadMore = false;
        CommonManager.setRefreshingState(mRefreshLayout, false);

        switch (state) {
            case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                if (mFilmReviewAdapter.getItemCount() == 0) {
                    mExcepController.show();
                    findViewById(R.id.contentLoadingLayout).setVisibility(View.INVISIBLE);

                }

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

                        CommonManager.setRefreshingState(mRefreshLayout, true);
                    }
                });
                break;

            case LoadMoreRecyclerView.LOAD_MORE_NO_MORE:
                mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                break;

            default:
                mExcepController.hide();
                findViewById(R.id.contentLoadingLayout).setVisibility(View.VISIBLE);

                mRecyclerView.setFooterClickListener(null);
                mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                break;
        }
    }

    private synchronized boolean loadMore()
    {
        if (mIsRefreshingOrLoadMore) {
            return false;
        }
        mIsRefreshingOrLoadMore = true;

        mNetworkManager.getFilmReviewList(mCurrentPageIndex + 1, PAGE_LIMIT_NUM, mLoadMoreFilmReviewCallback);

        return true;
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

    /**
     * 启动影评人详情界面
     */
    private void startCriticDetailActivity()
    {
        Intent intent = new Intent(mActivity, CriticDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mActivity.startActivity(intent);
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
            if (!loadMore()) {
                mRecyclerView.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
            }
        }
    };


    protected HttpRespCallback<NetworkManager.RespGetFilmReview>
            mLoadMoreFilmReviewCallback = new HttpRespCallback<NetworkManager.RespGetFilmReview>()
    {
        @Override
        public void onRespFailure(int code, String msg)
        {
            //Loge(TAG, "Code: " + code + " Msg: " + msg);
            endLoadMore(LoadMoreRecyclerView.LOAD_MORE_FAILED);
        }

        @Override
        public void runOnMainThread(Message msg)
        {
            NetworkManager.RespGetFilmReview review = (NetworkManager.RespGetFilmReview) msg.obj;
            if (review != null) {
                int size = (review.cinecisms == null ? 0 : review.cinecisms.size());
                mFilmReviewAdapter.appendAll(review.cinecisms, false);
                mCurrentPageIndex += 1;

                //Loge(TAG, "Size: " + size);
                if (size < PAGE_LIMIT_NUM) {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                }
                else {
                    endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                }
            }
            else {
                endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
            }
        }
    };

    protected HttpRespCallback<NetworkManager.RespGetFilmReview>
            mGetFilmReviewCallback = new HttpRespCallback<NetworkManager.RespGetFilmReview>()
    {
        @Override
        public void onRespFailure(int code, String msg)
        {
            endRefresh(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            MixUtils.toastShort(mActivity, "刷新失败");
        }

        @Override
        public void runOnMainThread(Message msg)
        {
            NetworkManager.RespGetFilmReview review = (NetworkManager.RespGetFilmReview) msg.obj;
            int size = (review.cinecisms == null ? 0 : review.cinecisms.size());

            mFilmReviewAdapter.setAll(review.cinecisms);
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

    /**
     * 启动全部影评人界面
     */
    private void startAllFilmReviewerActivity()
    {
        Intent intent = new Intent(mActivity, AllCriticActivity.class);

        mActivity.startActivity(intent);
    }

}
