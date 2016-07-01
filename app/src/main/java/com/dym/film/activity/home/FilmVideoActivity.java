package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.FilmVideoEntity;
import com.dym.film.ui.LoadMoreListView;
import com.dym.film.ui.listviewanimations.appearance.AnimationAdapter;
import com.dym.film.ui.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.dym.film.utils.DimenUtils;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmVideoActivity extends BaseActivity
{
    private LoadMoreListView listFilmVideo;
    private CommonBaseAdapter<FilmVideoEntity.FilmVideoInfo> mAdapter;
    private ArrayList<FilmVideoEntity.FilmVideoInfo> mVideoDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isRefreshOrLoad = true;//刷新
    private String filmId;
    private String filmName;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_video;
    }

    @Override
    protected void initVariable()
    {
        filmId = getIntent().getStringExtra("filmId");
        filmName = getIntent().getStringExtra("filmName");
        mVideoDatas = new ArrayList<FilmVideoEntity.FilmVideoInfo>();
    }

    @Override
    protected void findViews()
    {
        listFilmVideo = $(R.id.listFilmVideo);
        showTopBar();
        setTitle("视频-" + filmName);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                curPage = 0;
                isRefreshOrLoad = true;
                getFilmVideoList(0, limit);

            }
        });

    }

    @Override
    protected void initData()
    {

        mAdapter = new CommonBaseAdapter<FilmVideoEntity.FilmVideoInfo>(this, mVideoDatas, R.layout.list_item_film_video)
        {
            @Override
            public void convert(ViewHolder holder, FilmVideoEntity.FilmVideoInfo itemData, int position)
            {
                ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
                TextView tvVideoTitle = holder.getView(R.id.tvVideoTitle);
                TextView tvVideoTime = holder.getView(R.id.tvVideoTime);
                ImageLoaderUtils.displayImage(itemData.imgUrl,imgFilmCover);
                tvVideoTitle.setText(itemData.title);
                if (itemData.duration==0){
                    tvVideoTime.setText("");
                }else if (itemData.duration<60){
                    tvVideoTime.setText(itemData.duration+"秒");
                }else{
                    tvVideoTime.setText(itemData.duration/60+"分"+itemData.duration%60+"秒");
                }

            }

        };

        AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
        animAdapter.setAbsListView(listFilmVideo);
        listFilmVideo.setAdapter(animAdapter);
        isRefreshOrLoad = true;
        curPage = 0;
        startActivityLoading();
    }

    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getFilmVideoList(curPage, limit);
    }

    @Override
    protected void setListener()
    {
        listFilmVideo.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(mContext, VideoPlayerActivity.class);
                intent.putExtra("videoUrl", mVideoDatas.get(position).videoUrl);
                intent.putExtra("filmName", filmName);
                startActivity(intent);
            }
        });

        listFilmVideo.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {
                isRefreshOrLoad = false;
                getFilmVideoList(++curPage, limit);

            }
        });
    }

    @Override
    public void doClick(View view)
    {
        finish();
    }

    public void getFilmVideoList(int page, final int limit)
    {

        apiRequestManager.getFilmVideoList(filmId, page, limit, new AsyncHttpHelper.ResultCallback<FilmVideoEntity>()
        {
            @Override
            public void onSuccess(FilmVideoEntity data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isRefreshOrLoad) {
                    mVideoDatas.clear();
                }

                ArrayList<FilmVideoEntity.FilmVideoInfo> films = data.trailers;
                mVideoDatas.addAll(films);
                mAdapter.notifyDataSetChanged();

               if (films.size() == limit) {
                    listFilmVideo.onLoadSucess(true);
                }
                else {
                    listFilmVideo.onLoadSucess(false);

                }
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                if (isRefreshOrLoad) {
                    CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                    onActivityLoadingFailed();
                }
                else {
                    listFilmVideo.onLoadFailed();
                }
            }
        });
    }
}
