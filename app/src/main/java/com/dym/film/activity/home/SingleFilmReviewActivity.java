package com.dym.film.activity.home;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.adapter.SingleFilmReviewListAdapter;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.manager.CommonManager;
import com.dym.film.model.FilmReviewListInfo;
import com.dym.film.utils.DimenUtils;
import com.dym.film.ui.LoadMoreListView;

import java.util.ArrayList;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class SingleFilmReviewActivity extends BaseActivity
{
    String filmId;
    private SingleFilmReviewListAdapter filmReviewListAdapter;
    private ArrayList<FilmReviewListInfo.CinecismModel> filmReviewDatas;
    private LoadMoreListView listSingleFilmReview;
    private SwipeRefreshLayout mRefreshLayout;
    private int curPage = 0;
    private int limit = 20;
    private boolean isLoadOrRefresh = true;//刷新
    private String filmName;

    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_single_film_review;
    }

    @Override
    protected void initVariable()
    {
        filmId = getIntent().getStringExtra("filmId");
        filmName = getIntent().getStringExtra("filmName");
        filmReviewDatas = new ArrayList<FilmReviewListInfo.CinecismModel>();//影评数据
        filmReviewListAdapter = new SingleFilmReviewListAdapter(this, filmReviewDatas, R.layout.list_item_single_film_review);
    }

    @Override
    protected void findViews()
    {
        showTopBar();
        setTitle(filmName);
        listSingleFilmReview = (LoadMoreListView) findViewById(R.id.listSingleFilmReview);
        // 初始化刷新，加载更多控件
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        mRefreshLayout.setDistanceToTriggerSync(DimenUtils.dp2px(this, 100));
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getFilmReviewList(filmId, 0, limit);
                curPage = 0;
                isLoadOrRefresh = true;
            }
        });
    }

    @Override
    protected void initData()
    {

        listSingleFilmReview.setAdapter(filmReviewListAdapter);

        isLoadOrRefresh = true;
        curPage = 0;
        startActivityLoading();
    }
    @Override
    protected void onActivityLoading()
    {
        super.onActivityLoading();
        getFilmReviewList(filmId, 0, limit);
    }
    @Override
    protected void setListener()
    {
        listSingleFilmReview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                long cinecismID = filmReviewDatas.get(position).cinecismID;
                Intent intent = new Intent(SingleFilmReviewActivity.this, FilmReviewDetailActivity.class);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, cinecismID);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                SingleFilmReviewActivity.this.startActivity(intent);

            }
        });

        listSingleFilmReview.setOnLoadListener(new LoadMoreListView.OnLoadListener()
        {
            @Override
            public void onLoad(LoadMoreListView listView)
            {
                getFilmReviewList(filmId, ++curPage, limit);
                isLoadOrRefresh = false;
            }
        });
    }

    private void getFilmReviewList(String filmId, int page, final int limit)
    {
        apiRequestManager.getFilmReviewList(filmId, page, limit, new AsyncHttpHelper.ResultCallback<FilmReviewListInfo>()
        {
            @Override
            public void onSuccess(FilmReviewListInfo data)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingSuccess();
                if (isLoadOrRefresh) {
                    filmReviewDatas.clear();
                }
                ArrayList<FilmReviewListInfo.CinecismModel> cinecismList = data.cinecisms.list;
                int positiveSum = data.cinecisms.positiveNum;
                int negtiveSum = data.cinecisms.negativeNum;
                filmReviewDatas.addAll(cinecismList);
                filmReviewListAdapter.notifyDataSetChanged();

                if (cinecismList.size() == limit) {
                    listSingleFilmReview.onLoadSucess(true);
                }
                else {
                    listSingleFilmReview.onLoadSucess(false);
                }

            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                CommonManager.setRefreshingState(mRefreshLayout, false);//隐藏下拉刷新
                onActivityLoadingFailed();
                if (!isLoadOrRefresh) {
                    listSingleFilmReview.onLoadFailed();
                }
            }
        });
    }

    @Override
    public void doClick(View view)
    {
        this.finish();
    }

}
