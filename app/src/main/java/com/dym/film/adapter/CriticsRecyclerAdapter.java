package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.filmreview.CriticDetailActivity;
import com.dym.film.activity.filmreview.FilmReviewDetailActivity;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/3/23
 */
public class CriticsRecyclerAdapter extends SimpleRecyclerAdapter<NetworkManager.CriticRespModel>
{
    public final static String TAG = "AllCriticAdapter";

    public CriticsRecyclerAdapter(Activity activity, BaseSimpleDataDelegate<NetworkManager.CriticRespModel> manager)
    {
        super(activity, manager);
    }

    public CriticsRecyclerAdapter(Activity activity)
    {
        super(activity);
    }

    @Override
    public View onCreateView(ViewGroup parent, int viewType)
    {
        return mLayoutInflater.inflate(R.layout.list_item_simple_critic, parent, false);
    }

    @Override
    public void onBindModelToView(SimpleViewHolder viewHolder, int position)
    {
        final NetworkManager.CriticRespModel critic = getItem(position);

        if (critic == null) {
            return;
        }

        ImageView mAvatar = (ImageView) viewHolder.findView(R.id.avatar);
        /**
         * 设置影评人头像
         */
        CommonManager.displayAvatar(critic.avatar, mAvatar);

        /**
         * 设置名字
         */
        TextView nameText = (TextView) viewHolder.findView(R.id.name);
        nameText.setText(critic.name);

        /**
         * 设置介绍
         */
        TextView introText = (TextView) viewHolder.findView(R.id.intro);
        introText.setText(critic.title);

        /**
         * 设置条数
         */
        TextView numText = (TextView) viewHolder.findView(R.id.reviewNum);
        numText.setText(String.valueOf(critic.cinecismNum + "篇影评"));

        /**
         * 设置点击
         */
        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CommonManager.putData(CriticDetailActivity.KEY_CRITIC_DATA, critic);
                startCriticDetailActivity();
            }
        });

        viewHolder.findView(R.id.followFlag).setVisibility(critic.followed == 1?View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 启动影评人详情界面
     */
    private void startCriticDetailActivity()
    {
        Intent intent = new Intent(mActivity, CriticDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        mActivity.startActivity(intent);
    }
}
