package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.controllers.MixController;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;

import java.util.ArrayList;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/1
 */
public class CriticFilmReviewAdapter extends SimpleRecyclerAdapter<NetworkManager.FilmReviewRespModel>
{
    private boolean mVisibleImage = false;
    private boolean mVisibleAuthor = false;

    public CriticFilmReviewAdapter(@NonNull Activity activity)
    {
        this(activity, true, true);
    }

    public CriticFilmReviewAdapter(@NonNull Activity activity,
                                   boolean visibleImage,
                                   boolean visibleAuthor) {
        super(activity);
        mVisibleImage = visibleImage;
        mVisibleAuthor = visibleAuthor;
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType)
    {
        return mLayoutInflater.inflate(R.layout.list_item_film_review, parent, false);
    }

    @Override
    public void onBindModelToView(SimpleViewHolder viewHolder, int position)
    {
        MixController.setupCriticFilmReview(getItem(position), viewHolder.itemView, mVisibleImage, mVisibleAuthor);
    }
}
