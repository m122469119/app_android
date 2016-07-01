package com.dym.film.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.dym.film.R;
import com.dym.film.ui.CustomDialog;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by wbz360 on 2015/11/26.
 */
public class ShareManager
{
    private final Context context;
    private CustomDialog shareDialog;
    //通用字段（也是新浪微博）
    private String title="";//内容的标题(只有微博没有标题)
    private String text="";//分享的文本
    private String imagePath="";
    private String imageUrl="";
    /*
    *  QQ和QQ空间分享
    */
    private String titleUrl;// 标题的超链接
    /*微信（好友、朋友圈、收藏）*/
    private String webUrl;//分享的网页地址

    public ShareManager(Context context)
    {
        this.context = context;

    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setText(String shareText)
    {
        this.text = shareText;
    }

    public void setImagePath(String imagePath)
    {
        this.imagePath = imagePath;
    }

    /*如果imagePath和imageUrl同时存在，imageUrl将被忽略*/
    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public void setTitleUrl(String titleUrl)
    {
        this.titleUrl = titleUrl;
    }

    public void setWebUrl(String webUrl)
    {
        this.webUrl = webUrl;
    }

    /**
     * 演示调用ShareSDK执行分享
     * <p/>
     * 使用方式showShare（Wechat.NAME）；
     *
     * @param platformToShare 指定直接分享平台名称
     */
    public void showShare(String platformToShare)
    {
        Platform.ShareParams sp=new Platform.ShareParams();
        if (title!=null){
            sp.setTitle(title);
        }else{
            sp.setTitle("");//qq空间必填
        }
        if (!TextUtils.isEmpty(text)) {
            sp.setText(text);//可以为空,微博不能为null，空间不能为空
        }
        if (imagePath!=null){
            sp.setImagePath(imagePath);//可以为空
        }
        if (imageUrl!=null){
            sp.setImageUrl(imageUrl);//可以为空
        }
        if (!TextUtils.isEmpty(titleUrl)) {
            sp.setTitleUrl(titleUrl); // 标题的超链接
        }else{
            sp.setTitleUrl("http://www.dymfilm.com"); // 标题的超链接,qq分享必须有链接不能为空（null和""）
        }

        sp.setSite("公证电影");//发布分享的网站名称//qq空间必填
        sp.setSiteUrl("http://www.dymfilm.com");//发布分享网站的地址//qq空间必填
        sp.setShareType(Platform.SHARE_WEBPAGE);
        if (webUrl!=null) {
            sp.setUrl(webUrl);
        }
//        if (QQ.NAME.equals(platformToShare)) {
//        }else if (QZone.NAME.equals(platformToShare)) {
//        }else if (SinaWeibo.NAME.equals(platformToShare)) {
//        }else {}
        Platform platform = ShareSDK.getPlatform(platformToShare);
        platform.SSOSetting(false);
        platform.share(sp);
    }

    public void showShareDialog(Context context)
    {
        if (shareDialog == null) {
            shareDialog = new CustomDialog(context);
//            shareDialog.setWindowAnimations(R.style.bottom_dialog_animation);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_share, null);
            Button btnCancelShare = (Button) view.findViewById(R.id.btnCancelShare);
            ImageButton btnWeixin = (ImageButton) view.findViewById(R.id.btnWeixin);
            ImageButton btnWeixinCircle = (ImageButton) view.findViewById(R.id.btnWeixinCircle);
            ImageButton btnSina = (ImageButton) view.findViewById(R.id.btnSina);
            ImageButton btnQq = (ImageButton) view.findViewById(R.id.btnQq);
            ImageButton btnQqSpace = (ImageButton) view.findViewById(R.id.btnQqSpace);
            btnCancelShare.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                }
            });
            btnWeixin.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                    showShare(Wechat.NAME);
                }
            });
            btnWeixinCircle.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                    showShare(WechatMoments.NAME);

                }
            });
            btnSina.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                    showShare(SinaWeibo.NAME);

                }
            });
            btnQq.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                    showShare(QQ.NAME);
                }
            });
            btnQqSpace.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    shareDialog.dismiss();
                    showShare(QZone.NAME);
                }
            });
            shareDialog.setContentView(view);
        }
        DisplayMetrics d = context.getResources().getDisplayMetrics();
        shareDialog.show(Gravity.CENTER);
    }


}
