package com.dym.film.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.FilmVideoPostInfo;
import com.dym.film.utils.DimenUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class FilmVideoPostListAdapter extends CommonBaseAdapter<FilmVideoPostInfo>
{

    public int type=0;//0按时间最新上映，1默认按月公证指数
    public FilmVideoPostListAdapter(Context context, List<FilmVideoPostInfo> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);
    }
    public void setType(int type){
        this.type=type;
    }
    @Override
    public void convert(ViewHolder holder, FilmVideoPostInfo itemData, int position)
    {
        ImageView imgFilmPost=holder.getView(R.id.imgFilmPost);
        View imgFilmPlay=holder.getView(R.id.imgFilmPlay);
        if (itemData.type==0){
            imgFilmPlay.setVisibility(View.VISIBLE);
        }else{
            imgFilmPlay.setVisibility(View.INVISIBLE);
        }
        String url = QCloudManager.urlImage1(itemData.postUrl, DimenUtils.dp2px(mContext,75),  DimenUtils.dp2px(mContext,75));
        ImageLoader.getInstance().displayImage(url,imgFilmPost);

    }

}
