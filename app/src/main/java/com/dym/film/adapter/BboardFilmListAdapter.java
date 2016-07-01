package com.dym.film.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.model.FilmBboardListInfo;
import com.dym.film.utils.DimenUtils;
import com.dym.film.views.CenterRoundDisplayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class BboardFilmListAdapter extends BaseAdapter
{

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final List<FilmBboardListInfo.BillboardModel> mDatas;
    private final int mItemLayoutId;

    public BboardFilmListAdapter(Context context, List<FilmBboardListInfo.BillboardModel> mDatas, int itemLayoutId){
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }
    @Override
    public int getCount()
    {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder=null;
        if (convertView==null){
            convertView=mInflater.inflate(mItemLayoutId,null);
            holder=new ViewHolder();
            holder.imgRankingCover= (ImageView) convertView.findViewById(R.id.imgRankingCover);
            holder.tvRankingTitle= (TextView) convertView.findViewById(R.id.tvRankingTitle);
            convertView.setTag(holder);
        }
        holder= (ViewHolder) convertView.getTag();
        holder.imgRankingCover.getLayoutParams().height=DimenUtils.getScreenWidth(mContext)/2;
        FilmBboardListInfo.BillboardModel billboardModel=  mDatas.get(position);
        holder.tvRankingTitle.setText(billboardModel.title);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new CenterRoundDisplayer(DimenUtils.dp2px(mContext,6)))
                .showImageForEmptyUri(R.drawable.ic_default_loading_img)
                .build();
        ImageLoader.getInstance().displayImage(billboardModel.logo, holder.imgRankingCover,options);
        return convertView;
    }

    private class ViewHolder{
        public ImageView imgRankingCover;
        public TextView tvRankingTitle;
    }
}
