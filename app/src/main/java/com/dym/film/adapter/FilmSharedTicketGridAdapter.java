package com.dym.film.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.adapter.base.CommonBaseAdapter;
import com.dym.film.adapter.base.ViewHolder;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.NetworkManager;

import java.util.List;

public class FilmSharedTicketGridAdapter extends CommonBaseAdapter<NetworkManager.SharedTicketRespModel>
{

    public FilmSharedTicketGridAdapter(Context context, List<NetworkManager.SharedTicketRespModel> mDatas, int itemLayoutId)
    {
        super(context, mDatas, itemLayoutId);

    }

    @Override
    public void convert(ViewHolder holder,NetworkManager.SharedTicketRespModel itemData,int position)
    {
        ImageView imgFilmSharedTicket = holder.getView(R.id.imgFilmSharedTicket);
        View viewFilmLike= holder.getView(R.id.viewFilmLike);

        String stubImageUrl= (String) itemData.stubImage.url;
        int opinion= itemData.opinion;
        ImageLoaderUtils.displayImage(stubImageUrl,imgFilmSharedTicket);
//        ImageLoader.getInstance().displayImage(stubImageUrl,imgFilmSharedTicket);
        if (opinion==1){
            viewFilmLike.setBackgroundResource(R.drawable.ic_is_worth_yellow);
        }else{
            viewFilmLike.setBackgroundResource(R.drawable.ic_is_not_worth_green);
        }
    }


}
