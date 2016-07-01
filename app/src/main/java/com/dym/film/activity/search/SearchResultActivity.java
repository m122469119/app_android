package com.dym.film.activity.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.AllCriticRecyclerAdapter;
import com.dym.film.adapter.SearcResultAdapter;
import com.dym.film.adapter.SearchFilmReviewListAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.AttentionAuthor;
import com.dym.film.entity.DaoSession;
import com.dym.film.entity.FilmReview;
import com.dym.film.entity.Serchhistory;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.NetworkManager;
//import com.dym.film.manager.data.AllCriticDataManager;
import com.dym.film.ui.ProgressWheel;
import com.dym.film.ui.exrecyclerview.ExRcvAdapterWrapper;
import com.dym.film.utils.NetWorkUtils;
import com.dym.film.views.LoadMoreRecyclerView;

import java.util.ArrayList;

public class SearchResultActivity extends BaseActivity implements TextWatcher{

    private LoadMoreRecyclerView listview;
    private LoadMoreRecyclerView serch_result_list_review;
    private LoadMoreRecyclerView serch_result_list_author;
    private MySearchScrollView mySearchScrollView;
    private ExRcvAdapterWrapper adapterWrapper;
    private EditText serch_edittext;
    private ImageView serch_clear_icon_iv;
    private TextView serch_result_et;
    private TextView serch_result_et_review;
    private TextView serch_result_et_author;

    private SearcResultAdapter filmadapter;
    private    ArrayList<NetworkManager.SearchFilmModel> flimlist =null;

    private SearchFilmReviewListAdapter reviewadapter;
    private    ArrayList<NetworkManager.SearchReviewCinecismModel> reviewlist =null;

//    private AllCriticDataManager mDataManager = new AllCriticDataManager();
    private AllCriticRecyclerAdapter mCriticAdapter = null;
    private    ArrayList<NetworkManager.CriticRespModel> authorlist =null;
    private String key="";

    // 第一页
    private int intCurrentPage = 0;
    // 每页10项数据
    private final static int PAGE_SIZE = 20;
    private boolean isNextPageLoading = true;
    private NetworkManager networkManager = NetworkManager.getInstance();

    private ProgressWheel progress_dialog;

    private String TYPE="";

    public static void startSerchResultActivity(Activity activity,String key,String type){
        Intent it=new Intent(activity,SearchResultActivityNew.class);
        it.putExtra("key",key);
        it.putExtra(SearchActivity.SearchTYPE, type);
        activity.startActivity(it);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTranslucentStatusBar(R.color.main_title_color);
    }

    @Override
    protected int setLayoutView() {
        return R.layout.activity_serch_result;
    }

    @Override
    protected void initVariable() {
            key=getIntent().getStringExtra("key");
        if(TextUtils.isEmpty(key)){
            key="";
        }
        TYPE=getIntent().getStringExtra(SearchActivity.SearchTYPE);
    }


    @Override
    protected void findViews() {
        listview=$(R.id.serch_result_list);
        serch_result_list_review=$(R.id.serch_result_list_review);
        serch_result_list_author=$(R.id.serch_result_list_author);
        mySearchScrollView=$(R.id.mySearchScrollView);
        serch_edittext=$(R.id.serch_edittext);
        serch_clear_icon_iv=$(R.id.serch_clear_icon_iv);
        serch_result_et=$(R.id.serch_result_et);
        serch_result_et_review=$(R.id.serch_result_et_review);
        serch_result_et_author=$(R.id.serch_result_et_author);
        progress_dialog=$(R.id.progress_dialog);

        if(TYPE.equals(SearchActivity.FILM)){
            serch_edittext.setHint("输入影片名称");
        }else if(TYPE.equals(SearchActivity.REVIEW)){
            serch_edittext.setHint("输入影片名称");
        }else if(TYPE.equals(SearchActivity.AUTHOR)){
            serch_edittext.setHint("输入影评人");
        }
        serch_edittext.setText(key);
        serch_edittext.setSelection(key.length());
        serch_clear_icon_iv.setVisibility(View.VISIBLE);

        loadReviewInit();
        loadAuthorInit();
        loadInit();
    }

    @Override
    protected void initData() {
        flimlist =new ArrayList<NetworkManager.SearchFilmModel>();
        reviewlist=new ArrayList<>();
        authorlist=new ArrayList<>();
        startActivityLoading();
    }


    protected void loadInit() {
        filmadapter =new SearcResultAdapter(this);
//        StaggeredGridLayoutManager mStaggerLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        MyLinearLayoutManager mStaggerLayoutManager=new MyLinearLayoutManager(this);
        listview.setLayoutManager(mStaggerLayoutManager);
        listview.setLinearLayoutManager(mStaggerLayoutManager);
        // 设置ItemAnimator
        listview.setItemAnimator(new DefaultItemAnimator());
        adapterWrapper = new ExRcvAdapterWrapper<>(filmadapter, mStaggerLayoutManager);
        listview.setAdapter(adapterWrapper);
        // 设置固定大小
        listview.setHasFixedSize(true);
    }
    protected void loadReviewInit() {
        reviewadapter =new SearchFilmReviewListAdapter(this);
//        StaggeredGridLayoutManager mStaggerLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        MyLinearLayoutManager mStaggerLayoutManager=new MyLinearLayoutManager(this);
        serch_result_list_review.setLayoutManager(mStaggerLayoutManager);
        serch_result_list_review.setLinearLayoutManager(mStaggerLayoutManager);
        final Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.border_color));

        // 设置ItemAnimator
        serch_result_list_review.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
                final int left = parent.getPaddingLeft();
                final int right = parent.getWidth() - parent.getPaddingRight();

                final int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - adapterWrapper.getFooterCount(); i++) {
                    final View child = parent.getChildAt(i);
                    final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                    final int top = child.getBottom() + params.bottomMargin;
                    final int bottom = top + CommonManager.dpToPx(0.5f);
                    c.drawRect(left, top, right, bottom, paint);
                }
            }
        });
        serch_result_list_review.setItemAnimator(new DefaultItemAnimator());
        adapterWrapper = new ExRcvAdapterWrapper<>(reviewadapter, mStaggerLayoutManager);
        serch_result_list_review.setAdapter(adapterWrapper);
        // 设置固定大小
        serch_result_list_review.setHasFixedSize(true);
    }
    protected void loadAuthorInit() {
//        mDataManager = new AllCriticDataManager();
        mCriticAdapter = new AllCriticRecyclerAdapter(this);
        MyLinearLayoutManager mStaggerLayoutManager=new MyLinearLayoutManager(this);
        serch_result_list_author.setLayoutManager(mStaggerLayoutManager);
        serch_result_list_author.setLinearLayoutManager(mStaggerLayoutManager);
        serch_result_list_author.setItemAnimator(new DefaultItemAnimator());
        adapterWrapper = new ExRcvAdapterWrapper<>(mCriticAdapter, mStaggerLayoutManager);
        serch_result_list_author.setAdapter(adapterWrapper);
        // 设置固定大小
        serch_result_list_author.setHasFixedSize(true);
    }

    private void loadFilmData(boolean isFirst){
        if(isFirst){
            intCurrentPage = 0;
            flimlist.clear();
            loadInit();
            // 可以显示加载界面
            progress_dialog.setVisibility(View.VISIBLE);
        }
        networkManager.getSearchList(key, "", "", "", String.valueOf(intCurrentPage++), String.valueOf(PAGE_SIZE), new HttpRespCallback<NetworkManager.RespSearchFilmList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                onActivityLoadingSuccess();
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.RespSearchFilmList ms = (NetworkManager.RespSearchFilmList) msg.obj;
                String num = ms.result.sum;
                String keys = (key.length() > 15) ? key.substring(0, 14) + "..." : key;
                serch_result_et.setText("\"" + keys + "\"" + "共" + num + "个搜索结果");
                if (ms != null && ms.result.films != null && ms.result.films.size() > 0) {
                    int size = (ms.result.films == null ? 0 : ms.result.films.size());

                    filmadapter.appendAll(ms.result.films, true);
                    flimlist.addAll(ms.result.films);
                    adapterWrapper.setFooterView(listview.getLoadMoreFooterController().getFooterView());

                    if (size < PAGE_SIZE) {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    } else {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                    endRefresh(true);
                } else {
                    listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    if (flimlist.size() > 0) {
                        Toast.makeText(SearchResultActivity.this, "加载完毕", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchResultActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }
    private void loadReviewData(boolean isFirst){
        if(isFirst){
            intCurrentPage = 0;
            reviewlist.clear();
            loadReviewInit();
            // 可以显示加载界面
            progress_dialog.setVisibility(View.VISIBLE);
        }

        networkManager.getSearchReviewList(key, String.valueOf(intCurrentPage++), String.valueOf(PAGE_SIZE), new HttpRespCallback<NetworkManager.RespSearchReviewList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                onActivityLoadingSuccess();
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.RespSearchReviewList ms = (NetworkManager.RespSearchReviewList) msg.obj;
                String num = ms.result.sum;
                String keys = (key.length() > 15) ? key.substring(0, 14) + "..." : key;
                serch_result_et.setText("\"" + keys + "\"" + "共" + num + "个搜索结果");
                if (ms != null && ms.result.cinecisms != null && ms.result.cinecisms.size() > 0) {
                    int size = (ms.result.cinecisms == null ? 0 : ms.result.cinecisms.size());

                    reviewadapter.appendAll(ms.result.cinecisms, false);
                    reviewlist.addAll(ms.result.cinecisms);
                    adapterWrapper.setFooterView(listview.getLoadMoreFooterController().getFooterView());

                    if (size < PAGE_SIZE) {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    } else {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                    endRefresh(true);
                } else {
                    listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    if (reviewlist.size() > 0) {
                        Toast.makeText(SearchResultActivity.this, "加载完毕", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchResultActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }
    private void loadAuthorData(boolean isFirst){
        if(isFirst){
            intCurrentPage = 0;
            authorlist.clear();
            loadAuthorInit();
            // 可以显示加载界面
            progress_dialog.setVisibility(View.VISIBLE);
        }

        networkManager.getSearchAuthorList(key, String.valueOf(intCurrentPage++), String.valueOf(PAGE_SIZE), new HttpRespCallback<NetworkManager.RespSearchAuthorList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                onActivityLoadingSuccess();
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.RespSearchAuthorList ms = (NetworkManager.RespSearchAuthorList) msg.obj;
                String num = ms.result.sum;
                String keys = (key.length() > 15) ? key.substring(0, 14) + "..." : key;
                serch_result_et.setText("\"" + keys + "\"" + "共" + num + "个搜索结果");
                if (ms != null && ms.result.critics != null && ms.result.critics.size() > 0) {
                    int size = (ms.result.critics == null ? 0 : ms.result.critics.size());

                    mCriticAdapter.appendAll(ms.result.critics, false);
                    authorlist.addAll(ms.result.critics);
                    adapterWrapper.setFooterView(listview.getLoadMoreFooterController().getFooterView());

                    if (size < PAGE_SIZE) {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    } else {
                        listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
                    }
                    endRefresh(true);
                } else {
                    listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_NO_MORE);
                    if (authorlist.size() > 0) {
                        Toast.makeText(SearchResultActivity.this, "加载完毕", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SearchResultActivity.this, "暂无数据", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    private void loadSearchResult(){

        loadReviewInit();
        loadAuthorInit();
        loadInit();
        mySearchScrollView.scrollTo(0,0);
        serch_result_et.setVisibility(View.GONE);
        serch_result_et_review.setVisibility(View.GONE);
        serch_result_et_author.setVisibility(View.GONE);


        networkManager.getSearchResultList(key, new HttpRespCallback<NetworkManager.RespSearchResultList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                endRefresh(false);
                progress_dialog.setVisibility(View.GONE);
                onActivityLoadingSuccess();
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                onActivityLoadingSuccess();
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.RespSearchResultList ms = (NetworkManager.RespSearchResultList) msg.obj;

                if (ms != null ) {
                    int sizeflimlist = (ms.result.films == null ? 0 : ms.result.films.size());
                    int sizereviewlist = (ms.result.cinecisms == null ? 0 : ms.result.cinecisms.size());
                    int sizeauthorlist = (ms.result.critics == null ? 0 : ms.result.critics.size());


                    if(sizeflimlist!=0){
                        serch_result_et.setVisibility(View.VISIBLE);
                        filmadapter.setAll(ms.result.films);
                        flimlist.addAll(ms.result.films);
                    }
                    if(sizereviewlist!=0){
                        serch_result_et_review.setVisibility(View.VISIBLE);
//                        reviewadapter.setAll(ms.result.cinecisms);
//                        reviewlist.addAll(ms.result.cinecisms);
                    }
                    if(sizeauthorlist!=0){
                        serch_result_et_author.setVisibility(View.VISIBLE);
                        mCriticAdapter.setAll(ms.result.critics);
                        authorlist.addAll(ms.result.critics);
                    }



                }
            }
        });
    }

    private void endRefresh(boolean result)
    {
        isNextPageLoading = false;
        if (result) {
            listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_SUCCESS);
        }
        else {
            listview.loadMoreFinished(LoadMoreRecyclerView.LOAD_MORE_FAILED);
            if(!NetWorkUtils.isAvailable(this)){
                onActivityLoadingFailed();
                Toast.makeText(SearchResultActivity.this, "没有网络", Toast.LENGTH_SHORT).show();
            }
            onActivityLoadingFailed();
            listview.setFooterClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(!NetWorkUtils.isAvailable(SearchResultActivity.this)){
                        Toast.makeText(SearchResultActivity.this, "没有网络", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listview.startLoadMore();
                }
            });
        }
    }

    @Override
    protected void onActivityLoading() {
        super.onActivityLoading();
        loadSearchResult();

//        if(TYPE.equals(SearchActivity.FILM)){
//            loadFilmData(true);
//        }else if(TYPE.equals(SearchActivity.REVIEW)){
//            loadReviewData(true);
//        }else if(TYPE.equals(SearchActivity.AUTHOR)){
//            loadAuthorData(true);
//        }

    }

    private  void loadMore()
    {
        if (isNextPageLoading) {
            return;
        }
        isNextPageLoading = true;
        if(TYPE.equals(SearchActivity.FILM)){
            loadFilmData(false);
        }else if(TYPE.equals(SearchActivity.REVIEW)){
            loadReviewData(false);
        }else if(TYPE.equals(SearchActivity.AUTHOR)){
            loadAuthorData(false);
        }
    }


    @Override
    protected void setListener() {
        serch_edittext.addTextChangedListener(this);
        serch_edittext.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView arg0, int arg1,
                                          KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEARCH
                        || arg1 == EditorInfo.IME_ACTION_GO
                        || arg1 == EditorInfo.IME_ACTION_NEXT
                        || arg1 == EditorInfo.IME_ACTION_SEND
                        || arg1 == EditorInfo.IME_ACTION_DONE
                        || arg1 == EditorInfo.IME_NULL) {
                    String key1 = serch_edittext.getText().toString()
                            .trim();
                    // 去除空格
                    key = key1.trim();
                    if (!TextUtils.isEmpty(key)) {
                        DaoSession session = DatabaseManager.getInstance().getDaoSession();
//                        if(TYPE.equals(SearchActivity.FILM)){
//                            Serchhistory serch = new Serchhistory();
//                            serch.setMovie_id(key);
//                            serch.setMovie_name(key);
//                            session.getSerchhistoryDao().saveUserHistroy(serch);
//                            loadFilmData(true);
//                        }else if(TYPE.equals(SearchActivity.REVIEW)){
//                            FilmReview review = new FilmReview();
//                            review.setFilmReviewId(key);
//                            review.setFilmReviewDes(key);
//                            session.getFilmReviewDao().saveUserFilmReview(review);
//                            loadReviewData(true);
//                        }else if(TYPE.equals(SearchActivity.AUTHOR)){
//                            AttentionAuthor author = new AttentionAuthor();
//                            author.setAttentionAuthorId(key);
//                            author.setAttentionAuthorDes(key);
//                            session.getAttentionAuthorDao().saveUserAttentionAuthor(author);
//                            loadAuthorData(true);
//                        }
                            Serchhistory serch = new Serchhistory();
                            serch.setMovie_id(key);
                            serch.setMovie_name(key);
                            session.getSerchhistoryDao().saveUserHistroy(serch);
                        startActivityLoading();
                        loadSearchResult();
                        CommonManager.dismissSoftInputMethod(SearchResultActivity.this,serch_edittext.getWindowToken());
                    } else {
                        return false;
                    }

                }
                return true;
            }
        });
        listview.setLoadMoreListener(new LoadMoreRecyclerView.LoadMoreListener() {
            @Override
            public void onNeedLoadMore() {
                loadMore();
            }
        });

    }

    @Override
    public void doClick(View view) {
        switch (view.getId()){
            case R.id.serch_clear_icon_iv:
                serch_edittext.setText("");
                break;
            case R.id.serch_tv_cancel:
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(serch_edittext.getText().toString()!=null&&!serch_edittext.getText().toString().equals("")){
            serch_clear_icon_iv.setVisibility(View.VISIBLE);
        }else{
            serch_clear_icon_iv.setVisibility(View.GONE);
        }
    }
}
