package com.dym.film.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.adapter.base.BaseListAdapter;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/16
 */

/**
 * 今日热门的水平列表的List Adapter
 */
public class HotTagTodayAdapter extends BaseListAdapter<String>
{
    public final static String TAG = "HotTagTodayAdapter";
    public HotTagTodayAdapter(@NonNull Activity activity)
    {
        super(activity);
    }

    @Override
    protected int getItemViewId()
    {
        return R.layout.list_item_share_ticket_tag;
    }

    @Override
    protected BaseViewHolder getViewHolder(int pos, View root)
    {
        return new ViewHolder(pos, root);
    }

    private class ViewHolder extends BaseViewHolder
    {
        private TextView mMovieTagText = null;

        protected ViewHolder(int pos, View root)
        {
            super(pos, root);
        }

        @Override
        protected void bindView(String text)
        {
            if (mRootView == null) {
                return;
            }

            mMovieTagText = (TextView) mRootView.findViewById(R.id.movieTag);
            mMovieTagText.setText(text);
        }
    }
}
