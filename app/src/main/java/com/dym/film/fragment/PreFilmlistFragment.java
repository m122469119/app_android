package com.dym.film.fragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.adapter.PreFilmListAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.BannerListInfo;
import com.dym.film.model.FilmListInfo;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.ui.loopbanner.LoopBanner;
import com.dym.film.ui.loopbanner.LoopPageAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.MatStatsUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreFilmlistFragment extends BaseFragment
{
    private PreFilmListAdapter filmListAdapter;
    private LoadMoreListView listPreFilm;
    private SwipeRefreshLayout refreshLayout;
    private LoopBanner loopBanner;
    private int curPage = 0;
    private int limit = 20;
    private boolean isRefreshOrLoad = true;//刷新
    private ArrayList<BannerListInfo.BannerModel> bannerImagesData;
    private ArrayList<FilmListInfo.FilmModel> filmListDatas;
    @Override
    protected void initVariable()
    {
        MatStatsUtil.eventClick(mContext,MatStatsUtil.COMING,null);
        filmListDatas = new ArrayList<FilmListInfo.FilmModel>();//热映电影列表
        filmListAdapter = new PreFilmListAdapter(mContext, filmListDatas, R.layout.list_item_film_pre);
        bannerImagesData = new ArrayList<BannerListInfo.BannerModel>();

    }

    @Override
    protected int setContentView()
    {
        return R.layout.fragment_pre_filmlist;
    }

    @Override
    protected void findViews(View view)
    {
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        listPreFilm = (LoadMoreListView) view.findViewById(R.id.listPreFilm);
        loopBanner = new LoopBanner(mContext);
        int width = DimenUtils.getScreenWidth(mContext);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, width/3);
        loopBanner.setLayoutParams(params);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                curPage = 0;
                isRefreshOrLoad = true;
                loadData();
            }
        });
    }

    @Override
    protected void initData()
    {
        isRefreshOrLoad = true;
        curPage = 0;
        fillAdapter();
        startFragmentLoading();
    }
    private void fillAdapter()
    {
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(filmListAdapter);
        animAdapter.setAbsListView(listPreFilm);
        listPreFilm.setAdapter(animAdapter);
        loopBanner.setPageAdapter(new LoopPageAdapter<BannerListInfo.BannerModel>(mContext, bannerImagesData, R.layout.layout_main_banner_item)
        {

            @Override
            public void convert(ViewHolder holder, final BannerListInfo.BannerModel itemData, final int position)
            {
                // TODO Auto-generated method stub
                ImageView imageView = (ImageView) holder.getConvertView();
                String img = itemData.img;
                ImageLoader.getInstance().displayImage(img, imageView);
                imageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String url = itemData.url;
                        CommonManager.processBannerClick(mContext, url);
                    }
                });
            }
        });

    }
    @Override
    protected void onFragmentLoading()
    {
        super.onFragmentLoading();
        loadData();
    }

    private void loadData(){
//        getPreFilmBannerData();
        getPreFilmListData(0, limit);
    }
    private void getPreFilmListData(int page, final int limit)
    {
        apiRequestManager.getPreFilmListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmListInfo>()
        {
            @Override
            public void onSuccess(FilmListInfo data)
            {
                onFragmentLoadingSuccess();//显示内容区
                CommonManager.setRefreshingState(refreshLayout, false);//隐藏下拉刷新
                if (isRefreshOrLoad){
                    filmListDatas.clear();
                }
                filmListDatas.addAll(data.films.list);
                filmListAdapter.notifyDataSetChanged();

                if (data.films.list.size()==limit){//判断是否可以上拉加载更多
                    listPreFilm.onLoadSucess(true);
                }else{
                    listPreFilm.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                if (isRefreshOrLoad) {
                    onFragmentLoadingFailed();//显示刷新失败网络异常
                    CommonManager.setRefreshingState(refreshLayout, false);//隐藏下拉刷新
                }else{
                    listPreFilm.onLoadFailed();//加载失败
                }
            }
        });

    }

    private void getPreFilmBannerData()
    {
        apiRequestManager.getFilmBannerData(1, new AsyncHttpHelper.ResultCallback<BannerListInfo>()
        {
            @Override
            public void onSuccess(BannerListInfo data)
            {
                if(listPreFilm.getHeaderViewsCount()==0){
                    listPreFilm.addHeaderView(loopBanner);
                }
                ArrayList<BannerListInfo.BannerModel> banners = data.banners;
                bannerImagesData.clear();
                bannerImagesData.addAll(banners);
                if (bannerImagesData.size() == 0) {
                    listPreFilm.removeHeaderView(loopBanner);
                }
                loopBanner.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                listPreFilm.removeHeaderView(loopBanner);
            }
        });
    }
    @Override
    protected void setListener()
    {
        listPreFilm.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if(listPreFilm.getHeaderViewsCount()!=0){
                    position--;
                }
                FilmListInfo.FilmModel filmModel = filmListDatas.get(position);
                Intent intent = new Intent(mContext, FilmDetailActivity.class);
                intent.putExtra(FilmDetailActivity.KEY_FILM_ID, filmModel.filmID + "");
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, filmModel.name);
                startActivity(intent);
            }
        });

        listPreFilm.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {
                isRefreshOrLoad = false;
                getPreFilmListData(++curPage, limit);

            }
        });
    }

    @Override
    public void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        loopBanner.startTurning(5000);
    }

    @Override
    public void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
        loopBanner.stopTurning();
    }
    @Override
    public void onPause()
    {
        super.onPause();
        loopBanner.stopTurning();
    }

}
