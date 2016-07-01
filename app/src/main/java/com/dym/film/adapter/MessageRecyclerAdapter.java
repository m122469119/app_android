package com.dym.film.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.entity.UserMessage;
import com.dym.film.manager.CommonManager;
import com.dym.film.views.SwipeItemLayout;

import cn.bingoogolapple.androidcommon.adapter.BGARecyclerViewAdapter;
import cn.bingoogolapple.androidcommon.adapter.BGAViewHolderHelper;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/12/19
 */
public class MessageRecyclerAdapter extends BGARecyclerViewAdapter<UserMessage>
{
    /**
     * 当前处于打开状态的item
     */
//    private List<BGASwipeItemLayout> mOpenedSil = new ArrayList<>();

    public MessageRecyclerAdapter(RecyclerView recyclerView)
    {
        super(recyclerView, R.layout.list_item_user_message);
    }

    @Override
    public void setItemChildListener(BGAViewHolderHelper viewHolderHelper)
    {
        viewHolderHelper.setItemChildClickListener(R.id.mainLayout);
        viewHolderHelper.setItemChildClickListener(R.id.trashLayout);
    }

    @Override
    public void fillData(BGAViewHolderHelper viewHolderHelper, int position, UserMessage message)
    {
        SwipeItemLayout swipeItemLayout = viewHolderHelper.getView(R.id.swipeLayout);
        swipeItemLayout.close();

        TextView title = (TextView) viewHolderHelper.getView(R.id.messageTitle);
        ImageView unreadImage = (ImageView) viewHolderHelper.getView(R.id.unreadImage);

        // 设置是否已读
        if (message.getReaded()==0) {
            unreadImage.setVisibility(View.VISIBLE);
            title.setTextColor(Color.WHITE);
        }
        else {
            unreadImage.setVisibility(View.INVISIBLE);
            title.setTextColor(Color.parseColor("#B5B5B5"));
        }

        // 设置消息标题
        title.setText(message.getTitle());

        // 设置消息内容
        TextView content = (TextView) viewHolderHelper.getView(R.id.messageContent);
        content.setText(message.getContent());

        // 设置消息时间
        TextView timeText = (TextView) viewHolderHelper.getView(R.id.messageTime);
        TextView yearText = (TextView) viewHolderHelper.getView(R.id.messageTimeUnit);

        CommonManager.setTime2(timeText, yearText, message.getPushTime());
    }

//    public void closeOpenedSwipeItemLayoutWithAnim()
//    {
//        for (BGASwipeItemLayout sil : mOpenedSil) {
//            sil.closeWithAnim();
//        }
//        mOpenedSil.clear();
//    }
}
