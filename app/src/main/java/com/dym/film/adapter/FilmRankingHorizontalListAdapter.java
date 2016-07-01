package com.dym.film.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.model.FilmListInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

@Deprecated
public class FilmRankingHorizontalListAdapter extends CommonBaseAdapter<FilmListInfo.FilmModel>
{

    public int type=0;//0按时间最新上映，1默认按月公证指数
    public FilmRankingHorizontalListAdapter(Context context, List<FilmListInfo.FilmModel> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);
    }
    public void setType(int type){
        this.type=type;
    }
    @Override
    public void convert(ViewHolder holder, FilmListInfo.FilmModel itemData, int position)
    {

        TextView tvFilmIndex=holder.getView(R.id.tvFilmIndex);
        ImageView imgFilmCover=holder.getView(R.id.imgFilmCover);
        TextView tvFilmName=holder.getView(R.id.tvFilmName);
        TextView tvFilmIndexUnit=holder.getView(R.id.tvFilmIndexUnit);
        View viewFilmNameBg=holder.getView(R.id.viewFilmNameBg);
//        ImageLoaderUtils.displayImage(itemData.post, imgFilmCover, DimenUtils.dip2px(mContext, 100), DimenUtils.dip2px(mContext, 130));
        ImageLoader.getInstance().displayImage(itemData.post,imgFilmCover);
        tvFilmName.setText(itemData.name);
        String dymIndex = itemData.dymIndex;
        if (dymIndex.equals("") || dymIndex.equals("0") || dymIndex.equals("0.0")) {
            tvFilmIndex.setText("   -");
            tvFilmIndexUnit.setVisibility(View.GONE);
            tvFilmIndex.setTextSize(25);
        }
        else {
            int index = (int) Float.parseFloat(dymIndex);
            tvFilmIndex.setText(index + "");
            tvFilmIndexUnit.setVisibility(View.VISIBLE);
            tvFilmIndex.setTextSize(25);
        }

        int colorId=0;
        int apha= (int) (255*(1-0.05*position));//5%递减
        if (type==0){
            colorId= Color.argb(apha,177,11,11);
            tvFilmName.setTextColor(Color.WHITE);
        }else {
            colorId= Color.argb(apha,255,255,255);
            tvFilmName.setTextColor(Color.BLACK);
        }
        viewFilmNameBg.setBackgroundColor(colorId);

    }

}
