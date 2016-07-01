package com.dym.film.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.sharedticket.TagSharedTicketActivity;
import com.dym.film.adapter.base.RecyclingPagerAdapter;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.ImageLoaderUtils;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.TagTextView;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by wbz360 on 2015/11/19.
 */
@Deprecated
public class MyShareTecketImageAdapter extends RecyclingPagerAdapter
{
    LayoutInflater mInflater;
    ArrayList<NetworkManager.SharedTicketRespModel> shareTicketDatas;
    Context context;
    private ShareManager shareManager;
    public MyShareTecketImageAdapter(Context context,ArrayList<NetworkManager.SharedTicketRespModel> shareTicketDatas){
        this.context=context;
        mInflater=LayoutInflater.from(context);
        this.shareTicketDatas=shareTicketDatas;
        shareManager=new ShareManager(context);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup container)
    {
        if (convertView==null){
            convertView=mInflater.inflate(R.layout.layout_my_shared_ticket_image_view,null);
            ViewHolder viewHolder=new ViewHolder();
            viewHolder.imgMySharedTicket= (PhotoView) convertView.findViewById(R.id.imgMySharedTicket);
            viewHolder.tvMySharedTicketContent= (TagTextView) convertView.findViewById(R.id.tvMySharedTicketContent);
            viewHolder.tvPraiseCount= (TextView) convertView.findViewById(R.id.tvPraiseCount);
            viewHolder.btnShareMySharedTicket= (TextView) convertView.findViewById(R.id.btnShareMySharedTicket);
            viewHolder.btnShareMySharedTicket.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    NetworkManager.SharedTicketRespModel itemData= (NetworkManager.SharedTicketRespModel) v.getTag();
                    shareManager.setTitle("公证电影｜看电影晒票根");
                    shareManager.setTitleUrl(ConfigInfo.BASE_URL + (String) itemData.shareUrl);
                    shareManager.setText((String) itemData.content);
                    shareManager.setImageUrl((String) itemData.stubImage.url);
                    shareManager.setWebUrl(ConfigInfo.BASE_URL + (String) itemData.shareUrl);
                    shareManager.showShareDialog(context);
                }
            });
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder= (ViewHolder) convertView.getTag();
        NetworkManager.SharedTicketRespModel itemData=shareTicketDatas.get(position);
        viewHolder.btnShareMySharedTicket.setTag(itemData);
        //下面填充数据
        ImageLoaderUtils.displayImage((String) itemData.stubImage.url, viewHolder.imgMySharedTicket);
        viewHolder.tvMySharedTicketContent.setTagClickListener(new TagTextView.TagClickListener()
        {
            @Override
            public void onTagClicked(String tag)
            {
                LogUtils.i("123", "tag" + tag);
                Intent intent = new Intent(context, TagSharedTicketActivity.class);
                intent.putExtra(TagSharedTicketActivity.KEY_TAG, tag);
                context.startActivity(intent);
            }
        });

        viewHolder.tvMySharedTicketContent.setTagText(itemData.tags, itemData.content);
        viewHolder.tvPraiseCount.setText("赞 " + itemData.supportNum);
        viewHolder.tvMySharedTicketContent.setMovementMethod(new ScrollingMovementMethod());
        return convertView;
    }

    @Override
    public int getCount()
    {
        return shareTicketDatas.size();
    }
    public class ViewHolder{
        public PhotoView imgMySharedTicket;
        public TagTextView tvMySharedTicketContent;
        public TextView tvPraiseCount;
        public TextView btnShareMySharedTicket;
    }
}
