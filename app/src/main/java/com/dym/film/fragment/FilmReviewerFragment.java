package com.dym.film.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.adapter.CriticsRecyclerAdapter;
import com.dym.film.adapter.base.BaseListAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.ExceptionLayoutViewController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.ui.HorizontalListView;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/22
 */
public class FilmReviewerFragment extends Fragment
{
    private Activity mActivity = null;

    private NetworkManager mNetworkManager = NetworkManager.getInstance();

    private final static int PAGE_LIMIT_NUM = 20;
    private final static int LOOP_TIME = 3000;
    /**
     * 推荐影评人的banner
     */
    private ConvenientBanner mBannersLayout = null;
    private ArrayList<NetworkManager.BannerModel> mBanners = new ArrayList<>();

    private View mRecommendLayout = null;
    private HotReviewerAdapter mHotAdapter = null;

    /**
     * 影评列表
     */
    private SwipeRefreshLayout mRefreshLayout = null;
    private LoadMoreRecyclerView mRecyclerView = null;
    private CriticsRecyclerAdapter mCriticsAdapter = null;
    private ExRcvAdapterWrapper mAdapterWrapper = null;

    /**
     * 当前影评的页
     */
    private int mCurrentPageIndex = 0;
    private boolean mIsRefreshingOrLoadMore = false;

    private ArrayList<NetworkManager.CriticRespModel> mCritics = new ArrayList<>();

    private ExceptionLayoutViewController mExcepController = null;


    private ViewPager mParentViewPager = null;
    public void setViewPager(ViewPager pager)
    {
        mParentViewPager = pager;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_film_reviewer, container, false);
        initializeView(view);

        return view;
    }

    private void initializeView(View view)
    {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayout);
        mRefreshLayout.setColorSchemeColors(Color.BLACK);
        /**
         * 初始化 Header
         */
        View header = View.inflate(mActivity, R.layout.layout_film_review_header, null);
        header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mBannersLayout = (ConvenientBanner) header.findViewById(R.id.filmReviewBanner);
        initializeBanner();

        mRecommendLayout = header.findViewById(R.id.recommendLayout);
        HorizontalListView hList = (HorizontalListView) header.findViewById(R.id.hotReviewerList);
        mHotAdapter = new HotReviewerAdapter(mActivity);
        hList.setAdapter(mHotAdapter);
        hList.addConflictViews(mParentViewPager, mRefreshLayout);
        hList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, mHotAdapter.getItem(i));

                Intent intent = new Intent(mActivity, CriticDetailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                mActivity.startActivity(intent);
            }
        });
        mRecommendLayout.setVisibility(View.GONE);

        /**
         * 初始化RecyclerView
         */
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mCriticsAdapter = new CriticsRecyclerAdapter(mActivity);

        mRecyclerView = (LoadMoreRecyclerView) view.findViewById(R.id.filmReviewerList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLinearLayoutManager(mLayoutManager);
        mRecyclerView.setLoadMoreListener(mLoadMoreListener);

        mAdapterWrapper = new ExRcvAdapterWrapper<>(mCriticsAdapter, mLayoutManager);
        mAdapterWrapper.setHeaderView(header);

        mRecyclerView.setAdapter(mAdapterWrapper);

        /**
         * 设置刷新功能
         */
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
        mExcepController = new ExceptionLayoutViewController(mActivity, new ExceptionLayoutViewController.ViewCallback()
        {
            @Override
            public void onExceptionViewClicked()
            {
                mExcepController.progress();
                refresh();
            }
        }, view.findViewById(R.id.exceptionPage));

        mExcepController.progress();
        view.findViewById(R.id.contentLoadingLayout).setVisibility(View.INVISIBLE);

        /**
         * 开始请求数据
         */
        refresh();
    }

    private void setContentVisible(boolean visible)
    {
        View view = getView();
        if (view != null) {
            view.findViewById(R.id.contentLoadingLayout).setVisibility(visible?View.VISIBLE : View.INVISIBLE);
        }
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

        mNetworkManager.getRecommendCritic(new HttpRespCallback<NetworkManager.GetRecomCritics>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //Loge(TAG, "Code: " + code + " Msg: " + msg);
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                NetworkManager.GetRecomCritics critic = (NetworkManager.GetRecomCritics) msg.obj;
                mHotAdapter.setData(critic.critics);
                if (mHotAdapter.getCount() > 0) {
                    mRecommendLayout.setVisibility(View.VISIBLE);
                }
                else {
                    mRecommendLayout.setVisibility(View.GONE);
                }
            }
        });

        /**
         * 请求数更新据
         */
        mCurrentPageIndex = 0;
        mNetworkManager.getCriticList(0, PAGE_LIMIT_NUM, mGetCriticCallback);

        return true;
    }

    private void endRefresh(int state)
    {
        mIsRefreshingOrLoadMore = false;
        CommonManager.setRefreshingState(mRefreshLayout, false);

        switch (state) {
            case LoadMoreRecyclerView.LOAD_MORE_FAILED:
                if (mCriticsAdapter.getItemCount() == 0) {
                    mExcepController.show();
                    setContentVisible(false);
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
                setContentVisible(true);

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

        mNetworkManager.getCriticList(mCurrentPageIndex + 1, PAGE_LIMIT_NUM, mLoadMoreCriticCallback);

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

            int size = 0;
            if (resp.critics != null &&
                    resp.critics.list != null && !resp.critics.list.isEmpty()) {
                size = resp.critics.list.size();
                mCriticsAdapter.setAll(resp.critics.list);

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

            mCriticsAdapter.appendAll(list, false);
            if (list == null || list.size() < PAGE_LIMIT_NUM) {
                endLoadMore(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
            }
            else {
                mCurrentPageIndex += 1;
                endLoadMore(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
            }
        }
    };

    private class HotReviewerAdapter extends BaseListAdapter<NetworkManager.CriticRespModel>
    {

        protected HotReviewerAdapter(@NonNull Activity activity)
        {
            super(activity);
        }

        @Override
        protected int getItemViewId()
        {
            return R.layout.list_item_recom_critic;
        }

        @Override
        protected BaseViewHolder getViewHolder(int pos, View root)
        {
            return new BaseViewHolder(pos, root){

                @Override
                protected void bindView(final NetworkManager.CriticRespModel model)
                {
                    ImageView avatar = (ImageView) findView(R.id.avatar);
                    CommonManager.displayRoundAvatar(model.avatar, avatar);

                    TextView name = (TextView) findView(R.id.name);
                    name.setText(model.name);

//                    mRootView.setOnClickListener(new View.OnClickListener()
//                    {
//                        @Override
//                        public void onClick(View view)
//                        {
//                            CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, model);
//
//                            Intent intent = new Intent(mActivity, CriticDetailActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                            mActivity.startActivity(intent);
//                        }
//                    });
                }
            };
        }
    }
}
