package com.dym.film.activity.mine;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dym.film.R;
import com.dym.film.activity.HtmlActivity;
import com.dym.film.activity.base.BaseActivity;
import com.dym.film.application.ConfigInfo;
import com.dym.film.application.UserInfo;
import com.dym.film.common.AsyncHttpHelper;
import com.dym.film.common.BaseThread;
import com.dym.film.common.HttpRespCallback;
import com.dym.film.manager.CommonManager;
import com.dym.film.manager.NetworkManager;
import com.dym.film.manager.QCloudManager;
import com.dym.film.model.BaseRespInfo;
import com.dym.film.ui.CircleImageView;
import com.dym.film.ui.CustomDialog;
import com.dym.film.utils.FileUtils;
import com.dym.film.utils.LogUtils;
import com.dym.film.utils.StringUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.tencent.upload.UploadManager;
import com.tencent.upload.task.ITask;
import com.tencent.upload.task.IUploadTaskListener;
import com.tencent.upload.task.data.FileInfo;
import com.tencent.upload.task.impl.PhotoUploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wbz360 on 2015/11/12.
 */
public class MySetActivity extends BaseActivity
{
    public static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int PHOTO_CUT = 3;

    public String newName;
    private CustomDialog photoDialog;
    private CustomDialog nicknameDialog;
    private TextView tvName;
    private TextView tvSex;
    private TextView tvCacheSize;
    private EditText etNickName;
    private Button btnCancelInput;
    private CircleImageView imgPhoto;
    private Bitmap photoBmp;
    private String mAvatarFilePath = "";
    private String mAvatarFileCutPath = null;
    private Button btnLoginOut;
    private LinearLayout layAccount;
    private NetworkManager mNetworkManager = NetworkManager.getInstance();
    private boolean mIsGettingSign = false;
    protected HttpRespCallback<NetworkManager.RespGetQCloudSign> mGetSignCallback = new HttpRespCallback<NetworkManager.RespGetQCloudSign>()
    {
        private final static int E_CODE_IMAGE_TOO_LARGE = 0x13;
        private final static int E_CODE_UNKNOWN = 0x14;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        @Override
        public void onRespFailure(int code, String msg)
        {
            cancelProgressDialog();
            LogUtils.e(TAG, "获取签名错误..." + msg);

            switch (code) {
                case E_CODE_IMAGE_TOO_LARGE:
                    Toast.makeText(MySetActivity.this, "图片太大啦", Toast.LENGTH_SHORT).show();
                    break;

                case E_CODE_UNKNOWN:
                    Toast.makeText(MySetActivity.this, "好像出问题了", Toast.LENGTH_SHORT).show();
                    break;
            }

        }

        @Override
        public void onRespSuccess(NetworkManager.RespGetQCloudSign sign, String body)
        {
            if (sign.sig == null) {
                onRespFailure(-1, "签名错误");
            }
            else {
                //  在子线程中吧图片 压缩成 100 x 100 等比缩放

                LogUtils.e(TAG, "Size: " + (photoBmp.getByteCount() / 1024) + "K");
                if (photoBmp.getByteCount() / 1024 > ConfigInfo.MAX_UPLOAD_FILE_LIMIT) {
                    //Loge(TAG, "Bitmap: too larget");
                    sendMessage(WHAT_HTTP_FAILED, new HttpException(E_CODE_IMAGE_TOO_LARGE, "图片太大啦"));
                    return;
                }

                photoBmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//
//                mImageWidth = mBitmap.getWidth();
//                mImageHeight = mBitmap.getHeight();
//                mBitmap.recycle();

                sendMessage(WHAT_HTTP_SUCCESS, sign.sig);
            }
        }

        @Override
        public void runOnMainThread(Message msg)
        {
            switch (msg.what) {
                case WHAT_HTTP_SUCCESS:
                    NetworkManager.QCloudSignRespModel sign = (NetworkManager.QCloudSignRespModel) msg.obj;
                    UploadManager manager = QCloudManager.getPhotoUploadManager(MySetActivity.this);

                    PhotoUploadTask photoUploadTask = new PhotoUploadTask(outputStream.toByteArray(), new PhotoUploadTaskHandler());
                    photoUploadTask.setBucket(ConfigInfo.QCLOUD_BUCKET);
                    photoUploadTask.setAuth(sign.signature);
                    if (!manager.upload(photoUploadTask)) {
                        cancelProgressDialog();
                    }
                    break;
            }
            mIsGettingSign = false;
        }
    };
    private CustomDialog sexDialog;


    @Override
    protected int setLayoutView()
    {
        return R.layout.activity_my_set;
    }

    @Override
    protected void initVariable()
    {

    }

    @Override
    protected void findViews()
    {
        tvName = $(R.id.tvName);
        tvSex= $(R.id.tvSex);
        tvCacheSize = $(R.id.tvCacheSize);
        imgPhoto = $(R.id.imgPhoto);
        btnLoginOut = $(R.id.btnLoginOut);
        layAccount = $(R.id.layAccount);
    }

    @Override
    protected void initData()
    {
        if (UserInfo.isLogin) {
            layAccount.setVisibility(View.VISIBLE);
            btnLoginOut.setVisibility(View.VISIBLE);
            tvName.setText(UserInfo.name);
            tvSex.setText(getSex(UserInfo.gender));

            if (!TextUtils.isEmpty(UserInfo.avatar)) {
                ImageLoader.getInstance().displayImage(UserInfo.avatar, imgPhoto);
            }else {
                imgPhoto.setImageResource(R.drawable.ic_default_photo);
            }

        }
        else {
            layAccount.setVisibility(View.GONE);
            btnLoginOut.setVisibility(View.GONE);
        }

    }

    private String getSex(int sex){
        String gender="";
        if (sex==1){
            gender="男";
        }else if (sex==2){
            gender="女";
        }else{
            gender="";
        }
        return  gender;
    }
    @Override

    protected void setListener()
    {
        File individualCacheDir = StorageUtils.getIndividualCacheDirectory(this);
        long size= FileUtils.getFileSize(individualCacheDir);
        tvCacheSize.setText(StringUtils.formatFileSize(size,true));
    }

    @Override
    public void doClick(View view)
    {
        switch (view.getId()) {
            case R.id.layToPhoto:
                showPhotoDialog();
                break;
            case R.id.layToNickName:
                showNickNameDialog();
                break;

            case R.id.layToSex:
                showSexDialog();
                break;
            case R.id.layToUpdatePwd:
                if (!UserInfo.isNative){//不是原生用户
                    ShowMsg("第三方登录，不能修改密码");
                }else{
                    openActivity(UpdatePwd2Activity.class);
                }

                break;
            case R.id.layToClearCache:
                clearCache();
                break;
            case R.id.layToHelp:

                break;
            case R.id.layToFeedback:
                openActivity(FeedBackActivity.class);
                break;
            case R.id.layToAboutUs:
                String url= ConfigInfo.BASE_URL+"/app/about/me";
                Intent intent = new Intent(MySetActivity.this, HtmlActivity.class);
                intent.putExtra(HtmlActivity.KEY_HTML_URL,url);
                intent.putExtra(HtmlActivity.KEY_HTML_ACTION,4);
                startActivity(intent);
                break;
            case R.id.layToService:
                Intent intent4 = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + "021-52896532"));
                intent4.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent4);
                break;
            case R.id.btnLoginOut:
                apiRequestManager.loginOut(new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
                {
                    @Override
                    public void onSuccess(BaseRespInfo data)
                    {
                        UserInfo.clearUserInfo(mContext);
                        MyMainActivity.hasBaseInfo=false;
                        MySetActivity.this.finish();
                    }

                    @Override
                    public void onFailure(String errorCode, String message)
                    {
                        ShowMsg("登出失败");
                    }
                });


                break;
            default:
                finish();
                break;

        }
    }


    public void clearCache()
    {
        showProgressDialog();
        ImageLoader.getInstance().clearDiskCache();
        tvCacheSize.setText("0B");
        cancelProgressDialog();
        ShowMsg("清除完成");
    }

    /*
    * 拍照
    * */
    public void cameraPicture()
    {
        // 实例化拍照的Intent
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 设置图片存放的路径，Environment.getExternalStorageDirectory()得到SD卡的根目录
        mAvatarFilePath = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera/";// 存放照片的文件夹
        // 给相片命名
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".jpg";// 照片命名.为临时文件
        // 检查存放的路径是否存在，如果不存在则创建目录
        File file = FileUtils.getFile("/DCIM/Camera/", fileName);
//        File file = new File(strImgTempPath);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        // 在此目录下创建文件
//        file = new File(strImgTempPath, fileName);
        // // 该照片的绝对路径
        mAvatarFilePath = mAvatarFilePath + fileName;
        // 把文件地址转换成Uri格式
        Uri uri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片临时文件的存放地址,若果设置uri，Intent返回的数据为null
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    /*
    * 从相册里选取
    * */
    public void localPicture()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PHOTO);// 4.4版本
    }

    /**
     * 裁剪图片
     *
     * @param uri
     */
    public void startPhotoCut(Uri uri)
    {
        mAvatarFileCutPath = FileUtils.getFilePath("DaYinMu/images", "tempCutPhoto");
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");// crop=true 有这句才能出来最后的裁剪页面.
        intent.putExtra("aspectX", 1);// 这两项为裁剪框的比例.
        intent.putExtra("aspectY", 1);// x:y=1:1
        intent.putExtra("scale", true);
        // outputX,outputY 是剪裁后图片的宽高
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("output", Uri.fromFile(new File(mAvatarFileCutPath)));
        intent.putExtra("noFaceDetection", true);// 取消人脸识别功能,会按自定义aspectX和aspectY为来处理
        startActivityForResult(intent, PHOTO_CUT);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            //下面是将临时图片刷新显示出来
            Uri uri = Uri.fromFile(new File(mAvatarFilePath));
            //这里必须调用此广播方法，不然图片保存了，但不会显示出来
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            startPhotoCut(uri);//这里和下三行二选一
//          photoBmp = BitmapUtils.decodeSampledBitmapFromSD(strImgTempPath, 100, 100);
//          showProgressDialog();
//          mNetworkManager.getQCloudSign(mGetSignCallback);
        }
        else if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            Uri uri = data.getData();
//            ContentResolver resolver = getContentResolver();
            //photoBmp = MediaStore.Images.Media.getBitmap(resolver, uri);  //先得到bitmap图片
//            try {
//                InputStream input = resolver.openInputStream(uri);
//                photoBmp= BitmapFactory.decodeStream(input);
//                input.close();
//                //LogUtils.i("123", photoBmp.getWidth());
//                // LogUtils.i("123", photoBmp.getHeight());
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
            LogUtils.i("123","uri"+ uri.toString());
            mAvatarFilePath = CommonManager.getRealFilePath(this, uri);//先得到bitmap图片
            LogUtils.i("123","131231"+ mAvatarFilePath);
            if (mAvatarFilePath!=null){
                startPhotoCut(Uri.fromFile(new File(mAvatarFilePath)));//这里和下三行二选一
            }else{
                String path=getPath(this, uri);
                LogUtils.i("123", "PATH-" +path );
                Uri newUri = Uri.parse("file:///" + path); // 将绝对路径转换为URL
               startPhotoCut(newUri);//这里和下三行二选一
            }
//
//          photoBmp = CommonManager.compressImageFromFile(mAvatarFilePath, 150, 150);
//          showProgressDialog();
//          mNetworkManager.getQCloudSign(mGetSignCallback);

        }
        else if (requestCode == PHOTO_CUT && resultCode == RESULT_OK) {

//            photoBmp = CommonManager.compressImageFromFile(mAvatarFilePath, 100, 100);
            photoBmp= BitmapFactory.decodeFile(mAvatarFileCutPath);
            if (photoBmp != null)//保存图片
            {
                LogUtils.i("123", photoBmp.getWidth()+"");
                LogUtils.i("123", photoBmp.getHeight()+"");
                showProgressDialog();
                mNetworkManager.getQCloudSign(mGetSignCallback);
            }

        }

    }

    public void showPhotoDialog()
    {
        if (photoDialog == null) {
            photoDialog = new CustomDialog(this, R.style.default_dialog);
            photoDialog.setWindowAnimations(R.style.bottom_dialog_animation);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_photo, null);
            Button button = $(view, R.id.btnCancelPhoto);
            Button btnFromCamera = $(view, R.id.btnFromCamera);
            Button btnFromLocal = $(view, R.id.btnFromLocal);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    photoDialog.dismiss();
                }
            });
            btnFromCamera.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    photoDialog.dismiss();
                    cameraPicture();
                }
            });
            btnFromLocal.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    photoDialog.dismiss();
                    localPicture();
                }
            });
            photoDialog.setContentView(view);
        }
        DisplayMetrics d = getResources().getDisplayMetrics();
        photoDialog.show(Gravity.BOTTOM, d.widthPixels);
    }

    public void showNickNameDialog()
    {
        if (nicknameDialog == null) {
            nicknameDialog = new CustomDialog(this, R.style.default_dialog);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_nickname, null);
            etNickName = $(view, R.id.etNickName);
            Button buttonCancel = $(view, R.id.btnCancelNickname);
            Button buttonUpdate = $(view, R.id.btnUpdateNickname);
            btnCancelInput = $(view, R.id.btnCancelInput);
            buttonCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    nicknameDialog.dismiss();
                }
            });
            buttonUpdate.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {
                    newName = etNickName.getText().toString();
                    if (newName.equals(UserInfo.name)) {
                        nicknameDialog.dismiss();
                        return;
                    }
                    if (newName.equals("")) {
                        MySetActivity.this.ShowMsg("昵称不能为空");
                        return;
                    }
                    if (newName.length() >16) {
                        MySetActivity.this.ShowMsg("昵称字数不能超过16");
                        return;
                    }
                    nicknameDialog.dismiss();
                    updateNickname();
                    showProgressDialog();

                }
            });
            btnCancelInput.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    etNickName.setText("");
                    btnCancelInput.setVisibility(View.INVISIBLE);
                }
            });
            etNickName.addTextChangedListener(new TextWatcher()
            {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    LogUtils.i("123", s+"");
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                    LogUtils.i("123", s.toString() + "-" + start + "-" + count + "-" + after);
                    if (after == 1) {//说明添加字符
                        btnCancelInput.setVisibility(View.VISIBLE);
                    }
                    else if (after == 0 && start == 0) {//说明减少字符，并且为空了
                        btnCancelInput.setVisibility(View.INVISIBLE);
                    }
                    else {
                        btnCancelInput.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                    LogUtils.i("123", s.toString());
                }
            });
            etNickName.setText(UserInfo.name);
            nicknameDialog.setContentView(view);
        }
        nicknameDialog.show(Gravity.CENTER);
    }
    public void showSexDialog()
    {
        if (sexDialog == null) {
            sexDialog = new CustomDialog(this, R.style.default_dialog);
            sexDialog.setWindowAnimations(R.style.bottom_dialog_animation);
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_update_sex, null);
            Button btnCancelSex = $(view, R.id.btnCancelSex);
            LinearLayout btnSexBoy = $(view, R.id.btnSexBoy);
            LinearLayout btnSexGirl = $(view, R.id.btnSexGirl);
            btnCancelSex.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sexDialog.dismiss();
                }
            });
            btnSexBoy.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sexDialog.dismiss();
                    updateSex(1);

                }
            });
            btnSexGirl.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sexDialog.dismiss();
                    updateSex(2);
                }
            });
            sexDialog.setContentView(view);
        }
        DisplayMetrics d = getResources().getDisplayMetrics();
        sexDialog.show(Gravity.BOTTOM, d.widthPixels);
    }
    private void updateNickname()
    {
        apiRequestManager.updateNickname(newName, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                tvName.setText(newName);
                UserInfo.name = newName;
                UserInfo.saveUserInfo(mContext);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }

    private void updateSex(final int sex)
    {
        showProgressDialog();
        apiRequestManager.updateSex(sex, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                tvSex.setText(getSex(sex));
                UserInfo.gender = sex;
                UserInfo.saveUserInfo(mContext);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }
    protected class PhotoUploadTaskHandler extends BaseThread implements IUploadTaskListener
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
//            long per = (long) ((sendSize * 100) / (totalSize * 1.0f));
//            final long p = per == 100 ? 99 : per;

        }

        @Override
        public void onUploadStateChange(ITask.TaskState taskState)
        {

        }

        @Override
        public void handleMessage(Message message)
        {

            switch (message.what) {
                case WHAT_UPLOAD_FAILED:
                    HttpRespCallback.HttpException exception = (HttpRespCallback.HttpException) message.obj;
                    if (exception != null) {
                        LogUtils.e("TicketShare", "Exception: " + exception.toString());
                    }
                    cancelProgressDialog();
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
                    updatePhotoUrl(info.url);
                    break;
            }
        }
    }

    private void updatePhotoUrl(final String url)
    {
        apiRequestManager.updatePhotoUrl(url, new AsyncHttpHelper.ResultCallback<BaseRespInfo>()
        {
            @Override
            public void onSuccess(BaseRespInfo data)
            {
                cancelProgressDialog();
                UserInfo.avatar=url;
                UserInfo.saveUserInfo(mContext);
                imgPhoto.setImageBitmap(photoBmp);
            }

            @Override
            public void onFailure(String errorCode, String message)
            {
                cancelProgressDialog();
            }
        });

    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }
}
