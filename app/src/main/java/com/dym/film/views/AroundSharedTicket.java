package com.dym.film.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.dym.film.R;
import com.dym.film.application.ConfigInfo;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.ui.CircleImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/29
 */

/**
 * 附近晒票的模型
 */
public class AroundSharedTicket
{
    /**
     * 用户头像半径 25dp
     */
    public final static float RADIUS = 25;

    /**
     * 圆心
     */
    public float mX = Float.NaN;
    public float mY = Float.NaN;

    public float mR = CommonManager.dpToPx(RADIUS);

    public int mMapCircleIndex = 0;

    /**
     * 在数据中的索引
     */
    public int mPersonIndex = 0;

    private View mAvatarView = null;

    /**
     * 晒票数据
     */
    public NetworkManager.SharedTicketRespModel mSharedTicket = null;

    public AroundSharedTicket(NetworkManager.SharedTicketRespModel model)
    {
        mSharedTicket = model;
    }

    public NetworkManager.SharedTicketRespModel getSharedTicket()
    {
        return mSharedTicket;
    }

    public View getAvatarView()
    {
        return mAvatarView;
    }

    public View initAvatarView(final Context context, final LoadAvatarCallback callback)
    {
        mAvatarView = View.inflate(context, R.layout.layout_around_avatar, null);
        CircleImageView mAvatarImage = (CircleImageView) mAvatarView.findViewById(R.id.userAvatar);
        ImageView mGenderImage = (ImageView) mAvatarView.findViewById(R.id.genderImage);

        mAvatarImage.setImageResource(R.drawable.ic_default_photo);
        if (mSharedTicket != null) {
            mAvatarView.setVisibility(View.INVISIBLE);

            String url = QCloudManager.urlImage2(mSharedTicket.writer.avatar, ConfigInfo.SIZE_AVATAR);
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.cacheOnDisk(true);
            builder.showImageOnFail(R.drawable.ic_default_photo);
            builder.showImageOnLoading(R.drawable.ic_default_photo);
            builder.showImageForEmptyUri(R.drawable.ic_default_photo);

            ImageLoader.getInstance().displayImage(url, mAvatarImage, builder.build(), new SimpleImageLoadingListener()
            {
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason)
                {
                    //Loge("Kejin", "Loading Failed");
                    show(context);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap bitmap)
                {
                    //Loge("Kejin", "Loading onLoadingComplete");
                    show(context);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view)
                {
                    //Loge("Kejin", "Loading onLoadingCancelled");
                    show(context);
                }
            });

            mAvatarView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //Loge("Kejin", "Clicked: " + mPersonIndex);
                    callback.onImageClicked(v, mPersonIndex);
                }
            });

            mGenderImage.setImageResource(mSharedTicket.writer.gender == 1 ?
                    R.drawable.ic_gender_ar_male : R.drawable.ic_gender_ar_female);
        }

        return mAvatarView;
    }

    public synchronized void show(Context context)
    {
        if (mAvatarView != null) {
            Animation showAnim = AnimationUtils.loadAnimation(context, R.anim.show_around_person);
            mAvatarView.startAnimation(showAnim);
            mAvatarView.setVisibility(View.VISIBLE);
        }
    }

    public synchronized void hide(Context context, Animation.AnimationListener listener)
    {
        if (mAvatarView != null) {

            mAvatarView.setVisibility(View.VISIBLE);
            Animation showAnim = AnimationUtils.loadAnimation(context, R.anim.hide_around_person);
            showAnim.setAnimationListener(listener);
            mAvatarView.startAnimation(showAnim);
        }
    }

    public synchronized void hideSelected(Context context)
    {
        if (mAvatarView != null) {
            Animation showAnim = AnimationUtils.loadAnimation(context, R.anim.hide_selected_around_person);
            mAvatarView.startAnimation(showAnim);
        }
    }

    public synchronized void select(Context context)
    {
        if (mAvatarView != null) {
            Animation selectAnim = AnimationUtils.loadAnimation(context, R.anim.select_around_person);
            mAvatarView.startAnimation(selectAnim);
        }
    }

    public synchronized void unSelect(Context context)
    {
        if (mAvatarView != null) {
            Animation unSelectAnim = AnimationUtils.loadAnimation(context, R.anim.unselect_around_person);
            mAvatarView.startAnimation(unSelectAnim);
        }
    }

    @Override
    public String toString()
    {
        return "X: " + mX + "  Y: " + mY + " R: " + mR;
    }

    public interface LoadAvatarCallback
    {
        void onImageClicked(View v, int index);
    }
}
