package com.hotbitmapgg.bilibili.module.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hotbitmapgg.bilibili.base.RxBaseActivity;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.media.MediaController;
import com.hotbitmapgg.bilibili.media.VideoPlayerView;
import com.hotbitmapgg.bilibili.media.callback.DanmukuSwitchListener;
import com.hotbitmapgg.bilibili.media.callback.VideoBackListener;
import com.hotbitmapgg.bilibili.media.danmuku.BiliDanmukuDownloadUtil;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.network.auxiliary.Const;
import com.hotbitmapgg.bilibili.utils.ConstantUtil;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import master.flame.danmaku.controller.DrawHandler;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * 视频播放界面
 */
public class VideoPlayerActivity extends RxBaseActivity implements DanmukuSwitchListener, VideoBackListener {
    @BindView(R.id.sv_danmaku)
    IDanmakuView mDanmakuView;//弹幕控件
    @BindView(R.id.playerView)
    VideoPlayerView mPlayerView;//播放器控件
    @BindView(R.id.buffering_indicator)
    View mBufferingIndicator;
    @BindView(R.id.video_start)
    RelativeLayout mVideoPrepareLayout;
    @BindView(R.id.bili_anim)
    ImageView mAnimImageView;
    @BindView(R.id.video_start_info)
    TextView mPrepareText;

    private String title;
    private int LastPosition = 0;
    private String startText = "初始化播放器...";
    private AnimationDrawable mLoadingAnim;
    private DanmakuContext danmakuContext;
    private int _rid = 0;
    JSONObject  _resource = null;
    String      _streamData = null;
    JSONObject  _streamConfig = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_player;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            _rid = intent.getIntExtra(ConstantUtil.EXTRA_CID, 0);
            title = intent.getStringExtra(ConstantUtil.EXTRA_TITLE);
            String modeule_params = intent.getStringExtra(Const.MODULE_PARAMS);
            _resource = JsonUtil.Parse(modeule_params,new JSONObject());
        }
        initAnimation();
        initMediaPlayer();
    }

    @SuppressLint("UseSparseArrays")
    private void initMediaPlayer() {
        //配置播放器
        MediaController mMediaController = new MediaController(this);
        mMediaController.setTitle(title);
        mPlayerView.setMediaController(mMediaController);
        mPlayerView.setMediaBufferingIndicator(mBufferingIndicator);
        mPlayerView.requestFocus();
        mPlayerView.setOnInfoListener(onInfoListener);
        mPlayerView.setOnSeekCompleteListener(onSeekCompleteListener);
        mPlayerView.setOnCompletionListener(onCompletionListener);
        mPlayerView.setOnControllerEventsListener(onControllerEventsListener);
        //设置弹幕开关监听
        mMediaController.setDanmakuSwitchListener(this);
        //设置返回键监听
        mMediaController.setVideoBackEvent(this);
        //配置弹幕库
        mDanmakuView.enableDanmakuDrawingCache(true);
        //设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<>();
        //滚动弹幕最大显示5行
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5);
        //设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        //设置弹幕样式
        danmakuContext = DanmakuContext.create();
        danmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(0.8f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
        loadData();
    }

    /**
     * 初始化加载动画
     */
    private void initAnimation() {
        mVideoPrepareLayout.setVisibility(View.VISIBLE);
        appendMessage("【完成】\n解析视频地址...");
        mLoadingAnim = (AnimationDrawable) mAnimImageView.getBackground();
        mLoadingAnim.start();
    }

    @Override
    public void initToolBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 获取视频数据以及解析弹幕
     */
    @Override
    public void loadData()
    {
        int rid = JsonUtil.GetInt(_resource,"id",0);
        RetrofitHelper.getZZXAPI()
                .getResourceStream(rid, AppContext.AccountID)
                .compose(this.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"请求资源数据流失败," + reply.Message());
                        appendMessage("【失败】\n错误信息:" + reply.Message());
                        return;
                    }
                    _streamData = reply.GetString("streamData",null);
                    _streamConfig = reply.GetJObject("streamConfig",new JSONObject());
                    finishLoadDataTask();
                }, throwable -> {
                    appendMessage("【失败】\n错误信息:" + throwable.getMessage());
                });

/*        RetrofitHelper.getBiliGoAPI()
                .getHDVideoUrl(_rid, 4, ConstantUtil.VIDEO_TYPE_MP4)
                .compose(bindToLifecycle())
                .map(videoInfo -> Uri.parse(videoInfo.getDurl().get(0).getUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Uri, Observable<BaseDanmakuParser>>() {
                    @Override
                    public Observable<BaseDanmakuParser> call(Uri uri) {
                        mPlayerView.setVideoURI(uri);
                        mPlayerView.setOnPreparedListener(mp -> {
                            mLoadingAnim.stop();
                            startText = startText + "【完成】\n视频缓冲中...";
                            mPrepareText.setText(startText);
                            mVideoPrepareLayout.setVisibility(View.GONE);
                        });
                        String url = "http://comment.bilibili.com/" + _rid + ".xml";
                        return BiliDanmukuDownloadUtil.downloadXML(url);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseDanmakuParser -> {
                    mDanmakuView.prepare(baseDanmakuParser, danmakuContext);
                    mDanmakuView.showFPS(false);
                    mDanmakuView.enableDanmakuDrawingCache(false);
                    mDanmakuView.setCallback(new DrawHandler.Callback() {
                        @Override
                        public void prepared() {
                            mDanmakuView.start();
                        }

                        @Override
                        public void updateTimer(DanmakuTimer danmakuTimer) {
                        }

                        @Override
                        public void danmakuShown(BaseDanmaku danmaku) {
                        }

                        @Override
                        public void drawingFinished() {
                        }
                    });
                    mPlayerView.start();
                }, throwable -> {
                    startText = startText + "【失败】\n视频缓冲中...";
                    mPrepareText.setText(startText);
                    startText = startText + "【失败】\n" + throwable.getMessage();
                    mPrepareText.setText(startText);
                });*/
    }
    private void finishLoadDataTask()
    {
        try
        {
            if(!TextUtils.isEmpty(_streamData))
            {//将流数据写入vres_xxx.ffconcat文件中
                File fileDir = this.getExternalCacheDir();
                File streamfile = new File(fileDir,String.format("vres_%d.ffconcat",_rid));
                if(!streamfile.exists())
                    streamfile.createNewFile();

                DataOutputStream fw = new DataOutputStream(new FileOutputStream(streamfile));
                fw.write(_streamData.getBytes());
                fw.close();

                String filePath = streamfile.getPath();
                mPlayerView.setVideoPath(filePath);//视频控件加载ffconcat资源文件
                mPlayerView.setOnPreparedListener(mp -> {
                    mLoadingAnim.stop();
                    appendMessage("【完成】\n视频缓冲中...");
                    mVideoPrepareLayout.setVisibility(View.GONE);
                });
                mPlayerView.setOnErrorListener((mp, what, extra)->{
                    appendMessage(String.format("【错误】\nVideoView.setOnErrorListener(what:%d,extra:%d)",what,extra));
                    return true;
                });
                mPlayerView.setOnCompletionListener(mp -> {
                    appendMessage("【错误】\nVideoView.setOnCompletionListener");
                });
                mPlayerView.start();
            }
        }
        catch (Exception e)
        {
            appendMessage(String.format("【错误】\nfinishLoadDataTask.Exception:" + e.getMessage()));
        }
    }
    private void appendMessage(String message)
    {
        if(TextUtils.isEmpty(message)) return;

        startText = startText + message;
        mPrepareText.setText(startText);
    }

    /**
     * 视频缓冲事件回调
     */
    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                    mDanmakuView.pause();
                    if (mBufferingIndicator != null) {
                        mBufferingIndicator.setVisibility(View.VISIBLE);
                    }
                }
            } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                if (mDanmakuView != null && mDanmakuView.isPaused()) {
                    mDanmakuView.resume();
                }
                if (mBufferingIndicator != null) {
                    mBufferingIndicator.setVisibility(View.GONE);
                }
            }
            return true;
        }
    };

    /**
     * 视频跳转事件回调
     */
    private IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {

        @Override
        public void onSeekComplete(IMediaPlayer mp) {
            if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                mDanmakuView.seekTo(mp.getCurrentPosition());
            }
        }
    };

    /**
     * 视频播放完成事件回调
     */
    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {

        @Override
        public void onCompletion(IMediaPlayer mp) {
            if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                mDanmakuView.seekTo((long) 0);
                mDanmakuView.pause();
            }
            mPlayerView.pause();
        }
    };

    /**
     * 控制条控制状态事件回调
     */
    private VideoPlayerView.OnControllerEventsListener onControllerEventsListener = new VideoPlayerView.OnControllerEventsListener() {

        @Override
        public void onVideoPause() {
            if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                mDanmakuView.pause();
            }
        }
        @Override
        public void OnVideoResume() {
            if (mDanmakuView != null && mDanmakuView.isPaused()) {
                mDanmakuView.resume();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.seekTo((long) LastPosition);
        }
        if (mPlayerView != null && !mPlayerView.isPlaying()) {
            mPlayerView.seekTo(LastPosition);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayerView != null) {
            LastPosition = mPlayerView.getCurrentPosition();
            mPlayerView.pause();
        }
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDanmakuView != null) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
        if (mLoadingAnim != null) {
            mLoadingAnim.stop();
            mLoadingAnim = null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayerView != null && mPlayerView.isDrawingCacheEnabled()) {
            mPlayerView.destroyDrawingCache();
        }
        if (mDanmakuView != null && mDanmakuView.isPaused()) {
            mDanmakuView.release();
            mDanmakuView = null;
        }
        if (mLoadingAnim != null) {
            mLoadingAnim.stop();
            mLoadingAnim = null;
        }
    }

    /**
     * 弹幕开关回调
     */
    @Override
    public void setDanmakuShow(boolean isShow) {
        if (mDanmakuView != null) {
            if (isShow) {
                mDanmakuView.show();
            } else {
                mDanmakuView.hide();
            }
        }
    }

    /**
     * 退出界面回调
     */
    @Override
    public void back() {
        onBackPressed();
    }

    public static void launch(Activity activity, JSONObject resource)
    {
        if(resource == null) return;

        Intent mIntent = new Intent(activity, VideoPlayerActivity.class);
        mIntent.putExtra(Const.MODULE_PARAMS, resource.toString());
        activity.startActivity(mIntent);
    }
}