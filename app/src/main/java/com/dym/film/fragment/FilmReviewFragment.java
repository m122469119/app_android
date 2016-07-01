package com.dym.film.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.activity.home.SingleFilmReviewActivity;
import com.dym.film.activity.search.SearchActivity;
import com.dym.film.adapter.AllFilmReviewRecyclerAdapter;
import com.dym.film.adapter.base.BaseListAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.controllers.ExceptionLayoutViewController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.HorizontalListView;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.ui.exrecyclerview.OnRcvScrollListener;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

public class FilmReviewFragment extends Fragment
{
    private final static int PAGE_LIMIT_NUM = 20;

    private Activity mActivity = null;
    private NetworkManager mNetworkManager = NetworkManager.getInstance();

    private HotTagAdapter mTagAdapter = null;
    /**
     * 影评列表
     */
    private SwipeRefreshLayout mRefreshLayout = null;
    private LoadMoreRecyclerView mRecyclerView = null;
    private AllFilmReviewRecyclerAdapter mFilmReviewAdapter = null;
    private ExRcvAdapterWrapper mAdapterWrapper = null;

    private ExceptionLayoutViewController mExcepController = null;

    /**
     * 当前影评的页
     */
    private int mCurrentPageIndex = 0;
    private boolean mIsRefreshingOrLoadMore = false;

    private ViewPager mParentViewPager = null;
    public void setViewPager(ViewPager pager)
    {
        mParentViewPager = pager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View rootView = inflater.inflate(R.layout.fragment_film_review, container, false);
        initializeView(rootView);
        return rootView;
    }

    private void initializeView(View view)
    {
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayout);
        mRefreshLayout.setColorSchemeColors(Color.BLACK);

        View header = View.inflate(mActivity, R.layout.layout_home_film_review_header, null);

        final HorizontalListView hList = (HorizontalListView) header.findViewById(R.id.hotTagHorListView);
        mTagAdapter = new HotTagAdapter(mActivity);
        hList.setAdapter(mTagAdapter);
        hList.addConflictViews(mParentViewPager, mRefreshLayout);

        hList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l)
            {
                long filmId = mTagAdapter.getFilmId(i);
                Intent intent = new Intent(mActivity, SingleFilmReviewActivity.class);
                intent.putExtra("filmId", String.valueOf(filmId));
                intent.putExtra("filmName", mTagAdapter.getItem(i));
                startActivity(intent);
            }
        });

        /**
         * 初始化RecyclerView
         */
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
        mFilmReviewAdapter = new AllFilmReviewRecyclerAdapter(mActivity);

        mRecyclerView = (LoadMoreRecyclerView) view.findViewById(R.id.filmReviewList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setLinearLayoutManager(mLayoutManager);
        mRecyclerView.setLoadMoreListener(mLoadMoreListener);

        mAdapterWrapper = new ExRcvAdapterWrapper<>(mFilmReviewAdapter, mLayoutManager);

        mAdapterWrapper.setHeaderView(header);
        mRecyclerView.setAdapter(mAdapterWrapper);

        /**
         * 设置刷新
         */
        mRefreshLayout.setNestedScrollingEnabled(true);
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
         * 设置滑动效果
         */
        final int pLeft = mRecyclerView.getPaddingLeft();
        final int pRight = mRecyclerView.getPaddingRight();
        final int pTop = mRecyclerView.getPaddingTop();
        final int pBottom = mRecyclerView.getPaddingBottom();
        final View hotFilmLayout = view.findViewById(R.id.hotFilmLayout);
//        mRecyclerView.addOnScrollListener(new OnRcvScrollListener()
//        {
//            private int mLastDistanceY = 0;
//
//            @Override
//            public void onScrollUp() {}
//
//            @Override
//            public void onScrollDown() {}
//
//            @Override
//            public void onBottom() {}
//
//            @Override
//            public void onScrolled(int distanceX, int distanceY)
//            {
////                mRefreshLayout.setEnabled(distanceY == 0);
////                mRefreshLayout.setEnabled(distanceY == 0);
//
//                int top = mRecyclerView.getPaddingTop();
//                int dy = Math.abs(mLastDistanceY - distanceY);
//                LogUtils.e("Scrolled", "TOP: " + top + " Dy: " + dy + " lastDY: " + mLastDistanceY + " DX: " + distanceX + " DY: " + distanceY);
//                if (mLastDistanceY > distanceY) {
//                    /**
//                     * 向下
//                     */
//                    if (top >= 0) {
//                        dy = top + dy;
//                        if (dy > pTop) {
//                            dy = pTop;
//                        }
//                        mRecyclerView.setPadding(pLeft, dy, pRight, pBottom);
//                        hotFilmLayout.scrollTo(0, pTop-dy);
//                    }
//                }
//                else if (mLastDistanceY < distanceY) {
//                    /**
//                     * 向上
//                     */
//                    if (top <= pTop) {
//                        dy = top - dy;
//                        if (dy < 0) {
//                            dy = 0;
//                        }
//                        mRecyclerView.setPadding(pLeft, dy, pRight, pBottom);
//                        hotFilmLayout.scrollTo(0, pTop-dy);
//                    }
//                }
//                mLastDistanceY = distanceY;
//            }
//        });

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

    public synchronized boolean refresh()
    {
        if (mIsRefreshingOrLoadMore) {
            return false;
        }
        mIsRefreshingOrLoadMore = true;

        NetworkManager.getInstance().getReviewTagsList(new HttpRespCallback<NetworkManager.RespTagsList>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                //
            }

            @Override
            protected void runOnMainThread(Message msg)
            {
                NetworkManager.RespTagsList list = (NetworkManager.RespTagsList) msg.obj;
                mTagAdapter.setData(list);
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

    public class HotTagAdapter extends BaseListAdapter<String>
    {
        public final static String TAG = "HotTagTodayAdapter";

        public ArrayList<NetworkManager.FilmTag> films = new ArrayList<>();

        public long getFilmId(int index) {
            return index >= 0 && index < films.size() ? films.get(index).filmID : 0;
        }

        public void setData(NetworkManager.RespTagsList list)
        {
            films.clear();
            films.addAll(list.films);

            super.setData(list.tags);
        }

        public HotTagAdapter(@NonNull Activity activity)
        {
            super(activity);
        }

        @Override
        protected int getItemViewId()
        {
            return R.layout.list_item_hot_film_tag;
        }

        @Override
        protected BaseViewHolder getViewHolder(int pos, View root)
        {
            return new ViewHolder(pos, root);
        }

        private class ViewHolder extends BaseViewHolder
        {
            protected ViewHolder(int pos, View root)
            {
                super(pos, root);
            }

            @Override
            protected void bindView(String tag)
            {
                if (mRootView == null) {
                    return;
                }

                int pos = mPosition;
                String post = pos >= 0 && pos<films.size() ? films.get(pos).post:"";
                ImageView postView = (ImageView) findView(R.id.post);
                CommonManager.displayImage2(post, postView, R.drawable.ic_default_loading_img);

                TextView nameView = (TextView) findView(R.id.name);
                nameView.setText(tag);
//                final TextView textView = (TextView) mRootView;
//                textView.setText(tag);
//                textView.setBackgroundResource(R.drawable.bg_film_tag_unselect);
//                textView.setOnTouchListener(new View.OnTouchListener()
//                {
//                    @Override
//                    public boolean onTouch(final View view, MotionEvent motionEvent)
//                    {
//                        switch (motionEvent.getAction()) {
//                            case MotionEvent.ACTION_DOWN:
//                                view.setBackgroundResource(R.drawable.bg_film_tag_select);
//                                view.postDelayed(new Runnable()
//                                {
//                                    @Override
//                                    public void run()
//                                    {
//                                        view.setBackgroundResource(R.drawable.bg_film_tag_unselect);
//                                    }
//                                }, 200);
//                                break;
//
//                            case MotionEvent.ACTION_CANCEL:
//                            case MotionEvent.ACTION_UP:
//                                view.setBackgroundResource(R.drawable.bg_film_tag_unselect);
//                                break;
//                        }
//                        return false;
//                    }
//                });
            }
        }
    }
}
