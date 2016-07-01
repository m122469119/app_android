package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.PreFilmListAdapter;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.FilmListInfo;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;
import com.dym.film.ui.LoadMoreListView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
@Deprecated
public class PreFilmActivity extends BaseActivity
{

    private ArrayList<FilmListInfo.FilmModel> preFilmDatas;
    private LoadMoreListView listPreFilm;
    private PreFilmListAdapter preFilmAdapter;

    private SwipeRefreshLayout mRefreshLayout;
    private int curPage=0;
    private int limit=20;
    private boolean isLoadOrRefresh=true;//刷新
    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_pre;
    }

    @Override
    protected void initVariable()
    {
        preFilmDatas =new ArrayList<FilmListInfo.FilmModel>();//即将上映
    }

    @Override
    protected void findViews()
    {
        listPreFilm = (LoadMoreListView)findViewById(R.id.listPreFilm);
        showTopBar();
        setTitle("即将上映");
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getPreFilmListData(0, limit);
                curPage=0;
                isLoadOrRefresh=true;
            }
        });
//        mRefreshLayout.setProgressBackgroundColorSchemeColor(Color.BLUE);
//        mRefreshLayout.setColorSchemeColors(Color.YELLOW,Color.BLUE);
    }

    @Override
    protected void initData()
    {
        preFilmAdapter = new PreFilmListAdapter(mContext, preFilmDatas, R.layout.list_item_film_pre);
        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(preFilmAdapter);
        animAdapter.setAbsListView(listPreFilm);
        listPreFilm.setAdapter(animAdapter);

        isLoadOrRefresh=true;
        curPage=0;
        startActivityLoading();
    }
    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getPreFilmListData(0, limit);
    }
    @Override
    protected void setListener()
    {
        listPreFilm.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String filmId= preFilmDatas.get(position).filmID+"";
                Intent intent = new Intent(PreFilmActivity.this, PreFilmDetailActivity.class);
                intent.putExtra(PreFilmDetailActivity.KEY_FILM_ID, filmId);
                intent.putExtra(FilmDetailActivity.KEY_FILM_NAME, preFilmDatas.get(position).name);
                startActivity(intent);

            }
        });

        listPreFilm.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {

                getPreFilmListData(++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }



    @Override
    public void doClick(View view)
    {
        this.finish();
    }
    private void getPreFilmListData(int page, final int limit)
    {
        apiRequestManager.getPreFilmListData(page, limit, new AsyncHttpHelper.ResultCallback<FilmListInfo>()
        {
            @Override
            public void onSuccess(FilmListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isLoadOrRefresh){
                    preFilmDatas.clear();
                }
                ArrayList<FilmListInfo.FilmModel> listData= data.films.list;
                preFilmDatas.addAll(listData);
                preFilmAdapter.notifyDataSetChanged();

                if (listData.size()==limit){
                    listPreFilm.onLoadSucess(true);
                }else{
                    listPreFilm.onLoadSucess(false);
                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingFailed();
                if (!isLoadOrRefresh){
                    listPreFilm.onLoadFailed();
                }
            }
        });
    }

}
