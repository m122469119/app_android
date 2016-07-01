package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.FilmHotListAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.BannerListInfo;
import com.dym.film.model.FilmHotListInfo;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.ui.LoadMoreListView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmHotListActivity extends BaseActivity
{
    private FilmHotListAdapter filmHotListAdapter;
    private ArrayList<FilmHotListInfo.NewsModel> filmHotDatas;
    private ArrayList<BannerListInfo.BannerModel> banners;
    private LoadMoreListView listFilmHot;

    private SwipeRefreshLayout mRefreshLayout;
    private int curPage=0;
    private int limit=20;
    private boolean isLoadOrRefresh=true;//刷新
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_hot;
    }

    @Override
    protected void initVariable()
    {
        banners=new ArrayList<BannerListInfo.BannerModel>();
        filmHotDatas=new ArrayList<FilmHotListInfo.NewsModel>();//观影指数
        filmHotListAdapter =new FilmHotListAdapter(this,banners,filmHotDatas,R.layout.list_item_film_hot);
    }

    @Override
    protected void findViews()
    {
        showTopBar();
        setTitle("热点资讯");
        listFilmHot = (LoadMoreListView)findViewById(R.id.listFilmHot);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getFilmHotBannerData();
                getFilmHotListData(0, limit);
                curPage=0;
                isLoadOrRefresh=true;
            }
        });
    }

    @Override
    protected void initData()
    {
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(filmHotListAdapter);
        animAdapter.setAbsListView(listFilmHot);
        listFilmHot.setAdapter(animAdapter);
        isLoadOrRefresh=true;
        curPage=0;
        startActivityLoading();
    }
    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getFilmHotBannerData();
        getFilmHotListData(0, limit);
    }

    private void getFilmHotBannerData()
    {
        apiRequestManager.getFilmBannerData(5,new AsyncHttpHelper.ResultCallback<BannerListInfo>()
        {
            @Override
            public void onSuccess(BannerListInfo data)
            {
                banners.clear();
                banners.addAll(data.banners);
                filmHotListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {

            }
        });

    }
    private void getFilmHotListData(int page, final int limit)
    {
        apiRequestManager.getFilmHotListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmHotListInfo>()
        {
            @Override
            public void onSuccess(FilmHotListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isLoadOrRefresh){
                    filmHotDatas.clear();
                }
                ArrayList<FilmHotListInfo.NewsModel> newFilmHotDatas = data.news;
                filmHotDatas.addAll(newFilmHotDatas);
                filmHotListAdapter.notifyDataSetChanged();
                if (newFilmHotDatas.size()==limit){
                    listFilmHot.onLoadSucess(true);
                }else{
                    listFilmHot.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingFailed();
                if (!isLoadOrRefresh){
                    listFilmHot.onLoadFailed();
                }
            }
        });

    }
    @Override
    protected void setListener()
    {
        listFilmHot.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String url= filmHotDatas.get(position).url;
                url= ConfigInfo.BASE_URL+url;
                Intent intent = new Intent(FilmHotListActivity.this, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,1);
                intent.putExtra("imageUrl",filmHotDatas.get(position).logo);
                intent.putExtra("title",filmHotDatas.get(position).title);
                startActivity(intent);
//                FilmHotActivity.this.overridePendingTransition(R.anim.activity_zoom_in, android.R.anim.fade_out);
            }
        });

        listFilmHot.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {

                getFilmHotListData(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }

}
