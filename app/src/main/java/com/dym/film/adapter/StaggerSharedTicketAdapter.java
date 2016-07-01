package com.dym.film.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.sharedticket.SharedTicketDetailActivity;
import com.dym.film.activity.sharedticket.TagSharedTicketActivity;
import com.dym.film.adapter.base.BaseSimpleRecyclerAdapter;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.manager.data.BaseSharedTicketDataManager;
import com.dym.film.ui.CircleImageView;
import com.dym.film.utils.LogUtils;
import com.dym.film.views.CustomTypefaceTextView;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/18
 */

/**
 * 晒票的瀑布流式
 */
public class StaggerSharedTicketAdapter extends BaseSimpleRecyclerAdapter<
        NetworkManager.SharedTicketRespModel, StaggerSharedTicketAdapter.SharedTicketViewHolder>
{
    public final static String TAG = "StaggerSTAdapter";

    private int mImageWidth = 0;

    private int mMaxImageHeight = CommonManager.MAX_STAGGER_IMAGE_HEIGHT;
    private int mMinImageHeight = CommonManager.MIN_STAGGER_IMAGE_HEIGHT;

    public StaggerSharedTicketAdapter(Activity activity, BaseSharedTicketDataManager dataManager)
    {
        super(activity, dataManager);

        mImageWidth = (int) ((CommonManager.DISPLAY_METRICS.widthPixels - CommonManager.dpToPx(30)) / 2.0f);
    }

    @Override
    public SharedTicketViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mLayoutInflater.inflate(R.layout.stagger_item_shared_ticket, parent, false);

        return new SharedTicketViewHolder(view);
    }


    public class SharedTicketViewHolder extends BaseSimpleRecyclerAdapter.BaseViewHolder
    {
        /**
         * 头像
         */
        private CircleImageView mHeaderImageView = null;

        /**
         * 用户发表的文字内容
         */
        private CustomTypefaceTextView mContentTextView = null;

        /**
         * 用户发表的图片
         */
        private ImageView mTicketImage = null;
        /**
         * 点赞数
         */
        private CustomTypefaceTextView tvPraiseCount = null;
        /**
         * 评论数
         */
        private CustomTypefaceTextView tvCommentCount = null;

        public SharedTicketViewHolder(final View itemView)
        {
            super(itemView);
        }

        @Override
        public void bindModelToView(final int position)
        {
            final NetworkManager.SharedTicketRespModel ticket = getItem(position);
            NetworkManager.UserModel writer = ticket.writer;

            mHeaderImageView = (CircleImageView) findView(R.id.userHeadImage);
            mHeaderImageView.setImageResource(R.drawable.ic_default_photo);
            if (writer != null) {
                /**
                 * 设置用户头像
                 */
                CommonManager.displayAvatar(writer.avatar, mHeaderImageView);

                /**
                 * 设置名字
                 */
                TextView userName = (TextView) findView(R.id.userNameTextView);
                userName.setText(writer.name);

                /**
                 * 设置性别
                 */
                ImageView genderImage = (ImageView) findView(R.id.genderImage);
                genderImage.setImageResource(writer.gender == 1 ? R.drawable.ic_gender_male : R.drawable.ic_gender_female);
            }

            /**
             * 设置时间
             */
            long time = ticket.showOffTime;
            TextView userTimeText = (TextView) findView(R.id.userSharedTimeTextView);
            CommonManager.setTime(userTimeText, time);

            /**
             * 设置态度
             */
            int opinion = ticket.opinion;
            ImageView worthImage = (ImageView) findView(R.id.worthButtonImage);
            worthImage.setImageResource(opinion != 0 ? R.drawable.ic_is_worth_white : R.drawable.ic_is_not_worth);

            /**
             * 设置点赞数
             */
            tvPraiseCount = (CustomTypefaceTextView) findView(R.id.tvPraiseCount);
            tvPraiseCount.setText(ticket.supportNum+"");
            /**
             * 设置评论数
             */
            tvCommentCount = (CustomTypefaceTextView) findView(R.id.tvCommentCount);
            tvCommentCount.setText(ticket.commentsNum+"");
            /**
             * 设置晒票内容
             */
            mContentTextView = (CustomTypefaceTextView) findView(R.id.sharedTicketContentTextView);
            mContentTextView.setText(ticket.content);
            mContentTextView.setVisibility(TextUtils.isEmpty(ticket.content) ? View.GONE : View.VISIBLE);

            TextView ticketName = (TextView) findView(R.id.ticketName);
            ticketName.setText("");
            if (ticket.tags != null && !ticket.tags.isEmpty() && !TextUtils.isEmpty(ticket.tags.get(0))) {
                final String tag = ticket.tags.get(0);
                ticketName.setText(String.valueOf("《" + tag + "》"));

                ticketName.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (!TextUtils.isEmpty(tag)) {
                            startTagSharedTicketActivity(tag);
                        }
                    }
                });
            }


            /**
             * 设置图片
             */
            mTicketImage = (ImageView) findView(R.id.ticketImageView);
            String url = "";
            if (ticket.stubImage != null) {
                int width = ticket.stubImage.width;
                int height = ticket.stubImage.height;

                int imageHeight = (int) (mImageWidth * 1.0f / width * height);
                if (imageHeight > mMaxImageHeight) {
                    imageHeight = mMaxImageHeight + position % 10;
                }
                if (imageHeight < mMinImageHeight) {
                    imageHeight = mMinImageHeight - position % 10;
                }
//                LogUtils.i("123","mMaxImageHeight-"+mMaxImageHeight);
//                LogUtils.i("123","imageHeight-"+imageHeight);
                ViewGroup.LayoutParams params = mTicketImage.getLayoutParams();
                params.height = imageHeight;
                mTicketImage.setLayoutParams(params);

                int urlHeight = (int) (CommonManager.DEF_STAGGER_IMAGE_WIDTH * 1.0f / width * height);
                urlHeight = Math.min(imageHeight, urlHeight);
                url = QCloudManager.urlImage1(ticket.stubImage.url, CommonManager.DEF_STAGGER_IMAGE_WIDTH, urlHeight);
            }
            LogUtils.e(TAG, "URL: " + url);
            CommonManager.displayImage(url, mTicketImage, R.drawable.ic_default_loading_img);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    startDetailActivity(position);
                }
            });
        }

        private void startDetailActivity(int pos)
        {
            CommonManager.putData(SharedTicketDetailActivity.KEY_INTENT, getItem(pos));
            Intent intent = new Intent(mActivity, SharedTicketDetailActivity.class);

            mActivity.startActivity(intent);
        }

        private void startTagSharedTicketActivity(String tag)
        {
            Intent intent = new Intent(mActivity, TagSharedTicketActivity.class);
            intent.putExtra(TagSharedTicketActivity.KEY_TAG, tag);

            mActivity.startActivity(intent);
        }
    }
}
