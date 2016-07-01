package com.dym.film.fragment;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;

import com.dym.film.R;
import com.dym.film.activity.home.FilmRanking2Activity;
import com.dym.film.adapter.BboardFilmListAdapter;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.MatStatsUtil;

import java.util.ArrayList;

public class BboardFilmlistFragment extends BaseFragment
{
    private BboardFilmListAdapter bboardFilmListAdapter;
    private ArrayList<FilmBboardListInfo.BillboardModel> filmBboardDatas;
    private LoadMoreListView listRankingFilm;
    private SwipeRefreshLayout mRefreshLayout;

    private int curPage=0;
    private int limit=20;
    private boolean isRefreshOrLoad =true;//刷新
    private String sort="";


    @Override
    protected void initVariable()
    {
        MatStatsUtil.eventClick(mContext,MatStatsUtil.BOARD,null);
        filmBboardDatas =new ArrayList<FilmBboardListInfo.BillboardModel>();//观影指数
        bboardFilmListAdapter =new BboardFilmListAdapter(mContext, filmBboardDatas,R.layout.list_item_film_bboard);
    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_bboard_filmlist;
    }

    @Override
    protected void findViews(View view)
    {
        listRankingFilm = (LoadMoreListView)view.findViewById(R.id.listRankingFilm);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(mContext, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                isRefreshOrLoad =true;
                curPage=0;
                getFilmBillboardListData(0,limit);
            }
        });
    }


    @Override
    protected void initData()
    {
        listRankingFilm.setAdapter(bboardFilmListAdapter);
        curPage=0;
        isRefreshOrLoad =true;
        startFragmentLoading();
    }
    @Override
    protected void onFragmentLoading()
    {
        super.onFragmentLoading();
        getFilmBillboardListData(0,limit);
    }
    @Override
    protected void setListener()
    {
        listRankingFilm.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                FilmBboardListInfo.BillboardModel bboardModel = filmBboardDatas.get(position);
                Intent intent = new Intent(mContext, FilmRanking2Activity.class);
                intent.putExtra("bddata", bboardModel);
                startActivity(intent);

            }
        });
        listRankingFilm.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {
                isRefreshOrLoad =false;
                getFilmBillboardListData(++curPage, limit);
            }
        });
    }

    private void getFilmBillboardListData(int page, final int limit)
    {
        apiRequestManager.getFilmBillboardListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmBboardListInfo>()
        {
            @Override
            public void onSuccess(FilmBboardListInfo data)
            {

                onFragmentLoadingSuccess();//显示内容区
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                if (isRefreshOrLoad){
                    filmBboardDatas.clear();
                }
                filmBboardDatas.addAll(data.billboards);
                bboardFilmListAdapter.notifyDataSetChanged();

                if (data.billboards.size()==limit){//判断是否可以上拉加载更多
                    listRankingFilm.onLoadSucess(true);
                }else{
                    listRankingFilm.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                if (isRefreshOrLoad) {
                    onFragmentLoadingFailed();//显示刷新失败网络异常
                    CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                }else{
                    listRankingFilm.onLoadFailed();//加载失败
                }
            }
        });
    }

}