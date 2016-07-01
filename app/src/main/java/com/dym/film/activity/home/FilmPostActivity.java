package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonRecyclerAdapter;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.FilmPostEntity;
import com.dym.film.ui.LoadMoreRecyclerView;
import com.dym.film.ui.exrecyclerview.decoration.DividerGridItemDecoration;
import com.dym.film.utils.DimenUtils;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class FilmPostActivity extends BaseActivity
{
    private LoadMoreRecyclerView recyclerView;
    private ArrayList<String> filmPostDatas;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 21;
    private boolean isRefreshOrLoad = true;//刷新
    private CommonRecyclerAdapter<String> adapter;
    private String filmId="";
    private String filmName="";
    private int photoSum;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_film_post;
    }

    @Override
    protected void initVariable()
    {
        filmPostDatas = new ArrayList<String>();
        filmId=getIntent().getStringExtra("filmId");
        filmName=getIntent().getStringExtra("filmName");
        limit=getIntent().getIntExtra("photoSum",0);
    }

    @Override
    protected void findViews( )
    {
        showTopBar();
        setTitle("海报-"+filmName);
        recyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(mContext));
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
//        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(mContext, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                curPage = 0;
                isRefreshOrLoad = true;
                getFilmPostList(0, limit);
            }
        });
    }

    protected void initData()
    {
        curPage = 0;
        isRefreshOrLoad = true;
        fillAdapter();
        startActivityLoading();
    }

    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getFilmPostList(0, limit);
    }

    private void fillAdapter()
    {

        adapter = new CommonRecyclerAdapter<String>(mContext, filmPostDatas, R.layout.grid_item_film_post)
        {

            @Override
            public void convert(BaseViewHolder holder, String itemData, int position)
            {
                ImageView imgFilmCover = holder.getView(R.id.imgFilmCover);
                int width = (DimenUtils.getScreenWidth(mContext) - 40) / 3;
                int height = width * 4 / 3;
                imgFilmCover.getLayoutParams().width = width;
                imgFilmCover.getLayoutParams().height = height;
                //填充数据
                String url = QCloudManager.urlImage1(itemData, width, height);
                ImageLoaderUtils.displayImage(url, imgFilmCover);
            }
        };
        View footerView = mInflater.inflate(R.layout.layout_load_more_footer, null);
        adapter.addFooterView(footerView);
        recyclerView.setAdapter(adapter);
    }

    public void setListener()
    {
        adapter.setOnItemClickLitener(new CommonRecyclerAdapter.OnItemClickLitener()
        {
            @Override
            public void onItemClick(View itemView, int position)
            {
                Intent intent = new Intent();
                intent.setClass(FilmPostActivity.this, FilmBigPostActivity.class);
                intent.putExtra("filmPostDatas", filmPostDatas);
                intent.putExtra("curPosition", position);
                FilmPostActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onItemLongClick(View itemView, int position)
            {

            }
        });

        recyclerView.setOnLoadListener(new LoadMoreRecyclerView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreRecyclerView recyclerView)
            {
                isRefreshOrLoad = false;
                getFilmPostList(++curPage, limit);

            }
        });

    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }

    public void getFilmPostList(int page, final int limit)
    {
        apiRequestManager.getFilmPostList(filmId,page, limit, new AsyncHttpHelper.ResultCallback<FilmPostEntity>()
        {
            @Override
            public void onSuccess(FilmPostEntity data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isRefreshOrLoad) {
                    filmPostDatas.clear();
                }

                ArrayList<String> films = data.photos;
                filmPostDatas.addAll(films);
                adapter.notifyDataSetChanged();
                recyclerView.onLoadSucess(false);

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                if (isRefreshOrLoad) {
                    CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                    onActivityLoadingFailed();
                }else{
                    recyclerView.onLoadFailed();
                }
            }
        });

    }

}
