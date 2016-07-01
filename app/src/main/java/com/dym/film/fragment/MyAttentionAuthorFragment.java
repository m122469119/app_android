package com.dym.film.fragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.MyAttentionAuthorListInfo;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.utils.DimenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class MyAttentionAuthorFragment extends BaseFragment
{
    private LoadMoreListView listMyAttentionAuthor;
    private ArrayList<MyAttentionAuthorListInfo.CriticModel> mListData;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isLoadOrRefresh = true;//刷新
    private CommonBaseAdapter<MyAttentionAuthorListInfo.CriticModel> adapter;
    private LinearLayout layNoAttention;

    @Override
    protected void initVariable()
    {
        mListData = new ArrayList<MyAttentionAuthorListInfo.CriticModel>();
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_my_attention_author;
    }

    @Override
    protected void findViews(View view)
    {
        listMyAttentionAuthor = (LoadMoreListView) view.findViewById(R.id.listMyAttentionAuthor);
        layNoAttention = (LinearLayout) view.findViewById(R.id.layNoAttention);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(mContext, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getMyAttentionAuthorList(0, limit);
                curPage = 0;
                isLoadOrRefresh = true;
            }
        });
    }

    public void initData()
    {
        curPage = 0;
        isLoadOrRefresh = true;
        fillAdapter();
//        CommonManager.setRefreshingState(mRefreshLayout, true);
//        getMyAttentionAuthorList(0, limit);
        startFragmentLoading();
    }

    @Override
    protected void onFragmentLoading()
    {
        super.onFragmentLoading();
        getMyAttentionAuthorList(0, limit);
    }

    private void fillAdapter()
    {
        adapter = new CommonBaseAdapter<MyAttentionAuthorListInfo.CriticModel>(mContext, mListData, R.layout.list_item_my_attention_author)
        {
            @Override
            public void convert(ViewHolder holder, MyAttentionAuthorListInfo.CriticModel itemData, int position)
            {
                ImageView imgPhoto = holder.getView(R.id.imgPhoto);
                TextView tvUserName = holder.getView(R.id.tvUserName);
                TextView tvUserHonor = holder.getView(R.id.tvUserHonor);
                TextView tvCinecismNum = holder.getView(R.id.tvCinecismNum);
                String url = QCloudManager.urlImage1(itemData.avatar, DimenUtils.dp2px(mContext,55), DimenUtils.dp2px(mContext,55));

                //填充数据
                if (!TextUtils.isEmpty(url)) {
                    ImageLoader.getInstance().displayImage(url, imgPhoto);

                }
                tvUserName.setText(itemData.name);
                tvUserHonor.setText(itemData.title);
                tvCinecismNum.setText(itemData.cinecismNum + "");

            }
        };
        listMyAttentionAuthor.setAdapter(adapter);

    }

    public void setListener()
    {
        listMyAttentionAuthor.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

                MyAttentionAuthorListInfo.CriticModel itemData = mListData.get(position);
                NetworkManager.CriticRespModel critic = new NetworkManager.CriticRespModel();
                critic.criticID = itemData.criticID;
                critic.avatar = itemData.avatar;
                critic.title = itemData.title;
                critic.name = itemData.name;
                CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
                Intent intent = new Intent(mContext, CriticDetailActivity.class);
                startActivity(intent);

            }
        });

        listMyAttentionAuthor.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {

                getMyAttentionAuthorList(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }

    /*获取影评人列表数据*/
    public void getMyAttentionAuthorList(int page, final int limit)
    {

        apiRequestManager.getMyAttentionAuthorList(page, limit, new AsyncHttpHelper.ResultCallback<MyAttentionAuthorListInfo>()
        {
            @Override
            public void onSuccess(MyAttentionAuthorListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onFragmentLoadingSuccess();
                if (isLoadOrRefresh) {
                    mListData.clear();
                }
                ArrayList<MyAttentionAuthorListInfo.CriticModel> critics = data.critics;
                mListData.addAll(critics);
                adapter.notifyDataSetChanged();

                layNoAttention.setVisibility(View.INVISIBLE);
                if (critics.size() == 0) {
                    layNoAttention.setVisibility(View.VISIBLE);
                    listMyAttentionAuthor.onLoadUnavailable();
                }
                else if (critics.size() == limit) {
                    listMyAttentionAuthor.onLoadSucess(true);
                }
                else {
                    listMyAttentionAuthor.onLoadSucess(false);
                }

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onFragmentLoadingFailed();
                if (!isLoadOrRefresh) {
                    listMyAttentionAuthor.onLoadFailed();
                }
            }
        });
    }

}