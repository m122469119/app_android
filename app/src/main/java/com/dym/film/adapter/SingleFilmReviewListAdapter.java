package com.dym.film.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.model.FilmReviewListInfo;

import java.util.List;

public class SingleFilmReviewListAdapter extends CommonBaseAdapter<FilmReviewListInfo.CinecismModel>
{

    public SingleFilmReviewListAdapter(Context context, List<FilmReviewListInfo.CinecismModel> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void convert(ViewHolder holder, FilmReviewListInfo.CinecismModel cinecismModel, int position)
    {

        long cinecismID = cinecismModel.cinecismID;
        String title = cinecismModel.title;
        String srcMedia = cinecismModel.srcMedia;
        String srcUrl = cinecismModel.srcUrl;
        String srcScore = cinecismModel.srcScore;
        String createTime = cinecismModel.createTime;
        String filmPost = cinecismModel.logo;
        String summary = cinecismModel.summary;
        int opinion = cinecismModel.opinion;

        FilmReviewListInfo.Writer writer = cinecismModel.writer;
        long criticID = writer.criticID;
        String writerTitle = writer.title;
        String writerName = writer.name;
        String writerAvatar = writer.avatar;

        View imgFilmReviewPraise = holder.getView(R.id.imgFilmReviewPraise);
        TextView tvFilmReviewScore = holder.getView(R.id.tvFilmReviewScore);
        TextView tvFilmReviewTitle = holder.getView(R.id.tvFilmReviewTitle);
        TextView tvFilmReviewWriter = holder.getView(R.id.tvFilmReviewWriter);
        TextView tvWriterHonor = holder.getView(R.id.tvWriterHonor);
        TextView tvFilmReviewDate = holder.getView(R.id.tvFilmReviewDate);

        if (opinion == 1) {
            imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_is_worth_yellow);
        }
        else {
            imgFilmReviewPraise.setBackgroundResource(R.drawable.ic_is_not_worth_green);
        }
        tvFilmReviewScore.setText("FROM " + srcMedia);
//        if (srcScore.equals("0.0") || srcScore.equals("0")) {
//            tvFilmReviewScore.setText("FROM " + srcMedia);
//        }
//        else {
//            tvFilmReviewScore.setText("FROM " + srcMedia + " " + srcScore);
//        }
        if (TextUtils.isEmpty(summary)){
            tvFilmReviewTitle.setText(title);
        }else {
            tvFilmReviewTitle.setText(summary);
        }

        tvFilmReviewWriter.setText(writerName + " " );
//        CommonManager.setTime3(tvFilmReviewDate,createTime);
        int year=Integer.valueOf(createTime.substring(0,4));
        if (year<2016){
            tvFilmReviewDate.setText(createTime.substring(0,10));
        }else{
            tvFilmReviewDate.setText(createTime.substring(5,10));
        }

//        tvWriterHonor.setText(writerTitle);
    }

}
