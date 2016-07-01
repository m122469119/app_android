package com.dym.film.activity.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.dym.film.R;
import com.dym.film.ui.fullvideoview.FullScreenVideoView;
import com.dym.film.ui.fullvideoview.LightnessController;
import com.dym.film.ui.fullvideoview.VolumnController;
import com.dym.film.utils.DimenUtils;
import com.dym.film.utils.FileUtils;
import com.dym.film.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayerActivity extends Activity implements OnClickListener
{

    //	private String videoUrl = "file:///android_asset/video2.mp4";
    // 自动隐藏顶部和底部View的时间
    private static final int HIDE_TIME = 5000;
    // 自定义VideoView
    private FullScreenVideoView mVideo;
    // 头部View
    private View mTopView;
    // 底部View
    private View mBottomView;
    // 视频播放拖动条
    private SeekBar mSeekBar;
    private ImageView mPlay;
    private TextView mPlayTime;
    private TextView mDurationTime;
    // 音频管理器
    private AudioManager mAudioManager;
    // 屏幕宽高
    private float width;
    private float height;
    // 视频播放时间
    private int playTime;
//    private String videoUrl = "http://hot.vrs.sohu.com/ipad2739210_1448959255_8358104.m3u8?pg=1&pt=5&cv=5.0.0&qd=282&uid=86c2a0873d0641ee8615664090d4a59b&sver=5.0.0&plat=6&ca=3&prod=app";
    private String videoUrl = "http://hot.vrs.sohu.com/ipad2739210_1449664342_8358104.m3u8?pg=1&pt=5&cv=5.0.0&qd=282&uid=8936db6991a343658333583e7510f200&sver=5.0.0&plat=6&ca=3&prod=app";
    // 声音调节Toast
    private VolumnController volumnController;

    // 原始屏幕亮度
    private int orginalLight;
    private Runnable hideRunnable = new Runnable()
    {

        @Override
        public void run()
        {
            showOrHide();
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (mVideo.getCurrentPosition() > 0) {
                        mPlayTime.setText(formatTime(mVideo.getCurrentPosition()));
                        int progress = mVideo.getCurrentPosition() * 100 / mVideo.getDuration();
                        mSeekBar.setProgress(progress);
                        if (mVideo.getCurrentPosition() > mVideo.getDuration() - 100) {
                            mPlayTime.setText("00:00");
                            mSeekBar.setProgress(0);
                        }
                        mSeekBar.setSecondaryProgress(mVideo.getBufferPercentage());
                    }
                    else {
                        mPlayTime.setText("00:00");
                        mSeekBar.setProgress(0);
                    }

                    break;
                case 2:
                    showOrHide();
                    break;

                default:
                    break;
            }
        }
    };
    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener()
    {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            mHandler.removeCallbacks(hideRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (fromUser) {
                int time = progress * mVideo.getDuration() / 100;
                mVideo.seekTo(time);
            }
        }
    };
    private float mLastMotionX;
    private float mLastMotionY;
    private int startX;
    private int startY;
    private int threshold;
    private boolean isClick = true;
    private OnTouchListener mTouchListener = new OnTouchListener()
    {

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            final float x = event.getX();
            final float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = x;
                    mLastMotionY = y;
                    startX = (int) x;
                    startY = (int) y;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = x - mLastMotionX;
                    float deltaY = y - mLastMotionY;
                    float absDeltaX = Math.abs(deltaX);
                    float absDeltaY = Math.abs(deltaY);
                    // 声音调节标识
                    boolean isAdjustAudio = false;
                    if (absDeltaX > threshold && absDeltaY > threshold) {
                        if (absDeltaX < absDeltaY) {
                            isAdjustAudio = true;
                        }
                        else {
                            isAdjustAudio = false;
                        }
                    }
                    else if (absDeltaX < threshold && absDeltaY > threshold) {
                        isAdjustAudio = true;
                    }
                    else if (absDeltaX > threshold && absDeltaY < threshold) {
                        isAdjustAudio = false;
                    }
                    else {
                        return true;
                    }
                    if (isAdjustAudio) {
                        if (x < width / 2) {
                            if (deltaY > 0) {
                                lightDown(absDeltaY);
                            }
                            else if (deltaY < 0) {
                                lightUp(absDeltaY);
                            }
                        }
                        else {
                            if (deltaY > 0) {
                                volumeDown(absDeltaY);
                            }
                            else if (deltaY < 0) {
                                volumeUp(absDeltaY);
                            }
                        }

                    }
                    else {
                        if (deltaX > 0) {
                            forward(absDeltaX);
                        }
                        else if (deltaX < 0) {
                            backward(absDeltaX);
                        }
                    }
                    mLastMotionX = x;
                    mLastMotionY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(x - startX) > threshold || Math.abs(y - startY) > threshold) {
                        isClick = false;
                    }
                    mLastMotionX = 0;
                    mLastMotionY = 0;
                    startX = (int) 0;
                    if (isClick) {
                        showOrHide();
                    }
                    isClick = true;
                    break;

                default:
                    break;
            }
            return true;
        }

    };
    private String filmName;
    private TextView tvFilmName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        setContentView(R.layout.activity_video_player);
        registerReceiver();
        volumnController = new VolumnController(this);
        mVideo = (FullScreenVideoView) findViewById(R.id.videoview);
        mPlayTime = (TextView) findViewById(R.id.play_time);
        tvFilmName= (TextView) findViewById(R.id.tvFilmName);
        mDurationTime = (TextView) findViewById(R.id.total_time);
        mPlay = (ImageView) findViewById(R.id.play_btn);
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mTopView = findViewById(R.id.top_layout);
        mBottomView = findViewById(R.id.bottom_layout);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        width = DimenUtils.getScreenWidth(this);
        height = DimenUtils.getScreenHeight(this);
        threshold = DimenUtils.dp2px(this, 18);

        orginalLight = LightnessController.getLightness(this);

        mPlay.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

//        try {
//            File file = copyBigDataToSD("video2.mp4");
//            videoUrl = file.getAbsolutePath();
//            Log.i("123", videoUrl);
//
//        }
//        catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Log.i("123", e.toString());
//        }

        videoUrl=getIntent().getStringExtra("videoUrl");
        filmName=getIntent().getStringExtra("filmName");
        tvFilmName.setText(filmName);
        LogUtils.i("123", "video3:" + videoUrl);

    }

    private File copyBigDataToSD(String strOutFileName) throws IOException
    {
        File file = new File(Environment.getExternalStorageDirectory(), strOutFileName);
        //Logi("123", file.getAbsolutePath());

        if (file.exists()) {
            return file;
        }
        //Logi("123", file.exists() + "");
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(file);
        myInput = this.getAssets().open(strOutFileName);
        FileUtils.saveStreamToFile(myInput, file);
        return file;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = DimenUtils.getScreenWidth(this);
            width = DimenUtils.getScreenHeight(this);
        }
        else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = DimenUtils.getScreenWidth(this);
            height = DimenUtils.getScreenHeight(this);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        LightnessController.setLightness(this, orginalLight);
    }

    private void backward(float delataX)
    {
        int current = mVideo.getCurrentPosition();
        int backwardTime = (int) (delataX / width * mVideo.getDuration());
        int currentTime = current - backwardTime;
        mVideo.seekTo(currentTime);
        mSeekBar.setProgress(currentTime * 100 / mVideo.getDuration());
        mPlayTime.setText(formatTime(currentTime));
    }

    private void forward(float delataX)
    {
        int current = mVideo.getCurrentPosition();
        int forwardTime = (int) (delataX / width * mVideo.getDuration());
        int currentTime = current + forwardTime;
        mVideo.seekTo(currentTime);
        mSeekBar.setProgress(currentTime * 100 / mVideo.getDuration());
        mPlayTime.setText(formatTime(currentTime));
    }

    private void volumeDown(float delatY)
    {
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int down = (int) (delatY / height * max * 3);
        int volume = Math.max(current - down, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        int transformatVolume = volume * 100 / max;
//        volumnController.show(transformatVolume);
        mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

    private void volumeUp(float delatY)
    {
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int up = (int) ((delatY / height) * max * 3);
        int volume = Math.min(current + up, max);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        int transformatVolume = volume * 100 / max;
//        volumnController.show(transformatVolume);
        mAudioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

    private void lightDown(float delatY)
    {
        int down = (int) (delatY / height * 255 * 3);
        int transformatLight = LightnessController.getLightness(this) - down;
        LightnessController.setLightness(this, transformatLight);
    }

    private void lightUp(float delatY)
    {
        int up = (int) (delatY / height * 255 * 3);
        int transformatLight = LightnessController.getLightness(this) + up;
        LightnessController.setLightness(this, transformatLight);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver();
        mHandler.removeMessages(0);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void playVideo()
    {
        //Logi("123", videoUrl);
        mVideo.setVideoPath(videoUrl);
        mVideo.requestFocus();
        mVideo.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mVideo.setVideoWidth(mp.getVideoWidth());
                mVideo.setVideoHeight(mp.getVideoHeight());

                mVideo.start();
                if (playTime != 0) {
                    mVideo.seekTo(playTime);
                }

                mHandler.removeCallbacks(hideRunnable);
                mHandler.postDelayed(hideRunnable, HIDE_TIME);
                mDurationTime.setText(formatTime(mVideo.getDuration()));
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        mHandler.sendEmptyMessage(1);
                    }
                }, 0, 1000);
            }
        });
        mVideo.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlay.setImageResource(R.drawable.video_btn_down);
                mPlayTime.setText("00:00");
                mSeekBar.setProgress(0);
            }
        });
        mVideo.setOnTouchListener(mTouchListener);

        mVideo.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                new AlertDialog.Builder(VideoPlayerActivity.this)
                        .setMessage("无法播放此视频。")
                        .setPositiveButton("确定",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        /* If we get here, there is no onError listener, so
                                         * at least inform them that the video is over.
                                         */
                                        finish();
                                    }
                                })
                        .setCancelable(false)
                        .show();
                return true;
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String formatTime(long time)
    {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.play_btn:
                if (mVideo.isPlaying()) {
                    mVideo.pause();
                    mPlay.setImageResource(R.drawable.video_btn_down);
                }
                else {
                    mVideo.start();
                    mPlay.setImageResource(R.drawable.video_btn_on);
                }
                break;
            default:
                break;
        }
    }

    public void doClick(View v){
        finish();
    }
    private void showOrHide()
    {
        if (mTopView.getVisibility() == View.VISIBLE) {
            mTopView.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.option_leave_from_top);
            animation.setAnimationListener(new AnimationImp()
            {
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    super.onAnimationEnd(animation);
                    mTopView.setVisibility(View.GONE);
                }
            });
            mTopView.startAnimation(animation);

            mBottomView.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.option_leave_from_bottom);
            animation1.setAnimationListener(new AnimationImp()
            {
                @Override
                public void onAnimationEnd(Animation animation)
                {
                    super.onAnimationEnd(animation);
                    mBottomView.setVisibility(View.GONE);
                }
            });
            mBottomView.startAnimation(animation1);
        }
        else {
            mTopView.setVisibility(View.VISIBLE);
            mTopView.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.option_entry_from_top);
            mTopView.startAnimation(animation);

            mBottomView.setVisibility(View.VISIBLE);
            mBottomView.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.option_entry_from_bottom);
            mBottomView.startAnimation(animation1);
            mHandler.removeCallbacks(hideRunnable);
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }
    }

    private class AnimationImp implements AnimationListener
    {

        @Override
        public void onAnimationEnd(Animation animation)
        {

        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {
        }

        @Override
        public void onAnimationStart(Animation animation)
        {
        }

    }


    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiStatusReceiver, filter);
    }

    public void unregisterReceiver() {
		if(wifiStatusReceiver!=null)
			unregisterReceiver(wifiStatusReceiver);
    }
    BroadcastReceiver wifiStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isWifiConnected = false;
            boolean isMobileConnected = false;
            ConnectivityManager connMgr = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo != null) {
                isWifiConnected = networkInfo.isConnected();
                networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            }
            if (networkInfo != null) {
                isMobileConnected = networkInfo.isConnected();
            }

            // Wifi和移动数据同时打开时为false 或者 移动数据关闭的时候为false
            if (isMobileConnected) {
                showDialog("3G/4G流量可能产生资费情况,是否继续观看影片?");
            }
            // 当wifi打开的时候为true
            if (isWifiConnected) {
                playVideo();
            }

            if(!isMobileConnected&&!isWifiConnected){
                showDialog();
            }

        }
    };

    public void showDialog(String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        StringBuilder build = new StringBuilder();
        alert
//                .setTitle("发现新的版本")
                .setMessage(msg)
                .setPositiveButton("继续",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                playVideo();
                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                finish();
                            }
                        });
        alert.create().show();
    }
    public void showDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        StringBuilder build = new StringBuilder();
        alert
//                .setTitle("发现新的版本")
                .setMessage("暂无网络")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                playVideo();
                            }
                        });
        alert.create().show();
    }

}
