package com.hotbitmapgg.bilibili.module.video;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.flyco.tablayout.SlidingTabLayout;
import com.hotbitmapgg.bilibili.base.RxBaseActivity;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.event.AppBarStateChangeEvent;
import com.hotbitmapgg.bilibili.network.auxiliary.Const;
import com.hotbitmapgg.bilibili.utils.ConstantUtil;
import com.hotbitmapgg.bilibili.utils.DisplayUtil;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.PathUtil;
import com.hotbitmapgg.bilibili.utils.SystemBarHelper;
import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.bilibili.entity.video.VideoDetailsInfo;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 视频详情界面
 */
public class VideoDetailsActivity extends RxBaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.video_preview)
    ImageView mVideoPreview;
    @BindView(R.id.tab_layout)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.fab)
    FloatingActionButton mFAB;//开始播放按钮
    @BindView(R.id.app_bar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.tv_player)
    TextView mTvPlayer;
    @BindView(R.id.tv_av)
    TextView mAvText;

    private int _id = 0;
    private VideoDetailsInfo.DataBean mVideoDetailsInfo;
    private List<String> titles = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    JSONObject _resource = null;
    JSONObject _resComments = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_video_details;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null)
        {//获取从前一个Activity传递过来的参数
            String params = intent.getStringExtra(Const.MODULE_PARAMS);
            _resource = JsonUtil.Parse(params,new JSONObject());
            _id = _resource.optInt("id");
            String imgUrl = _resource.optString("cover");
            if(!TextUtils.isEmpty(imgUrl))
            {
                Glide.with(VideoDetailsActivity.this)
                        .load(PathUtil.GetZZXImageURL(imgUrl,true))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.bili_default_image_tv)
                        .dontAnimate()
                        .into(mVideoPreview);
            }
        }
        loadData();//从网络获取数据
        mFAB.setClickable(false);
        mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_20)));
        mFAB.setTranslationY(-getResources().getDimension(R.dimen.floating_action_button_size_half));
        //播放按钮点击事件,切换到视频播放界面
        mFAB.setOnClickListener(v -> VideoPlayerActivity.launch(VideoDetailsActivity.this, _resource));
        mAppBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> setViewsTranslation(verticalOffset));
        mAppBarLayout.addOnOffsetChangedListener(new AppBarStateChangeEvent() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state, int verticalOffset) {
                if (state == State.EXPANDED) {
                    //展开状态
                    mTvPlayer.setVisibility(View.GONE);
                    mAvText.setVisibility(View.VISIBLE);
                    mToolbar.setContentInsetsRelative(DisplayUtil.dp2px(VideoDetailsActivity.this, 15), 0);
                } else if (state == State.COLLAPSED) {
                    //折叠状态
                    mTvPlayer.setVisibility(View.VISIBLE);
                    mAvText.setVisibility(View.GONE);
                    mToolbar.setContentInsetsRelative(DisplayUtil.dp2px(VideoDetailsActivity.this, 150), 0);
                } else {
                    mTvPlayer.setVisibility(View.GONE);
                    mAvText.setVisibility(View.VISIBLE);
                    mToolbar.setContentInsetsRelative(DisplayUtil.dp2px(VideoDetailsActivity.this, 15), 0);
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initToolBar() {
        mToolbar.setTitle("");//工具栏标题设置为空,AWEIGH:好像设置了也看不到
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //设置还没收缩时状态下字体颜色
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        //设置收缩后Toolbar上字体的颜色
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        //设置StatusBar透明
        SystemBarHelper.immersiveStatusBar(this);
        SystemBarHelper.setHeightAndPadding(this, mToolbar);

        String name = JsonUtil.GetDefStrIfEmpty(_resource,"name","");
        String title = JsonUtil.GetDefStrIfEmpty(_resource,"title","");
        mAvText.setText((name + " " + title).trim());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setViewsTranslation(int target) {
        mFAB.setTranslationY(target);
        if (target == 0) {
            showFAB();
        } else if (target < 0) {
            hideFAB();
        }
    }

    public static void launch(Activity activity, int vid, String imgUrl) {
        Intent intent = new Intent(activity, VideoDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ConstantUtil.EXTRA_AV, vid);
        intent.putExtra(ConstantUtil.EXTRA_IMG_URL, imgUrl);
        activity.startActivity(intent);
    }

    private void showFAB() {
        mFAB.animate().scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator())
                .start();
        mFAB.setClickable(true);
    }

    private void hideFAB() {
        mFAB.animate().scaleX(0f).scaleY(0f)
                .setInterpolator(new AccelerateInterpolator())
                .start();
        mFAB.setClickable(false);
    }

    @Override
    public void loadData() {
        RetrofitHelper.getZZXAPI()
                .getResourceDetail(_id,AppContext.AccountID)
                .compose(this.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"请求资源详情失败," + reply.Message());
                        mFAB.setClickable(false);//disabled 播放按钮
                        mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_20)));
                        return;
                    }
                    JSONObject resTemp = reply.GetJObject("resource",null);
                    JsonUtil.Combine(_resource,resTemp);//将旧对象与新请求到的数据对象合并,即更新当前json对象
                    _resComments = reply.GetJObject("comments",new JSONObject());
                    finishTask();
                }, throwable -> {
                    mFAB.setClickable(false);//disabled 播放按钮
                    mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.gray_20)));
                });
    }


    @Override
    public void finishTask() {
        mFAB.setClickable(true);
        mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        mCollapsingToolbarLayout.setTitle("");
        //设置和声明两个Fragment:简介和评论(xxxx)
        VideoIntroductionFragment mVideoIntroductionFragment = VideoIntroductionFragment.newInstance(_resource);
        VideoCommentFragment mVideoCommentFragment = VideoCommentFragment.newInstance(_id);
        fragments.add(mVideoIntroductionFragment);//添加简介区域
        fragments.add(mVideoCommentFragment);//添加评论区域
        titles.add("简介");
        titles.add("评论" + "(" + JsonUtil.GetInt(_resComments,"totoal",0) + ")");
        setPagerTitle();
    }

    private void setPagerTitle() {
        VideoDetailsPagerAdapter mAdapter = new VideoDetailsPagerAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mSlidingTabLayout.setViewPager(mViewPager);
        measureTabLayoutTextWidth(0);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                measureTabLayoutTextWidth(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void measureTabLayoutTextWidth(int position) {
        String title = titles.get(position);
        TextView titleView = mSlidingTabLayout.getTitleView(position);
        TextPaint paint = titleView.getPaint();
        float textWidth = paint.measureText(title);
        mSlidingTabLayout.setIndicatorWidth(textWidth / 3);
    }

    public static class VideoDetailsPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments;
        private List<String> titles;

        VideoDetailsPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
            super(fm);
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
