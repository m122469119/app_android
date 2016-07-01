package com.dym.film.activity.search;

import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.entity.AttentionAuthor;
import com.dym.film.entity.DaoSession;
import com.dym.film.entity.FilmReview;
import com.dym.film.entity.Serchhistory;
import com.dym.film.manager.DatabaseManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.views.StretchedListView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements TextWatcher,AdapterView.OnItemClickListener {

    private GridView serch_gridview;
    private StretchedListView serch_listview;
    private TextView serch_all_btn;
    private EditText serch_edittext;
    private ImageView serch_clear_icon_iv;

    private ArrayList<String> listTags =null;
    private  List<Serchhistory> listFilm =null;
    private  List<FilmReview> listReview =null;
    private  List<AttentionAuthor> listAuthor =null;
    private TextView customStatusBarView;
    private NetworkManager networkManager=NetworkManager.getInstance();

    // FILM REVIEW AUTHOR
    public static String FILM="FILM";
    public static String REVIEW="REVIEW";
    public static String AUTHOR="AUTHOR";
    public static String SearchTYPE="SearchTYPE";
    private String TYPE="FILM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTranslucentStatusBar(R.color.main_title_color);
    }
    @Override
    protected int setLayoutView() {
        return R.layout.activity_serch;
    }

    @Override
    protected void initVariable() {
        TYPE=getIntent().getStringExtra(SearchTYPE);
        TYPE="FILM";
        listTags=new ArrayList<>();
    }

    @Override
    protected void findViews() {
        serch_gridview=$(R.id.serch_gridview);
        serch_listview=$(R.id.serch_listview);
        serch_edittext=$(R.id.serch_edittext);
        serch_all_btn=$(R.id.serch_all_btn);
        serch_clear_icon_iv=$(R.id.serch_clear_icon_iv);

    }


    @Override
    protected void onResume() {
        super.onResume();
        Refused(3);
    }

    @Override
    protected void initData() {
        if(TYPE.equals(FILM)){
//            serch_edittext.setHint("输入影片名称");
            initFilm();
        }else if(TYPE.equals(REVIEW)){
//            serch_edittext.setHint("输入影片名称");
            initReview();
        }else if(TYPE.equals(AUTHOR)){
//            serch_edittext.setHint("输入影评人");
            initAuthor();
        }

    }

    private void initFilm() {
        networkManager.getSearchFilmHotList(new HttpRespCallback<NetworkManager.RespSearchFilmHotList>() {
            @Override
            public void onRespFailure(int code, String msg) {
            }
            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespSearchFilmHotList list = (NetworkManager.RespSearchFilmHotList) msg.obj;
                if (list != null && list.films != null && list.films.size() > 0) {
                    listTags.addAll(list.films);
                    serch_gridview.setAdapter(new CommonBaseAdapter<String>(SearchActivity.this, list.films, R.layout.hot_serch_item) {
                        @Override
                        public void convert(ViewHolder holder, String itemData, int position) {
                            holder.setText(R.id.hot_serch_item_tv, itemData);
                        }
                    });
                }
            }
        });
    }
    private void initReview() {
        networkManager.getSearchFilmHotList(new HttpRespCallback<NetworkManager.RespSearchFilmHotList>() {
            @Override
            public void onRespFailure(int code, String msg) {
            }

            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespSearchFilmHotList list = (NetworkManager.RespSearchFilmHotList) msg.obj;
                if (list != null && list.films != null && list.films.size() > 0) {
                    listTags.addAll(list.films);
                    serch_gridview.setAdapter(new CommonBaseAdapter<String>(SearchActivity.this, list.films, R.layout.hot_serch_item) {
                        @Override
                        public void convert(ViewHolder holder, String itemData, int position) {
                            holder.setText(R.id.hot_serch_item_tv, itemData);
                        }
                    });
                }
            }
        });
    }
    private void initAuthor() {
        networkManager.getSearchReviewHotList(new HttpRespCallback<NetworkManager.RespSearchReviewHotList>() {
            @Override
            public void onRespFailure(int code, String msg) {
            }
            @Override
            protected void runOnMainThread(Message msg) {
                super.runOnMainThread(msg);
                NetworkManager.RespSearchReviewHotList list = (NetworkManager.RespSearchReviewHotList) msg.obj;
                if (list != null && list.critics != null && list.critics.size() > 0) {
                    listTags.addAll(list.critics);
                    serch_gridview.setAdapter(new CommonBaseAdapter<String>(SearchActivity.this, list.critics, R.layout.hot_serch_item) {
                        @Override
                        public void convert(ViewHolder holder, String itemData, int position) {
                            holder.setText(R.id.hot_serch_item_tv, itemData);
                        }
                    });
                }
            }
        });
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
                    String key = serch_edittext.getText().toString()
                            .trim();
                    // 去除空格
                    key = key.trim();
                    if (!TextUtils.isEmpty(key)) {
                        DaoSession session = DatabaseManager.getInstance().getDaoSession();
                        if(TYPE.equals(FILM)){
                            Serchhistory serch = new Serchhistory();
                            serch.setMovie_id(key);
                            serch.setMovie_name(key);
                            session.getSerchhistoryDao().saveUserHistroy(serch);
                        }else if(TYPE.equals(REVIEW)){
                            FilmReview review = new FilmReview();
                            review.setFilmReviewId(key);
                            review.setFilmReviewDes(key);
                            session.getFilmReviewDao().saveUserFilmReview(review);
                        }else if(TYPE.equals(AUTHOR)){
                            AttentionAuthor author = new AttentionAuthor();
                            author.setAttentionAuthorId(key);
                            author.setAttentionAuthorDes(key);
                            session.getAttentionAuthorDao().saveUserAttentionAuthor(author);
                        }
                        Refused(3);
                        SearchResultActivityNew.startSerchResultActivity(SearchActivity.this, key, TYPE);
                        finish();
                    } else {
                        return false;
                    }

                }
                return true;
            }
        });

        serch_gridview.setOnItemClickListener(this);
        serch_listview.setOnItemClickListener(this);
    }

    private boolean offonFlag=true;
    @Override
    public void doClick(View view) {

        switch (view.getId()){
            case R.id.serch_clear_icon_iv:
                serch_edittext.setText("");
                break;
            case R.id.serch_tv_cancel:
               finish();
                break;
            case R.id.serch_all_btn:
                offonFlag=!offonFlag;
                if(offonFlag) {
                    Refused(-1);
                }else {
                    Refused(3);
                }
                break;
        }
    }

    /**
     * 时时获取文本输入框字符长度
     */
    private CharSequence temp;
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        temp = s;
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(serch_edittext.getText().toString()!=null&&!serch_edittext.getText().toString().equals("")){
            serch_clear_icon_iv.setVisibility(View.VISIBLE);
        }else{
            serch_clear_icon_iv.setVisibility(View.GONE);
        }
    }

    public void Refused(int i){
        DaoSession session = DatabaseManager.getInstance().getDaoSession();

        if(TYPE.equals(FILM)){

            listFilm =session.getSerchhistoryDao().getHistroyList(session.getSerchhistoryDao(),i);
            if(listFilm.size()>20){
                session.getSerchhistoryDao().delectHistroyList(session.getSerchhistoryDao());
            }
            serch_listview.setAdapter(new CommonBaseAdapter<Serchhistory>(this, listFilm, R.layout.list_item_search_all_histroy) {
                @Override
                public void convert(ViewHolder holder, Serchhistory itemData, int position) {
                    holder.setText(R.id.hot_serch_item_tv, itemData.getMovie_name());
                    DeleteData(holder, itemData);
                }
            });
            if(i==-1){
                serch_all_btn.setText("关闭搜索历史");
            }else{
                serch_all_btn.setText("全部搜索历史");
            }
            serch_all_btn.setVisibility(View.VISIBLE);
            List<Serchhistory> list1=session.getSerchhistoryDao().getHistroyList(session.getSerchhistoryDao(),-1);
            if(listFilm ==null|| listFilm.size()==0||list1.size()<=3){
                serch_all_btn.setVisibility(View.GONE);
            }
        }else if(TYPE.equals(REVIEW)){
            listReview =session.getFilmReviewDao().getFilmReviewList(session.getFilmReviewDao(), i);
            if(listReview.size()>20){
                session.getFilmReviewDao().delectFilmReviewList(session.getFilmReviewDao());
            }
            serch_listview.setAdapter(new CommonBaseAdapter<FilmReview>(this, listReview, R.layout.list_item_search_all_histroy) {
                @Override
                public void convert(ViewHolder holder, FilmReview itemData, int position) {
                    holder.setText(R.id.hot_serch_item_tv, itemData.getFilmReviewDes());
                    DeleteReciewData(holder, itemData);
                }
            });
            if(i==-1){
                serch_all_btn.setText("关闭搜索历史");
            }else{
                serch_all_btn.setText("全部搜索历史");
            }
            serch_all_btn.setVisibility(View.VISIBLE);
            List<FilmReview> list1=session.getFilmReviewDao().getFilmReviewList(session.getFilmReviewDao(), -1);
            if(listReview ==null|| listReview.size()==0||list1.size()<=3){
                serch_all_btn.setVisibility(View.GONE);
            }
        }else if(TYPE.equals(AUTHOR)){
            listAuthor =session.getAttentionAuthorDao().getAttentionAuthorList(session.getAttentionAuthorDao(), i);
            if(listAuthor.size()>20){
                session.getAttentionAuthorDao().delectAttentionAuthorList(session.getAttentionAuthorDao());
            }
            serch_listview.setAdapter(new CommonBaseAdapter<AttentionAuthor>(this, listAuthor, R.layout.list_item_search_all_histroy) {
                @Override
                public void convert(ViewHolder holder, AttentionAuthor itemData, int position) {
                    holder.setText(R.id.hot_serch_item_tv, itemData.getAttentionAuthorDes());
                    DeleteAuthorData(holder, itemData);
                }
            });
            if(i==-1){
                serch_all_btn.setText("关闭搜索历史");
            }else{
                serch_all_btn.setText("全部搜索历史");
            }
            serch_all_btn.setVisibility(View.VISIBLE);
            List<AttentionAuthor> list1=session.getAttentionAuthorDao().getAttentionAuthorList(session.getAttentionAuthorDao(), -1);
            if(listAuthor ==null|| listAuthor.size()==0||list1.size()<=3){
                serch_all_btn.setVisibility(View.GONE);
            }
        }

    }

    public void DeleteData(ViewHolder holder, final Serchhistory itemData){
        holder.getView(R.id.serch_item_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoSession session = DatabaseManager.getInstance().getDaoSession();
                session.getSerchhistoryDao().delete(itemData);
                Refused(3);
            }
        });
    }
    public void DeleteReciewData(ViewHolder holder, final FilmReview itemData){
        holder.getView(R.id.serch_item_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoSession session = DatabaseManager.getInstance().getDaoSession();
                session.getFilmReviewDao().delete(itemData);
                Refused(3);
            }
        });
    }
    public void DeleteAuthorData(ViewHolder holder, final AttentionAuthor itemData){
        holder.getView(R.id.serch_item_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DaoSession session = DatabaseManager.getInstance().getDaoSession();
                session.getAttentionAuthorDao().delete(itemData);
                Refused(3);
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String key="";
        if(parent instanceof GridView){
            key= listTags.get(position);
        }else{
            if(TYPE.equals(FILM)){
            key= listFilm.get(position).getMovie_name();
            }else if(TYPE.equals(REVIEW)){
                key= listReview.get(position).getFilmReviewDes();
            }else if(TYPE.equals(AUTHOR)){
                key= listAuthor.get(position).getAttentionAuthorDes();
            }
        }
        DaoSession session = DatabaseManager.getInstance().getDaoSession();
        if(TYPE.equals(FILM)){
            Serchhistory serch = new Serchhistory();
            serch.setMovie_id(key);
            serch.setMovie_name(key);
            session.getSerchhistoryDao().saveUserHistroy(serch);
        }else if(TYPE.equals(REVIEW)){
            FilmReview review = new FilmReview();
            review.setFilmReviewId(key);
            review.setFilmReviewDes(key);
            session.getFilmReviewDao().saveUserFilmReview(review);
        }else if(TYPE.equals(AUTHOR)){
            AttentionAuthor author = new AttentionAuthor();
            author.setAttentionAuthorId(key);
            author.setAttentionAuthorDes(key);
            session.getAttentionAuthorDao().saveUserAttentionAuthor(author);
        }
        SearchResultActivityNew.startSerchResultActivity(SearchActivity.this, key, TYPE);
        finish();
    }
}
