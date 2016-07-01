package com.dym.film.activity.sharedticket;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.activity.base.BaseViewCtrlActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.common.BaseThread;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.BaiduLBSManager;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.manager.ShareManager;
import com.dym.film.manager.data.MainSharedTicketDataManager;
import com.dym.film.manager.data.TagSharedTicketDataManager;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.InputMethodUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.MatStatsUtil;
import com.dym.film.utils.MixUtils;
import com.dym.film.views.FlowLayout;
import com.tencent.upload.UploadManager;
import com.tencent.upload.task.ITask;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.PhotoUploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2015/11/12
 */

/**
 * 用户晒票的第一个页面
 */
public class TicketShareActivity extends BaseViewCtrlActivity
{
    public final static String TAG = "TicketShareView";

    public final static String KEY_IMAGE_URI = "imageFile";

    private TicketShareViewController mViewController = null;
    private CustomDialog tagDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mViewController = new TicketShareViewController();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy()
    {
        mViewController.onDestroy();
        super.onDestroy();
    }

    public class TicketShareViewController extends BaseContentViewController
    {
        public final static int MAX_COMMENT_LENGTH = 512;
        private final static String DEF_WORTH_STRING = "非常值得一看";
        private final static String DEF_NOT_WORTH_STRING = "表现平平，没有什么惊喜";
        /**
         * Share Ticket数据
         */
        private Uri mImageUri = null;
        private File mImageFile = null;
        private Bitmap mImageBitmap = null;
        private int mImageWidth = 0;
        private int mImageHeight = 0;
        protected HttpRespCallback<NetworkManager.RespGetQCloudSign> mGetSignCallback = new HttpRespCallback<NetworkManager.RespGetQCloudSign>()
        {
            private final static int E_CODE_IMAGE_TOO_LARGE = 0x13;
            private final static int E_CODE_UNKNOWN = 0x14;
            private final static int E_CODE_IMAGE_INVALID = 0x15;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public void onRespFailure(int code, String msg)
            {
                MixUtils.dismissProgressDialog();
                LogUtils.e(TAG, "获取签名错误..." + msg);

                switch (code) {
                    case E_CODE_IMAGE_TOO_LARGE:
                        MixUtils.toastShort(mActivity, "图片太大啦");
                        break;

                    case E_CODE_IMAGE_INVALID:
                        MixUtils.toastShort(mActivity, "无效图片");
                        break;

                    case E_CODE_UNKNOWN:
                        MixUtils.toastShort(mActivity, "晒票好像出问题了");
                        break;
                }

            }

            @Override
            public void onRespSuccess(NetworkManager.RespGetQCloudSign sign, String body)
            {
                if (sign.sig == null) {
                    sendMessage(WHAT_HTTP_FAILED, new HttpException(E_CODE_UNKNOWN, "签名无效"));
                }
                else {
                    Bitmap bitmap = CommonManager.compressImage(mImageFile);
                    if (bitmap == null) {
                        bitmap = mImageBitmap;
                    }

                    if (bitmap == null) {
                        sendMessage(WHAT_HTTP_FAILED, new HttpException(E_CODE_IMAGE_INVALID, "无效图片"));
                        return;
                    }

                    LogUtils.e(TAG, "Size: " + (bitmap.getByteCount() / 1024) + "K" + " W: " + bitmap.getWidth() + " H: " + bitmap.getHeight());
                    if (bitmap.getByteCount() / 1024 > ConfigInfo.MAX_UPLOAD_FILE_LIMIT) {
                        LogUtils.e(TAG, "Bitmap: too larger");
                        sendMessage(WHAT_HTTP_FAILED, new HttpException(E_CODE_IMAGE_TOO_LARGE, "图片太大啦"));
                        return;
                    }

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                    mImageWidth = bitmap.getWidth();
                    mImageHeight = bitmap.getHeight();
                    bitmap.recycle();

                    sendMessage(WHAT_HTTP_SUCCESS, sign.sig);
                }
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                switch (msg.what) {
                    case WHAT_HTTP_SUCCESS:
                        NetworkManager.QCloudSignRespModel sign = (NetworkManager.QCloudSignRespModel) msg.obj;
                        UploadManager manager = QCloudManager.getPhotoUploadManager(mActivity);


                        PhotoUploadTask photoUploadTask = new PhotoUploadTask(outputStream.toByteArray(), new TicketUploadTaskHandler());
                        photoUploadTask.setBucket(ConfigInfo.QCLOUD_BUCKET);
                        photoUploadTask.setAuth(sign.signature);
                        if (manager.upload(photoUploadTask)) {
                            MixUtils.setProgressDialogMessage("1%");
                        }
                        else {
                            //Loge("ShareTicket", "发布错误");
                            onRespFailure(E_CODE_UNKNOWN, "发布错误");
                        }
                        break;
                }
            }
        };
        private boolean mIsWorth = false;
        private String mCity = "";
        private String mDistrict = "";
        private int mOpinion = -1;
        /**
         * 晒票后的imageUrl
         */
        private String mSharedImageUrl = "";
        /**
         * 返回进行分享的url
         */
        private String mShareUrl = "";
        private BaiduLBSManager mLBSManager = BaiduLBSManager.getInstance();
        private NetworkManager mNetworkManager = NetworkManager.getInstance();
        private ShareManager mShareManager = null;
        /**
         * Views
         */
        private View mMainPage = null;
        private View mShareFinishedPage = null;
        private EditText mFilmCommentEdit = null;
        private Animation mBottomInAnim = null;
        protected HttpRespCallback<NetworkManager.RespTicketShare> mHttRespCallback = new HttpRespCallback<NetworkManager.RespTicketShare>()
        {
            @Override
            public void onRespFailure(int code, String msg)
            {
                MixUtils.dismissProgressDialog();
                MixUtils.toastShort(mActivity, "发表失败");
            }

            @Override
            public void runOnMainThread(Message msg)
            {
                MixUtils.dismissProgressDialog();
                NetworkManager.RespTicketShare model = (NetworkManager.RespTicketShare) msg.obj;

                mShareUrl = model.shareUrl;
                TagSharedTicketDataManager.mInstance.setNeedRefreshAll(true);
                MainSharedTicketDataManager.mInstance.setNeedRefreshAll(true);
                LogUtils.e(TAG, "ShareUrl: " + mShareUrl);

                onShareTicketFinished();
            }
        };
        private FlowLayout mFlowLayout = null;
        private RadioGroup mOpinionSwitcher = null;
//        private ImageView mOpinionImage = null;
//        private TextView mNotWorthText = null;
//        private TextView mWorthText = null;
        private String mSelectedFilmTag = "";
        private TextView mLastSelectedView = null;

        public TicketShareViewController()
        {
            super(true);
            initialize();
        }

        public void onDestroy()
        {
            mLBSManager.stopLocation();
        }

        public void initialize()
        {
            // 初始化 title
            setOnClickListener(R.id.backInnerButton);
            setOnClickListener(R.id.shareTicketButton);
            mFlowLayout = (FlowLayout) findViewById(R.id.tagFlowLayout);
            mFlowLayout.removeAllViews();

            mImageUri = mActivity.getIntent().getData();
            if (mImageUri != null) {
                String filePath = CommonManager.getRealFilePath(mActivity, mImageUri);
                mImageFile = new File(filePath);
            }

            if (mImageUri == null || !mImageFile.exists()) {
                MixUtils.toastShort(mActivity, "发表失败");
                finish();
                return;
            }

            /**
             * 如果图片被旋转了, 需要旋转回来
             */
            final int degrees = CommonManager.getImageDegree(mImageFile.getAbsolutePath());
            LogUtils.e(TAG, "Degrees: " + degrees);

            if (degrees != 0) {
                MixUtils.showProgressDialog(mActivity, "", true);
                new BaseThread()
                {
                    @Override
                    public void run()
                    {
                        // 获取图片宽高
                        BitmapFactory.Options newOpts = new BitmapFactory.Options();
                        newOpts.inJustDecodeBounds = false;
                        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
                        mImageBitmap = BitmapFactory.decodeFile(mImageFile.getAbsolutePath(), newOpts);
                        mImageBitmap = CommonManager.rotateBitmapByDegree(mImageBitmap, degrees);

                        try {
                            FileOutputStream fos = new FileOutputStream(mImageFile);
                            mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        LogUtils.e(TAG, "ImageWidth: " + mImageWidth + "  ImageHeight: " + mImageHeight);
                        sendMessage(0);
                    }

                    @Override
                    public void handleMessage(Message msg)
                    {
                        MixUtils.dismissProgressDialog();
                        initializeViews();
                    }
                }.startThread();
            }
            else {
                initializeViews();
            }

            /**
             * 定位
             */
            mLBSManager.startLocation();
        }

//        private void switchOpinion(boolean worth)
//        {

//
//            mOpinionSwitcher.setBackgroundResource(worth ? R.drawable.bg_opinion_switch_worth : R.drawable.bg_opinion_switch_not_worth);
//            mNotWorthText.setText(worth ? "" : "不值");
//            mWorthText.setText(worth ? "值" : "");
//
//            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mOpinionImage.getLayoutParams();
//            params.gravity = (worth ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
//            mOpinionImage.setLayoutParams(params);
//            mOpinionImage.setImageResource(worth ? R.drawable.ic_is_white_worth : R.drawable.ic_is_white_not_worth);
//
//            String text = mFilmCommentEdit.getText().toString();
//            if (TextUtils.isEmpty(text)) {
//                mFilmCommentEdit.setHint(worth ? DEF_WORTH_STRING : DEF_NOT_WORTH_STRING);
//            }
//        }

        /**
         * @param root         最外层布局，需要调整的布局
         * @param scrollToView 被键盘遮挡的scrollToView，滚动root,使scrollToView在root可视区域的底部
         */
        private void controlKeyboardLayout(final View root, final View scrollToView)
        {
            root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
            {
                int mLastHeight = 0;
                int mLastBottom = -1;


                ValueAnimator scrollAnimator = null;

                @Override
                public void onGlobalLayout()
                {
                    Rect rect = new Rect();
                    root.getWindowVisibleDisplayFrame(rect);

                    LogUtils.e(TAG, "Rect: " + rect);

                    if (mLastBottom == -1) {
                        mLastBottom = rect.bottom;
                        return;
                    }

                    int nb = rect.bottom;
                    int ob = mLastBottom;

                    if (nb < ob) {
                        // 键盘显示了， 滑上去
                        int[] location = new int[2];
                        scrollToView.getLocationInWindow(location);
                        int scrollHeight = (location[1] + scrollToView.getHeight()) - nb;

                        scrollAnimator = ValueAnimator.ofInt(0, scrollHeight);
                        scrollAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator)
                            {
                                int value = (int) valueAnimator.getAnimatedValue();
                                root.scrollTo(0, value);
                                mLastHeight = value;
                            }
                        });
                        scrollAnimator.start();
                    }
                    else if (nb > ob) {
                        // 键盘隐藏了, 滑下来
                        if (scrollAnimator != null) {
                            scrollAnimator.cancel();
                        }
                        scrollAnimator = ValueAnimator.ofInt(mLastHeight, 0);
                        scrollAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
                        {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator)
                            {
                                int value = (int) valueAnimator.getAnimatedValue();
                                root.scrollTo(0, value);
                                mLastHeight = value;
                            }
                        });
                        scrollAnimator.addListener(new Animator.AnimatorListener()
                        {
                            @Override
                            public void onAnimationStart(Animator animator)
                            {
                            }

                            @Override
                            public void onAnimationEnd(Animator animator)
                            {
                                root.scrollTo(0, 0);
                                mLastHeight = 0;
                            }

                            @Override
                            public void onAnimationCancel(Animator animator)
                            {
                                root.scrollTo(0, 0);
                                mLastHeight = 0;
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator)
                            {
                            }
                        });

                        scrollAnimator.start();
                    }

                    if (nb != ob) {
                        mLastBottom = nb;
                    }
                }
            });
        }

        private void initializeViews()
        {
            mBottomInAnim = AnimationUtils.loadAnimation(mActivity, R.anim.bottom_dialog_in);

            mMainPage = findViewById(R.id.pageOneLayout);
            mShareFinishedPage = findViewById(R.id.pageThreeLayout);

            mFilmCommentEdit = (EditText) findViewById(R.id.commentEdit);
            final TextView numberText = (TextView) findViewById(R.id.textNum);
            mFilmCommentEdit.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                {
                }

                @Override
                public void afterTextChanged(Editable editable)
                {
                    String s = editable.toString();
                    int index = s.indexOf("\n\n");
                    if (index >= 0) {
                        editable.delete(index, index + 1);
                    }

                    int length = editable.length();
                    if (length >= MAX_COMMENT_LENGTH) {
                        editable.delete(MAX_COMMENT_LENGTH, editable.length());
                    }
                    numberText.setText(String.valueOf(editable.length()));
                }
            });

            final View contentView = findViewById(R.id.contentView);
            contentView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    CommonManager.dismissSoftInputMethod(mActivity, contentView.getWindowToken());
                }
            });
            controlKeyboardLayout(contentView, findViewById(R.id.bottomLayout));

            /**
             * 设置态度 switcher
             */
            mOpinionSwitcher = (RadioGroup) findViewById(R.id.opinionSwitcher);
            mOpinionSwitcher.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i)
                {
                    if (i==R.id.rbWorth){
                        mOpinion=1;
                        mFilmCommentEdit.setHint(DEF_WORTH_STRING );
                    }else{
                        mOpinion=0;
                        mFilmCommentEdit.setHint(DEF_NOT_WORTH_STRING);
                    }
//                    CommonManager.dismissSoftInputMethod(mActivity, mMainPage.getWindowToken());
                }
            });
//            mOpinionImage = (ImageView) findViewById(R.id.worthImage);
//            mNotWorthText = (TextView) findViewById(R.id.notWorthText);
//            mWorthText = (TextView) findViewById(R.id.worthText);
//
//            switchOpinion(true);
//            setOnClickListener(R.id.opinionSwitcher);
//
            setOnClickListener(R.id.btnCustomTag);
            setOnClickListener(R.id.closeButton);
            setOnClickListener(R.id.shareButton);

            /**
             * 获取tag
             */
            mFlowLayout.removeAllViews();
            mNetworkManager.getTagsList(new HttpRespCallback<NetworkManager.RespTagsList>()
            {
                @Override
                public void onRespFailure(int code, String msg)
                {
                    //
                }

                @Override
                public void runOnMainThread(Message msg)
                {
                    NetworkManager.RespTagsList list = (NetworkManager.RespTagsList) msg.obj;
                    if (list.tags != null) {
                        for (String tag : list.tags) {
                            mFlowLayout.addView(newFilmTagView(tag));
                        }
                    }

//                    mFlowLayout.addView(newFilmTagView("+自定义"));
                }
            });
        }

        public View newFilmTagView(String tag)
        {
            final TextView textView = (TextView) View.inflate(mActivity, R.layout.layout_film_tag, null);
            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            int dp = CommonManager.dpToPx(5);
            params.setMargins(dp, dp, dp, dp);
            textView.setLayoutParams(params);
            textView.setText(tag);
            textView.setBackgroundResource(R.drawable.bg_film_tag_unselect);

            textView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    CommonManager.dismissSoftInputMethod(view.getContext(), view.getWindowToken());
                    if (mLastSelectedView != null) {
                        mLastSelectedView.setBackgroundResource(R.drawable.bg_film_tag_unselect);
                        mLastSelectedView.setTextColor(0xff878787);
                        if (mLastSelectedView.equals(view)) {
                            mLastSelectedView = null;
                            return;
                        }
                    }
                    view.setBackgroundResource(R.drawable.bg_film_tag_select);
                    mLastSelectedView = (TextView) view;
                    mLastSelectedView.setTextColor(0xffffffff);
                    mSelectedFilmTag = ((TextView) view).getText().toString();
                }
            });
            return textView;
        }

        private void onShareTicketFinished()
        {
            CommonManager.dismissSoftInputMethod(mActivity, mMainPage.getWindowToken());
            mMainPage.setVisibility(View.GONE);
            mShareFinishedPage.setVisibility(View.VISIBLE);
            mShareFinishedPage.startAnimation(mBottomInAnim);
        }

        @Override
        protected void onViewClicked(@NonNull View view)
        {
            switch (view.getId()) {
                case R.id.backInnerButton:
                case R.id.closeButton:
                    finish();
                    break;
                case R.id.btnCustomTag:
                    showCustomTagDialog();
                    break;
//                case R.id.opinionSwitcher:
//                    CommonManager.dismissSoftInputMethod(mActivity, mMainPage.getWindowToken());
////                    switchOpinion(!mOpinion);
//                    break;

                case R.id.shareTicketButton:
                    shareTicketToServer();
                    break;

                case R.id.shareButton:
                    if (mShareManager == null) {
                        mShareManager = new ShareManager(mActivity);
                    }
                    mShareManager.setImageUrl(mSharedImageUrl);
                    mShareManager.setWebUrl(NetworkManager.getShareUrl(mShareUrl));
                    mShareManager.setTitleUrl(NetworkManager.getShareUrl(mShareUrl));
                    mShareManager.setTitle("公证电影｜看电影晒票根");
                    String content = mFilmCommentEdit.getText().toString();
                    if (!TextUtils.isEmpty(mSelectedFilmTag)) {
                        content = "《" + mSelectedFilmTag + "》 " + content;
                    }
                    mShareManager.setText(content);
                    mShareManager.showShareDialog(mActivity);
                    break;
            }
        }

        /**
         * 正式发布晒票到服务器
         */
        private void shareTicketToServer()
        {
            MatStatsUtil.eventClick(mActivity, MatStatsUtil.SHOW_SUBMIT);
//            if (mFilmCommentEdit.getText().toString().isEmpty()) {
//                MixUtils.toastShort(mActivity, "说点什么吧!");
//                return;
//            }

            if (mFilmCommentEdit.getText().length() > 512) {
                MixUtils.toastShort(mActivity, "字数过长，无法发布");
                return;
            }

            if (mLastSelectedView == null) {
                MixUtils.toastShort(mActivity, "请选择电影");
                return;
            }
            if (mOpinion ==-1) {
                MixUtils.toastShort(mActivity, "请选择态度");
                return;
            }
            CommonManager.dismissSoftInputMethod(mActivity, mMainPage.getWindowToken());
            MixUtils.showProgressDialog(mActivity, "0%", true);
            mNetworkManager.getQCloudSign(mGetSignCallback);
        }

        @Override
        protected int getViewId()
        {
            return R.layout.activity_ticket_share;
        }

        protected class TicketUploadTaskHandler extends BaseThread implements IUploadTaskListener
        {

            public final static int WHAT_UPLOAD_SUCCESS = 0x11;

            public final static int WHAT_UPLOAD_FAILED = 0x12;

            @Override
            public void onUploadSucceed(FileInfo fileInfo)
            {
                sendMessage(WHAT_UPLOAD_SUCCESS, fileInfo);
            }

            @Override
            public void onUploadFailed(int i, String s)
            {
                sendMessage(WHAT_UPLOAD_FAILED, new HttpRespCallback.HttpException(i, s));
            }

            @Override
            public void onUploadProgress(long totalSize, long sendSize)
            {
                long per = (long) ((sendSize * 100) / (totalSize * 1.0f));
                final long p = per == 100 ? 99 : per;
                mHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        MixUtils.setProgressDialogMessage(p + "%");
                    }
                });
            }

            @Override
            public void onUploadStateChange(ITask.TaskState taskState)
            {

            }

            @Override
            public void handleMessage(Message message)
            {
                MixUtils.dismissProgressDialog();
                switch (message.what) {
                    case WHAT_UPLOAD_FAILED:
                        HttpRespCallback.HttpException exception = (HttpRespCallback.HttpException) message.obj;
                        if (exception != null) {
                            LogUtils.e("TicketShare", "Exception: " + exception.toString());
                        }
                        break;

                    case WHAT_UPLOAD_SUCCESS:
                        FileInfo info = (FileInfo) message.obj;
                        LogUtils.e(TAG, "File Url: " + info.url);
                        LogUtils.e(TAG, "File Id: " + info.url);
                        if (info.extendInfo != null) {
                            for (String key : info.extendInfo.keySet()) {
                                LogUtils.e(TAG, "KeySet: " + key + " : " + info.extendInfo.get(key));
                            }
                        }

                        mSharedImageUrl = info.url;
                        NetworkManager.ReqTicketShare req = new NetworkManager.ReqTicketShare();
                        req.stubUrl = info.url;
                        req.stubImageWidth = mImageWidth;
                        req.stubImageHeight = mImageHeight;
                        if (mFilmCommentEdit.getText().toString().length()==0){
                            req.comment = mFilmCommentEdit.getHint().toString();
                        }else{
                            req.comment = mFilmCommentEdit.getText().toString();
                        }

                        req.opinion = mOpinion;
                        req.film = mSelectedFilmTag;

                        NetworkManager.LocationModel location = new NetworkManager.LocationModel();

                        location.province = mLBSManager.getProvince();
                        location.city = mLBSManager.getCity();
                        location.district = mLBSManager.getDistrict();
                        location.latitude = mLBSManager.getLatitude();
                        location.longitude = mLBSManager.getLongitude();

                        req.location = location;

                        LogUtils.e(TAG, "City: " + location.city + " District: " + location.district);

                        mNetworkManager.shareTicket(req, mHttRespCallback);
                        break;
                }
            }
        }
    }
    public void showCustomTagDialog()
    {
        MatStatsUtil.eventClick(this, MatStatsUtil.ADD_TAG);
        if (tagDialog == null) {
            tagDialog = new CustomDialog(this, R.style.default_dialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_film_tag, null);
            InputMethodUtils.showSoftInput(this);
            final EditText etFilmName = (EditText) view.findViewById(R.id.etFilmName);
            Button buttonCancel = (Button) view.findViewById(R.id.btnCancel);
            Button buttonFinish = (Button) view.findViewById(R.id.btnFinish);
            buttonCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    tagDialog.dismiss();
                }
            });
            buttonFinish.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                   String filmName = etFilmName.getText().toString();
                    if (filmName.equals("")) {
                        MixUtils.toastShort(TicketShareActivity.this, "请输入电影名称");
                        return;
                    }
                    if (filmName.length() >16) {
                        MixUtils.toastShort(TicketShareActivity.this, "字数不能超过16");
                        return;
                    }
                    tagDialog.dismiss();
                   View view= mViewController.newFilmTagView(filmName);
                    mViewController.mFlowLayout.addView(view);
                    view.performClick();
                }
            });

            etFilmName.setText("");
            tagDialog.setContentView(view);
        }
        tagDialog.show(Gravity.CENTER);
    }
}
