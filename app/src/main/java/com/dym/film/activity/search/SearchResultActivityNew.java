package com.dym.film.activity.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.SearchResultAdapter;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.DaoSession;
import com.dym.film.entity.Serchhistory;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.ui.ProgressWheel;

import java.util.ArrayList;
import java.util.Objects;

//import com.dym.film.manager.data.AllCriticDataManager;

public class SearchResultActivityNew extends BaseActivity implements TextWatcher{

    private ListView listview;
    private EditText serch_edittext;
    private ImageView serch_clear_icon_iv;
    private TextView serch_result_no_data;

    private SearchResultAdapter adapter;

    private ArrayList<ArrayList<NetworkManager.BaseRespModel>> mListItemData;
    private ArrayList<String> mListGroupData;


    private    ArrayList<NetworkManager.BaseRespModel> flimlist =null;

    private    ArrayList<NetworkManager.BaseRespModel> reviewlist =null;

    private    ArrayList<NetworkManager.BaseRespModel> authorlist =null;
    private String key="";

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
        return R.layout.activity_search_result;
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
        listview=$(R.id.mySearchList);
        serch_result_no_data=$(R.id.serch_result_no_data);
        serch_edittext=$(R.id.serch_edittext);
        serch_clear_icon_iv=$(R.id.serch_clear_icon_iv);
        progress_dialog=$(R.id.progress_dialog);

        if(TYPE.equals(SearchActivity.FILM)){
//            serch_edittext.setHint("输入影片名称");
        }else if(TYPE.equals(SearchActivity.REVIEW)){
//            serch_edittext.setHint("输入影片名称");
        }else if(TYPE.equals(SearchActivity.AUTHOR)){
//            serch_edittext.setHint("输入影评人");
        }
        serch_edittext.setText(key);
        serch_edittext.setSelection(key.length());
        serch_clear_icon_iv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initData() {
        flimlist =new ArrayList<>();
        reviewlist=new ArrayList<>();
        authorlist=new ArrayList<>();
        startActivityLoading();
    }

    private void loadSearchResult(){


        serch_result_no_data.setVisibility(View.GONE);
       networkManager.getSearchResultList(key, new HttpRespCallback<NetworkManager.RespSearchResultList>() {
            @Override
            public void onRespFailure(int code, String msg) {
                progress_dialog.setVisibility(View.GONE);
                serch_result_no_data.setVisibility(View.GONE);
                onActivityLoadingFailed();
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                onActivityLoadingSuccess();
                progress_dialog.setVisibility(View.GONE);
                NetworkManager.BaseRespModel msa = (NetworkManager.RespSearchResultList) msg.obj;
                NetworkManager.RespSearchResultList ms = (NetworkManager.RespSearchResultList) msg.obj;

                if (ms != null ) {
                    int sizeflimlist = (ms.result.films == null ? 0 : ms.result.films.size());
                    int sizereviewlist = (ms.result.cinecisms == null ? 0 : ms.result.cinecisms.size());
                    int sizeauthorlist = (ms.result.critics == null ? 0 : ms.result.critics.size());
                    mListItemData=new ArrayList<ArrayList<NetworkManager.BaseRespModel>>();
                    mListGroupData=new ArrayList<String>();

                    if(sizeflimlist!=0){
                        flimlist.clear();
                        flimlist.addAll(ms.result.films);
                        mListGroupData.add("电影");
                        mListItemData.add(flimlist);
                    }
                    if(sizeauthorlist!=0){
                        authorlist.clear();
                        authorlist.addAll(ms.result.critics);
                        mListGroupData.add("影评人");
                        mListItemData.add(authorlist);
                    }
                    if(sizereviewlist!=0){
                        reviewlist.clear();
                        reviewlist.addAll(ms.result.cinecisms);
                        mListGroupData.add("影评");
                        mListItemData.add(reviewlist);
                    }

                    if(mListGroupData.size()==0){
                        serch_result_no_data.setVisibility(View.VISIBLE);
                    }
                    adapter=new SearchResultAdapter(SearchResultActivityNew.this,mListItemData,mListGroupData);
                    listview.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    protected void onActivityLoading() {
        super.onActivityLoading();
        loadSearchResult();
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
//                        loadSearchResult();
                        CommonManager.dismissSoftInputMethod(SearchResultActivityNew.this,serch_edittext.getWindowToken());
                    } else {
                        return false;
                    }

                }
                return true;
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
