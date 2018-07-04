package com.hotbitmapgg.bilibili.module.home.bangumi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hotbitmapgg.bilibili.adapter.BangumiDetailsCommentAdapter;
import com.hotbitmapgg.bilibili.adapter.BangumiDetailsHotCommentAdapter;
import com.hotbitmapgg.bilibili.adapter.BangumiDetailsRecommendAdapter;
import com.hotbitmapgg.bilibili.adapter.BangumiDetailsSeasonsAdapter;
import com.hotbitmapgg.bilibili.adapter.BangumiDetailsSelectionAdapter;
import com.hotbitmapgg.bilibili.adapter.helper.HeaderViewRecyclerAdapter;
import com.hotbitmapgg.bilibili.base.RxBaseActivity;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiDetailsCommentInfo;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiDetailsInfo;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiDetailsRecommendInfo;
import com.hotbitmapgg.bilibili.module.video.VideoDetailsActivity;
import com.hotbitmapgg.bilibili.network.auxiliary.Const;
import com.hotbitmapgg.bilibili.utils.ConstantUtil;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.LogUtil;
import com.hotbitmapgg.bilibili.utils.NumberUtil;
import com.hotbitmapgg.bilibili.utils.SystemBarHelper;
import com.hotbitmapgg.bilibili.widget.CircleProgressView;
import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 番剧详情界面
 */
public class BangumiDetailsActivity extends RxBaseActivity {
    @BindView(R.id.nested_scroll_view)
    NestedScrollView mNestedScrollView;
    @BindView(R.id.bangumi_bg)
    ImageView mBangumiBackgroundImage;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bangumi_pic)
    ImageView mBangumiPic;
    @BindView(R.id.bangumi_details_layout)
    LinearLayout mDetailsLayout;
    @BindView(R.id.circle_progress)
    CircleProgressView mCircleProgressView;
    @BindView(R.id.bangumi_title)
    TextView mBangumiTitle;
    @BindView(R.id.bangumi_update)
    TextView mBangumiUpdate;
    @BindView(R.id.bangumi_play)
    TextView mBangumiPlay;
    @BindView(R.id.bangumi_selection_recycler)
    RecyclerView mBangumiSelectionRecycler;
    @BindView(R.id.tags_layout)
    TagFlowLayout mTagsLayout;
    @BindView(R.id.bangumi_details_introduction)
    TextView mBangumiIntroduction;
    @BindView(R.id.tv_update_index)
    TextView mUpdateIndex;
    @BindView(R.id.bangumi_seasons_recycler)
    RecyclerView mBangumiSeasonsRecycler;//分季版本控件
    @BindView(R.id.bangumi_comment_recycler)
    RecyclerView mBangumiCommentRecycler;
    @BindView(R.id.bangumi_recommend_recycler)
    RecyclerView mBangumiRecommendRecycler;
    @BindView(R.id.tv_bangumi_comment_count)
    TextView mBangumiCommentCount;

    private int seasonId;
    private BangumiDetailsInfo.ResultBean _videoDetail;
    private BangumiDetailsCommentInfo.DataBean.PageBean mPageInfo;
    private List<BangumiDetailsCommentInfo.DataBean.RepliesBean> replies = new ArrayList<>();
    private List<BangumiDetailsCommentInfo.DataBean.HotsBean> hotComments = new ArrayList<>();
    private List<BangumiDetailsRecommendInfo.ResultBean.ListBean> bangumiRecommends = new ArrayList<>();
    private JSONObject _videoComments = null;//视频评论
    private JSONArray _videoTagArr = null;//视频标签
    private List<JSONObject> _videoSeasonVerArr = null;//分季版本集合
    private List<JSONObject> _videoResourceArr = null;//资源集合

    @Override
    public int getLayoutId() {
        return R.layout.activity_bangumi_details;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            seasonId = intent.getIntExtra(ConstantUtil.EXTRA_BANGUMI_KEY, 0);
        }
        loadData();
    }

    @Override
    public void loadData(){
        int vid = 41;//vid=41="你的名字 日语中文字幕.mp4"
        Log.d(Const.LOG_TAG,"BangumiDetailsActivity.loadData=>vid:" + vid);
        RetrofitHelper.getZZXAPI().getVideoDetail(41).compose(bindToLifecycle()).
                doOnSubscribe(this::showProgressBar)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"请求视频详情失败," + reply.Message());
                        hideProgressBar();
                        return;
                    }
                    _videoDetail = new BangumiDetailsInfo.ResultBean(reply.GetJObject("video",null));
                    _videoComments = reply.GetJObject("comments",new JSONObject());
                    _videoTagArr = reply.GetJArray("tagArr",new JSONArray());
                    _videoSeasonVerArr = reply.GetJObjArray("seasonVerArr");
                    _videoResourceArr = reply.GetJObjArray("resArr");
                    finishTask();
                },throwable -> {
                    hideProgressBar();
                    Log.e(Const.LOG_TAG,throwable.getMessage());
                });
    }
    public void loadData2()
    {
        RetrofitHelper.getBangumiAPI()
                .getBangumiDetails()
                .compose(bindToLifecycle())
                .doOnSubscribe(this::showProgressBar)
                .flatMap(new Func1<BangumiDetailsInfo, Observable<BangumiDetailsRecommendInfo>>() {
                    @Override
                    public Observable<BangumiDetailsRecommendInfo> call(BangumiDetailsInfo bangumiDetailsInfo) {
                        _videoDetail = bangumiDetailsInfo.getResult();
                        return RetrofitHelper.getBangumiAPI().getBangumiDetailsRecommend();
                    }
                })
                .compose(bindToLifecycle())
                .map(bangumiDetailsRecommendInfo -> bangumiDetailsRecommendInfo.getResult().getList())
                .flatMap(new Func1<List<BangumiDetailsRecommendInfo.ResultBean.ListBean>, Observable<BangumiDetailsCommentInfo>>() {
                    @Override
                    public Observable<BangumiDetailsCommentInfo> call(List<BangumiDetailsRecommendInfo.ResultBean.ListBean> listBeans) {
                        bangumiRecommends.addAll(listBeans);
                        return RetrofitHelper.getBiliAPI().getBangumiDetailsComments();
                    }
                })
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bangumiDetailsCommentInfo -> {
                    hotComments.addAll(bangumiDetailsCommentInfo.getData().getHots());
                    replies.addAll(bangumiDetailsCommentInfo.getData().getReplies());
                    mPageInfo = bangumiDetailsCommentInfo.getData().getPage();
                    finishTask();
                }, throwable -> {
                    LogUtil.all(throwable.getMessage());
                    hideProgressBar();
                });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void finishTask() {
        //设置番剧封面
        Glide.with(this)
                .load(_videoDetail.getCover(true))
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bili_default_image_tv)
                .dontAnimate()
                .into(mBangumiPic);

        //设置背景高斯模糊图片
        Glide.with(this)
                .load(_videoDetail.getCover(true))
                .bitmapTransform(new BlurTransformation(this))
                .into(mBangumiBackgroundImage);
        //设置番剧标题
        mBangumiTitle.setText(_videoDetail.getTitle());
        //设置番剧更新状态
        if (!_videoDetail.getFinish()) {
            mUpdateIndex.setText("更新至第" + _videoDetail.getNewest_ep_index() + "话");
            mBangumiUpdate.setText("连载中");
        } else {
            mUpdateIndex.setText(_videoDetail.getNewest_ep_index() + "话全");
            mBangumiUpdate.setText("已完结" + _videoDetail.getNewest_ep_index() + "话全");
        }
        //设置番剧播放和追番数量
        mBangumiPlay.setText(String.format("播放：%s  追番：%s",
                NumberUtil.converString(_videoDetail.getPlayCount()),
                NumberUtil.converString(_videoDetail.getfavoriteCount())
                ));
        //设置番剧简介
        mBangumiIntroduction.setText(_videoDetail.getDescription());
        //设置评论数量
        mBangumiCommentCount.setText("评论 第1话(" + JsonUtil.GetInt(_videoComments,"totoal",0) + ")");
        //设置标签布局
        List<String> tags = JsonUtil.ToStringList(_videoTagArr);//_videoDetail.getTags();
        mTagsLayout.setAdapter(new TagAdapter<String>(tags) {
            @Override
            public View getView(FlowLayout parent, int position, String tagsBean) {
                TextView mTags = (TextView) LayoutInflater.from(BangumiDetailsActivity.this)
                        .inflate(R.layout.layout_tags_item, parent, false);
                mTags.setText(tagsBean);
                return mTags;
            }
        });
        //设置番剧分季版本
        initSeasonsRecycler();
        //设置番剧选集
        initSelectionRecycler();
        //设置番剧推荐
        initRecommendRecycler();
        //设置番剧评论
        initCommentRecycler();
        //加载完毕隐藏进度条
        hideProgressBar();
    }

    /**
     * 初始化评论recyclerView
     */
    private void initCommentRecycler() {
        mBangumiCommentRecycler.setHasFixedSize(false);
        mBangumiCommentRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mBangumiCommentRecycler.setLayoutManager(mLinearLayoutManager);
        BangumiDetailsCommentAdapter mCommentAdapter = new BangumiDetailsCommentAdapter(mBangumiCommentRecycler, replies);
        HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mCommentAdapter);
        View headView = LayoutInflater.from(this).inflate(R.layout.layout_video_hot_comment_head, mBangumiCommentRecycler, false);
        RecyclerView mHotCommentRecycler = (RecyclerView) headView.findViewById(R.id.hot_comment_recycler);
        mHotCommentRecycler.setHasFixedSize(false);
        mHotCommentRecycler.setNestedScrollingEnabled(false);
        mHotCommentRecycler.setLayoutManager(new LinearLayoutManager(this));
        BangumiDetailsHotCommentAdapter mVideoHotCommentAdapter = new BangumiDetailsHotCommentAdapter(mHotCommentRecycler, hotComments);
        mHotCommentRecycler.setAdapter(mVideoHotCommentAdapter);
        mHeaderViewRecyclerAdapter.addHeaderView(headView);
        mBangumiCommentRecycler.setAdapter(mHeaderViewRecyclerAdapter);
    }

    /**
     * 初始化分季版本recyclerView
     */
    private void initSeasonsRecycler()
    {
        if(_videoSeasonVerArr == null||_videoSeasonVerArr.size()<=0)
            return;

        mBangumiSeasonsRecycler.setHasFixedSize(false);
        mBangumiSeasonsRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mBangumiSeasonsRecycler.setLayoutManager(mLinearLayoutManager);
        BangumiDetailsSeasonsAdapter mBangumiDetailsSeasonsAdapter = new BangumiDetailsSeasonsAdapter(mBangumiSeasonsRecycler, _videoSeasonVerArr);
        mBangumiSeasonsRecycler.setAdapter(mBangumiDetailsSeasonsAdapter);
//        for (int i = 0, size = _videoSeasonVerArr.size(); i < size; i++)
//        {
//            if (_videoSeasonVerArr.get(i).optInt("id").equals(_videoDetail.getSeason_id())) {
//                mBangumiDetailsSeasonsAdapter.notifyItemForeground(i);
//            }
//        }
    }

    /**
     * 初始化选集recyclerView
     */
    private void initSelectionRecycler() {
        if(_videoResourceArr == null||_videoResourceArr.size()<=0)
            return;

        mBangumiSelectionRecycler.setHasFixedSize(false);
        mBangumiSelectionRecycler.setNestedScrollingEnabled(false);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mLinearLayoutManager.setReverseLayout(true);
        mBangumiSelectionRecycler.setLayoutManager(mLinearLayoutManager);
        BangumiDetailsSelectionAdapter mBangumiDetailsSelectionAdapter = new BangumiDetailsSelectionAdapter(mBangumiSelectionRecycler, _videoResourceArr);
        mBangumiSelectionRecycler.setAdapter(mBangumiDetailsSelectionAdapter);
        mBangumiDetailsSelectionAdapter.notifyItemForeground(_videoResourceArr.size() - 1);
        mBangumiSelectionRecycler.scrollToPosition(_videoResourceArr.size() - 1);
        mBangumiDetailsSelectionAdapter.setOnItemClickListener((position, holder) -> {
            JSONObject videoRes = _videoResourceArr.get(position);//获取具体资源数据
            if(!videoRes.has("description"))
                JsonUtil.SetValue(videoRes,"description",_videoDetail.getDescription());
            mBangumiDetailsSelectionAdapter.notifyItemForeground(holder.getLayoutPosition());

            Intent intent = new Intent(this, VideoDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Const.MODULE_PARAMS, videoRes.toString());
            this.startActivity(intent);//跳转并将参数将数据传递给下一个页面
        });
    }

    /**
     * 初始化番剧推荐recyclerView
     */
    private void initRecommendRecycler() {
        mBangumiRecommendRecycler.setHasFixedSize(false);
        mBangumiRecommendRecycler.setNestedScrollingEnabled(false);
        mBangumiRecommendRecycler.setLayoutManager(new GridLayoutManager(BangumiDetailsActivity.this, 3));
        BangumiDetailsRecommendAdapter mBangumiDetailsRecommendAdapter = new BangumiDetailsRecommendAdapter(mBangumiRecommendRecycler, bangumiRecommends);
        mBangumiRecommendRecycler.setAdapter(mBangumiDetailsRecommendAdapter);
    }

    @Override
    public void initToolBar() {
        mToolbar.setTitle("番剧详情");
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //设置Toolbar的透明度
        mToolbar.setBackgroundColor(Color.argb(0, 251, 114, 153));
        //设置StatusBar透明
        SystemBarHelper.immersiveStatusBar(this);
        SystemBarHelper.setPadding(this, mToolbar);
        //获取顶部image高度和toolbar高度
        float imageHeight = getResources().getDimension(R.dimen.bangumi_details_top_layout_height);
        float toolBarHeight = getResources().getDimension(R.dimen.action_bar_default_height);
        mNestedScrollView.setNestedScrollingEnabled(true);
        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //根据NestedScrollView滑动改变Toolbar的透明度
                float offsetY = toolBarHeight - imageHeight;
                //计算滑动距离的偏移量
                float offset = 1 - Math.max((offsetY - scrollY) / offsetY, 0f);
                float absOffset = Math.abs(offset);
                //如果滑动距离大于1就设置完全不透明
                if (absOffset >= 1) {
                    absOffset = 1;
                }
                mToolbar.setBackgroundColor(Color.argb((int) (absOffset * 255), 251, 114, 153));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bangumi_details, menu);
        return true;
    }


    @Override
    public void showProgressBar() {
        mCircleProgressView.setVisibility(View.VISIBLE);
        mCircleProgressView.spin();
        mDetailsLayout.setVisibility(View.GONE);
    }


    @Override
    public void hideProgressBar() {
        mCircleProgressView.setVisibility(View.GONE);
        mCircleProgressView.stopSpinning();
        mDetailsLayout.setVisibility(View.VISIBLE);
    }


    public static void launch(Activity activity, int seasonId) {
        Intent mIntent = new Intent(activity, BangumiDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ConstantUtil.EXTRA_BANGUMI_KEY, seasonId);
        mIntent.putExtras(bundle);
        activity.startActivity(mIntent);
    }
}
