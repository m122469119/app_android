package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.home.FilmDetailActivity;
import com.dym.film.activity.home.PreFilmDetailActivity;
import com.dym.film.adapter.base.BaseSimpleRecyclerAdapter;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;

/**
 * Created by xusoku on 2015/12/1.
 */
public class SearcResultAdapter extends BaseSimpleRecyclerAdapter<NetworkManager.SearchFilmModel, SearcResultAdapter.SearcResultViewHolder> {


    public SearcResultAdapter(@NonNull Activity activity) {
        super(activity);
    }
    @Override
    public SearcResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayout = LayoutInflater.from(mActivity)
                .inflate(R.layout.list_item_search_film_result, null);
        return new SearcResultViewHolder(itemLayout);
    }



    public class SearcResultViewHolder extends BaseSimpleRecyclerAdapter.BaseViewHolder {

        //        private View itemView;
        private ImageView iv;
        private View iv_play;
        private TextView serch_result_tvFilmName;
        private TextView serch_result_tvFilmDirector;
        private TextView serch_result_tvFilmActor;
        private TextView serch_result_yinmu;
        private TextView serch_result_piaogen;
        private TextView serch_result_tvFilmRegion;
        private TextView serch_result_tv_month;
        private TextView serch_result_tv_year;

        public SearcResultViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) findView(R.id.serch_result_imgFilmCover_iv);
            iv_play = (View) findView(R.id.serch_result_play);
            iv_play.setVisibility(View.GONE);
            serch_result_tvFilmName = (TextView) findView(R.id.serch_result_tvFilmName);
            serch_result_tvFilmRegion = (TextView) findView(R.id.serch_result_tvFilmRegion);
            serch_result_tvFilmDirector = (TextView) findView(R.id.serch_result_tvFilmDirector);
            serch_result_tvFilmActor = (TextView) findView(R.id.serch_result_tvFilmActor);
            serch_result_yinmu = (TextView) findView(R.id.serch_result_yinmu);
            serch_result_piaogen = (TextView) findView(R.id.serch_result_piaogen);
            serch_result_tv_month = (TextView) findView(R.id.serch_result_tv_month);
            serch_result_tv_year = (TextView) findView(R.id.serch_result_tv_year);
        }

        @Override
        public void bindModelToView(int position) {

            final NetworkManager.SearchFilmModel model = getItem(position);
            String name = model.name;
            if (TextUtils.isEmpty(name)) {
                name = "暂无";
            }
            String country = model.country;
            if (TextUtils.isEmpty(country)) {
                country = "暂无";
            }
            String director = model.director;
            if (TextUtils.isEmpty(director)) {
                director = "暂无";
            }
            String cast = model.cast;
            if (TextUtils.isEmpty(cast)) {
                cast = "暂无";
            }
            String releaseDate = model.releaseDate;
            if (TextUtils.isEmpty(releaseDate)) {
                releaseDate = "0";
            }

            String dymIndex = model.dymIndex;
            if (TextUtils.isEmpty(dymIndex) || dymIndex.equals("0") || dymIndex.equals("0.0")) {
                dymIndex = "  暂无";
            }

            String stubIndex = model.stubIndex;
            if (TextUtils.isEmpty(stubIndex) || stubIndex.equals("0") || stubIndex.equals("0.0")) {
                stubIndex = "  暂无";
            }
            final String status = model.status;

            serch_result_tvFilmName.setText(name);
            CommonManager.displayImage(model.post, iv, R.drawable.ic_default_loading_img);
//        ImageLoaderConfigUtil.setDisplayImager(R.drawable.ic_default_loading_img, holder.iv, model.post, false);
//        ImageLoader.getInstance().displayImage(model.post, holder.iv);
            serch_result_tvFilmDirector.setText(director);
            serch_result_tvFilmActor.setText(cast);
            serch_result_tvFilmRegion.setText(country + " / " + releaseDate);
            serch_result_yinmu.setText(dymIndex.trim().equals("暂无") ? dymIndex : dymIndex + "%");
            serch_result_piaogen.setText(stubIndex.trim().equals("暂无") ? stubIndex : stubIndex + "%");

//        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            long time = sf.parse(releaseDate).getTime();
//            CommonManager.setTime( holder.serch_result_tv_year, holder.serch_result_tv_month, time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (status.equals("1")) {
                        Intent it = new Intent(mActivity, PreFilmDetailActivity.class);
                        it.putExtra(FilmDetailActivity.KEY_FILM_ID, model.filmID);
                        mActivity.startActivity(it);
                    } else {
                        Intent it = new Intent(mActivity, FilmDetailActivity.class);
                        it.putExtra(FilmDetailActivity.KEY_FILM_ID, model.filmID);
                        mActivity.startActivity(it);
                    }
                }
            });
        }


    }
}
