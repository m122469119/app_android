package com.dym.film.controllers;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/28
 */
public class MixController
{
    /**
     * 设置影评态度的区域
     * @param attLayout
     * @param opinion
     * @param res
     */
    public static void setupAttLayout(View attLayout, int opinion, String res) {
        if (attLayout == null || attLayout.getId() != R.id.attLayout) {
            throw new IllegalArgumentException("Need layout_att_film");
        }

        ImageView attImage = (ImageView) attLayout.findViewById(R.id.attImage);
        TextView film = (TextView) attLayout.findViewById(R.id.attFilm);

        film.setText(res);

        if (opinion == 1) {
            attLayout.setBackgroundResource(R.drawable.bg_support_border);
            attImage.setImageResource(R.drawable.ic_is_worth_white);
            attImage.setColorFilter(Color.parseColor("#F5A623"));
            film.setTextColor(Color.parseColor("#F5A623"));
        }
        else {
            attLayout.setBackgroundResource(R.drawable.bg_not_support_border);
            attImage.setImageResource(R.drawable.ic_is_not_worth);
            attImage.setColorFilter(Color.parseColor("#31AC86"));
            film.setTextColor(Color.parseColor("#31AC86"));
        }
    }

    public static void setupCriticFilmReview(NetworkManager.FilmReviewRespModel review,
                                             View itemView,
                                             boolean visibleImage,
                                             boolean visibleAuthor) {
        ArrayList<NetworkManager.FilmRespModel> films = review.film;

        NetworkManager.FilmRespModel film1 = films == null || films.isEmpty() ? null : films.get(0);
        NetworkManager.FilmRespModel film2 = films == null || films.size() < 2 ? null : films.get(1);
        /**
         * 加载影评logo
         */
        ImageView imageView = (ImageView) itemView.findViewById(R.id.filmReviewImage);
        if (visibleImage) {
            imageView.setVisibility(View.VISIBLE);
            CommonManager.displayReviewLogo(film1 == null ? "" : film1.post, imageView);
        }
        else {
            imageView.setVisibility(View.GONE);
        }

        /**
         * 设置来源
         */
        TextView textView = (TextView) itemView.findViewById(R.id.fromResourceText);
        textView.setText(review.srcMedia);

        /**
         * 设置评分
         */
        TextView scoreText = (TextView) itemView.findViewById(R.id.resourceScoreText);
        scoreText.setVisibility(View.INVISIBLE);
//        float score = 0;
//        try {
//            score = Float.valueOf(review.srcScore);
//        }
//        catch (Exception e) {
//            score = 0;
//        }
//
//        if (score > 0) {
//            scoreText.setText(review.srcScore);
//            scoreText.setVisibility(View.VISIBLE);
//        }
//        else {
//            scoreText.setVisibility(View.GONE);
//        }

        /**
         * 设置内容
         */
        TextView contentText = (TextView) itemView.findViewById(R.id.filmReviewContentText);
        contentText.setText(review.title);

        /**
         * 设置影评人名字
         */
        TextView writerName = (TextView) itemView.findViewById(R.id.filmReviewerName);
        if (visibleAuthor) {
            writerName.setVisibility(View.VISIBLE);
            writerName.setText(review.writer.name);
        }
        else {
            writerName.setVisibility(View.GONE);
        }

        /**
         * 设置评价的电影名字
         */
        String res = "";
        if (film1 != null) {
            res += film1.name;
        }
        if (film2 != null) {
            res += ", " + film2.name;
        }
        MixController.setupAttLayout(itemView.findViewById(R.id.attLayout), review.opinion, res);

        /**
         * 设置时间
         */
        TextView time = (TextView) itemView.findViewById(R.id.timeText);
        CommonManager.setTime(time, review.createTime == null ? 0 : review.createTime.getTime());


        /**
         * 设置item点击事件
         */
        final long id = review.cinecismID;
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), FilmReviewDetailActivity.class);
                intent.putExtra(FilmReviewDetailActivity.KEY_FILM_REVIEW_DATA, id);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                v.getContext().startActivity(intent);
            }
        });
    }
}
