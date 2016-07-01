package com.dym.film.controllers;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.MainActivity;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.activity.sharedticket.AroundSharedTicketActivity;
import com.dym.film.activity.sharedticket.TagSharedTicketActivity;
import com.dym.film.adapter.HotTagTodayAdapter;
import com.dym.film.adapter.StaggerSharedTicketAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.manager.data.MainSharedTicketDataManager;
import com.dym.film.ui.CircleImageView;
import com.dym.film.ui.HorizontalListView;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

/**
 * 晒票直播的view控制器
 */
public class SharedTicketViewController extends BaseViewController
{
    public final static String TAG = "ShareTicketViewCtrl";

    private final static int PAGE_LIMIT_NUM = 20;

    private boolean mIsRefreshingOrLoadMore = false;

    private NetworkManager mNetworkManager = NetworkManager.getInstance();

    private MainSharedTicketDataManager mDataManager = MainSharedTicketDataManager.mInstance;

    /**
     * Content Layout
     */
    private LinearLayout mContentLayout = null;
    private ExceptionLayoutViewController mExcepController = null;

    /**
     * 今日热门的tag List view
     */
    private HorizontalListView mHotTagsListView = null;
    private HotTagTodayAdapter mHotTagsAdapter = null;
    protected AdapterView.OnItemClickListener mTagItemClickedListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            startTagSharedTicketActivity(mHotTagsAdapter.getItem(position));
        }
    };
    /**
     * 刷新的Layout
     */
    private SwipeRefreshLayout mRefreshLayout = null;
    /**
     * 瀑布流
     */
    private LoadMoreRecyclerView mRecyclerView = null;
    private StaggeredGridLayoutManager mStaggerLayoutManager = null;
    private StaggerSharedTicketAdapter mStaggerAdapter = null;
    private ExRcvAdapterWrapper mAdapterWrapper = null;
    protected HttpRespCallback<NetworkManager.RespSharedTicketList> mTicketRefreshCallback = new HttpRespCallback<NetworkManager.RespSharedTicketList>()
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
            NetworkManager.RespSharedTicketList list = (NetworkManager.RespSharedTicketList) msg.obj;
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
    /**
     * 晒票的选项view控制
     */
    private ShareTicketDialogViewController mSelectDialogController = null;
    private int mCurrentPageIndex = 0;
    protected HttpRespCallback<NetworkManager.RespSharedTicketList> mTicketLoadMoreCallback = new HttpRespCallback<NetworkManager.RespSharedTicketList>()
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
            NetworkManager.RespSharedTicketList list = (NetworkManager.RespSharedTicketList) msg.obj;

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
    private MainActivity mainActivity;
    private TextView tvStubEventTitle;
    private LinearLayout layStubEvent;
    private String eventUrl;
    protected HttpRespCallback<NetworkManager.RespTagsList> mCallbackTagsList = new HttpRespCallback<NetworkManager.RespTagsList>()
    {
        @Override
        public void onRespFailure(int code, String msg)
        {
        }

        @Override
        public void onRespSuccess(NetworkManager.RespTagsList model, String body)
        {
            sendMessage(WHAT_HTTP_SUCCESS, model);
        }

        @Override
        public void runOnMainThread(Message msg)
        {
            NetworkManager.RespTagsList list = (NetworkManager.RespTagsList) msg.obj;
            mHotTagsAdapter.setData(list.tags);
            //stubEvent数据
            if (list.stubEvent == null||list.stubEvent.url.equals("")) {
                layStubEvent.setVisibility(View.GONE);
            }
            else {
                eventUrl = list.stubEvent.url;
                LogUtils.i("123","eventUrl-"+ eventUrl);
                tvStubEventTitle.setText(list.stubEvent.title);
            }
        }
    };

    public SharedTicketViewController(@NonNull Activity activity, View view)
    {
        super(activity, view);
        mainActivity = (MainActivity) activity;
        initializeShareTicketView();
    }

    public void onResume()
    {
        if (mDataManager.needRefreshAll()) {
            refresh();
        }
    }

    public void onUserStateChanged(BaseViewCtrlActivity.UserState oldState)
    {
        if (oldState.mIsLogin != UserInfo.isLogin) {
            refresh();
        }
        else {
            mDataManager.refreshMyUserInfo();
            refreshVisibleUserInfo();
        }
    }

    public void onDestroy()
    {
        mDataManager.clear();
    }

    /**
     * 改变头像后，需要重新刷新可见的头像
     */
    private void refreshVisibleUserInfo()
    {
        if (mStaggerLayoutManager == null || mStaggerAdapter == null) {
            return;
        }

        int first = 0;
        int last = 0;

        int[] firstInfo = new int[2];
        mStaggerLayoutManager.findFirstVisibleItemPositions(firstInfo);

        int[] lastInfo = new int[2];
        mStaggerLayoutManager.findLastVisibleItemPositions(lastInfo);

        first = Math.min(firstInfo[1], firstInfo[0]) - 1;
        first = first < 0 ? 0 : first;

        last = Math.max(lastInfo[0], lastInfo[1]) + 1;
        last = last >= mStaggerAdapter.getItemCount() ? mStaggerAdapter.getItemCount() - 1 : last;

        //Loge(TAG, "First: " + first  + " Last: " + last);
        for (int i = first; i <= last; ++i) {
            View view = mStaggerLayoutManager.findViewByPosition(i);
            if (view != null) {
                NetworkManager.SharedTicketRespModel model = mDataManager.get(i);
                if (model != null && model.writer != null &&
                        UserInfo.userID == model.writer.userID) {
                    CircleImageView avatar = (CircleImageView) view.findViewById(R.id.userHeadImage);
                    TextView name = (TextView) view.findViewById(R.id.userNameTextView);
                    ImageView gender = (ImageView) view.findViewById(R.id.genderImage);
                    if (name != null) {
                        name.setText(UserInfo.name);
                    }
                    if (avatar != null) {
                        String url = QCloudManager.urlImage2(model.writer.avatar, ConfigInfo.SIZE_AVATAR);
                        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
                        builder.cacheOnDisk(false);
                        builder.cacheInMemory(false);
                        builder.showImageForEmptyUri(R.drawable.ic_default_photo);
                        builder.showImageOnFail(R.drawable.ic_default_photo);
                        ImageLoader.getInstance().displayImage(url, avatar);
                    }
                    if (gender != null) {
                        gender.setImageResource(UserInfo.gender == 1 ? R.drawable.ic_gender_male : R.drawable.ic_gender_female);
                    }
                }
            }
        }
    }

    /**
     * 初始化所有的页面控件
     */
    private void initializeShareTicketView()
    {
        // 初始化title
        findViewById(R.id.titleAroundImage).setOnClickListener(mViewClickedListener);
        findViewById(R.id.titleShareTicketImage).setOnClickListener(mViewClickedListener);

        // 初始化今日热门

        mHotTagsAdapter = new HotTagTodayAdapter(mActivity);

        //初始化stubEvent
        tvStubEventTitle = (TextView) findViewById(R.id.tvStubEventTitle);
        layStubEvent = (LinearLayout) findViewById(R.id.layStubEvent);
        layStubEvent.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String url= ConfigInfo.BASE_URL+ eventUrl;
                Intent intent = new Intent(mActivity, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,2);
                mActivity.startActivity(intent);
            }
        });
        /**
         * 请求数据
         */

        mHotTagsListView = (HorizontalListView) findViewById(R.id.hotTagHorListView);
        mHotTagsListView.setViewPager(mainActivity.viewPager);
        mHotTagsListView.setAdapter(mHotTagsAdapter);
        mHotTagsListView.setOnItemClickListener(mTagItemClickedListener);

        //初始化刷新，加载更多控件
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

        // 初始化RecyclerView
        // 初始化晒票列表
        mStaggerAdapter = new StaggerSharedTicketAdapter(mActivity, mDataManager);
        mStaggerLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.sharedTicketRecyclerView);
        mRecyclerView.setLayoutManager(mStaggerLayoutManager);
        mRecyclerView.setStaggerLayoutManager(mStaggerLayoutManager);
        mRecyclerView.setLoadMoreListener(mLoadMoreListener);

        mAdapterWrapper = new ExRcvAdapterWrapper<>(mStaggerAdapter, mStaggerLayoutManager);
        mRecyclerView.setAdapter(mAdapterWrapper);

        // 初始化异常页面
        mContentLayout = (LinearLayout) findViewById(R.id.contentLoadingLayout);
        mExcepController = new ExceptionLayoutViewController(mActivity, new ExceptionLayoutViewController.ViewCallback()
        {
            @Override
            public void onExceptionViewClicked()
            {
                mExcepController.progress();
                refresh();
            }
        }, findViewById(R.id.exceptionPage));

//        mExcepController.hide();
//        CommonManager.setRefreshingState(mRefreshLayout, true);
        mExcepController.progress();
        mContentLayout.setVisibility(View.INVISIBLE);
        refresh();
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
        mNetworkManager.getTagsList(mCallbackTagsList);

        mCurrentPageIndex = 0;
        mNetworkManager.getSharedTicketList(mCurrentPageIndex, PAGE_LIMIT_NUM, mTicketRefreshCallback);
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

                if (mStaggerAdapter.getItemCount() == 0 && mHotTagsAdapter.getCount() == 0) {
                    mExcepController.show();
                    mContentLayout.setVisibility(View.INVISIBLE);
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
//                mExcepController.show();
                break;

            default:
                mExcepController.hide();
                mContentLayout.setVisibility(View.VISIBLE);
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

        mNetworkManager.getSharedTicketList(mCurrentPageIndex + 1, PAGE_LIMIT_NUM, mTicketLoadMoreCallback);
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
                mExcepController.hide();
                mRecyclerView.setFooterClickListener(null);
                break;
        }
        mRecyclerView.loadMoreFinished(state);
    }

    @Override
    protected void onViewClicked(@NonNull View view)
    {
        switch (view.getId()) {
            case R.id.titleAroundImage:
                MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.SHOW_AROUND);
                startAroundSharedTicketActivity();
                break;
            case R.id.titleShareTicketImage:
                MatStatsUtil.eventClick(view.getContext(), MatStatsUtil.TICKET_SHOW);
                if (mSelectDialogController == null) {
                    mSelectDialogController = new ShareTicketDialogViewController(mActivity);
                }
                mSelectDialogController.show();
                break;
        }
    }

    /**
     * 启动周围的晒票页面
     */
    private void startAroundSharedTicketActivity()
    {
        Intent intent = new Intent(mActivity, AroundSharedTicketActivity.class);

        mActivity.startActivity(intent);
    }

    /**
     * 启动标签的影评页面
     */
    private void startTagSharedTicketActivity(String tag)
    {
        Intent intent = new Intent(mActivity, TagSharedTicketActivity.class);
        intent.putExtra(TagSharedTicketActivity.KEY_TAG, tag);

        mActivity.startActivity(intent);
    }
}
