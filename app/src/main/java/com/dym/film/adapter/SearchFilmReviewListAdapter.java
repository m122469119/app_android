package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;

public class SearchFilmReviewListAdapter extends SimpleRecyclerAdapter<NetworkManager.SearchReviewCinecismModel> {


    public SearchFilmReviewListAdapter(@NonNull Activity activity) {
        super(activity);
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType) {
        View itemLayout = LayoutInflater.from(mActivity)
                .inflate(R.layout.list_item_single_film_review, null);
        itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return itemLayout;
    }

    @Override
    public void onBindModelToView(SimpleViewHolder holder, int position) {

        View imgFilmReviewPraise = holder.findView(R.id.imgFilmReviewPraise);
        TextView tvFilmReviewScore = (TextView) holder.findView(R.id.tvFilmReviewScore);
        TextView tvFilmReviewTitle = (TextView) holder.findView(R.id.tvFilmReviewTitle);
        TextView tvFilmReviewWriter = (TextView) holder.findView(R.id.tvFilmReviewWriter);
        TextView tvWriterHonor = (TextView) holder.findView(R.id.tvWriterHonor);
        tvWriterHonor.setVisibility(View.GONE);
        TextView tvFilmReviewDate = (TextView) holder.findView(R.id.tvFilmReviewDate);


        NetworkManager.SearchReviewCinecismModel cinecismModel = getItem(position);
       final String cinecismID = cinecismModel.cinecismID;
        String title = cinecismModel.title;
        String srcMedia = cinecismModel.srcMedia;
        String srcUrl = cinecismModel.srcUrl;
        String srcScore = cinecismModel.srcScore;
        String createTime = cinecismModel.createTime;
        String filmPost = cinecismModel.logo;
        String opinion = cinecismModel.opinion;

        NetworkManager.SearchReviewWriterModel writer = cinecismModel.writer;
        String criticID = writer.criticID;
        String writerTitle = writer.title;
        String writerName = writer.name;
        String writerAvatar = writer.avatar;

       ArrayList<NetworkManager.SearchReviewFilmModel> filmlist =cinecismModel.film;
        String film_name=filmlist.get(0).name;


        if (opinion.equals("1")) {
            imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_good_movie2);
        } else {
            imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_bad_movie2);
        }
        if (srcScore.equals("0.0") || srcScore.equals("0")) {
            tvFilmReviewScore.setText("FROM " + srcMedia);
        } else {
            tvFilmReviewScore.setText("FROM " + srcMedia + " " + srcScore);
        }


        tvFilmReviewTitle.setText(title);
        tvFilmReviewWriter.setText("《"+film_name.trim()+"》 "+writerName + " ");
//        CommonManager.setTime3(tvFilmReviewDate,createTime);
        tvFilmReviewDate.setText(createTime.substring(0, 10));
        tvWriterHonor.setText(writerTitle);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mActivity, FilmReviewDetailActivity.class);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, Long.parseLong(cinecismID));
                mActivity.startActivity(intent);
            }
        });

    }


}
