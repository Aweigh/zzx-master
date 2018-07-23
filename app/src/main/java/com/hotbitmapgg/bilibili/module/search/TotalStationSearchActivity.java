package com.hotbitmapgg.bilibili.module.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyco.tablayout.SlidingTabLayout;
import com.hotbitmapgg.bilibili.base.RxBaseActivity;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.entity.search.SearchArchiveInfo;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.KeyBoardUtil;
import com.hotbitmapgg.bilibili.utils.StatusBarUtil;
import com.hotbitmapgg.bilibili.widget.NoScrollViewPager;
import com.hotbitmapgg.ohmybilibili.R;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 全站搜索界面
 */
public class TotalStationSearchActivity extends RxBaseActivity {
    @BindView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.view_pager)
    NoScrollViewPager mViewPager;
    @BindView(R.id.iv_search_loading)
    ImageView mLoadingView;
    @BindView(R.id.search_img)
    ImageView mSearchBtn;
    @BindView(R.id.search_edit)
    EditText mSearchEdit;
    @BindView(R.id.search_text_clear)
    ImageView mSearchTextClear;
    @BindView(R.id.search_layout)
    LinearLayout mSearchLayout;

    private String _queryKeyword = null;//查询关键字
    private AnimationDrawable mAnimationDrawable;
    private List<String> titles = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    public int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public void initToolBar() {
        //设置6.0以上StatusBar字体颜色
        StatusBarUtil.from(this).setLightStatusBar(true).process();
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null)
            _queryKeyword = intent.getStringExtra(Const.MODULE_PARAMS);//获取查询关键字字符串

        mLoadingView.setImageResource(R.drawable.anim_search_loading);//设置动画gif图片
        mAnimationDrawable = (AnimationDrawable) mLoadingView.getDrawable();
        //showSearchAnim();//显示搜索动画
        mSearchEdit.clearFocus();
        mSearchEdit.setText(_queryKeyword);
        finishTask();
        search();
        setUpEditText();
    }

    private void setUpEditText() {
        RxTextView.textChanges(mSearchEdit)
                .compose(this.bindToLifecycle())
                .map(CharSequence::toString)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (!TextUtils.isEmpty(s)) {
                        mSearchTextClear.setVisibility(View.VISIBLE);
                    } else {
                        mSearchTextClear.setVisibility(View.GONE);
                    }
                });
        RxView.clicks(mSearchTextClear)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> mSearchEdit.setText(""));

        RxTextView.editorActions(mSearchEdit)
                .filter(integer -> !TextUtils.isEmpty(mSearchEdit.getText().toString().trim()))
                .filter(integer -> integer == EditorInfo.IME_ACTION_SEARCH)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer integer) {
                        return Observable.just(mSearchEdit.getText().toString().trim());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    KeyBoardUtil.closeKeybord(mSearchEdit, TotalStationSearchActivity.this);
                    showSearchAnim();
                    clearData();
                    _queryKeyword = s;
                    finishTask();
                });
    }


    private void search() {
        RxView.clicks(mSearchBtn)
                .throttleFirst(2, TimeUnit.SECONDS)
                .map(aVoid -> mSearchEdit.getText().toString().trim())
                .filter(s -> !TextUtils.isEmpty(s))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    KeyBoardUtil.closeKeybord(mSearchEdit, TotalStationSearchActivity.this);
                    showSearchAnim();
                    clearData();
                    _queryKeyword = s;
                    finishTask();
                });
    }

    private void clearData() {
        titles.clear();
        fragments.clear();
    }

    //显示界面,创建子标签页等
    @Override
    public void finishTask() {
        //hideSearchAnim();//关闭搜索动画
        titles.add(JsonUtil.GetString(AppContext.TotalStationSearchCfg,"all_title"));
        titles.add(JsonUtil.GetString(AppContext.TotalStationSearchCfg,"video_title"));
        titles.add(JsonUtil.GetString(AppContext.TotalStationSearchCfg,"resource_title"));

        ArchiveResultsFragment archiveResultsFragment = ArchiveResultsFragment.newInstance(_queryKeyword);
        BangumiResultsFragment bangumiResultsFragment = BangumiResultsFragment.newInstance(_queryKeyword);
        //UpperResultsFragment upperResultsFragment = UpperResultsFragment.newInstance(_queryKeyword);
        MovieResultsFragment movieResultsFragment = MovieResultsFragment.newInstance(_queryKeyword);

        fragments.add(archiveResultsFragment);
        fragments.add(bangumiResultsFragment);
        //fragments.add(upperResultsFragment);
        fragments.add(movieResultsFragment);

        SearchTabAdapter mAdapter = new SearchTabAdapter(getSupportFragmentManager(), titles, fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(titles.size());
        mSlidingTabLayout.setViewPager(mViewPager);
        measureTabLayoutTextWidth(0);
        mSlidingTabLayout.setCurrentTab(0);//切换到第一个Tab页
        mAdapter.notifyDataSetChanged();
        mSlidingTabLayout.notifyDataSetChanged();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                measureTabLayoutTextWidth(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void measureTabLayoutTextWidth(int position) {
        String title = titles.get(position);
        TextView titleView = mSlidingTabLayout.getTitleView(position);
        TextPaint paint = titleView.getPaint();
        float textWidth = paint.measureText(title);
        mSlidingTabLayout.setIndicatorWidth(textWidth / 3);
    }
    //显示搜索动画
    private void showSearchAnim() {
        mLoadingView.setVisibility(View.VISIBLE);
        mSearchLayout.setVisibility(View.GONE);
        mAnimationDrawable.start();
    }
    //关闭搜索动画
    private void hideSearchAnim() {
        mLoadingView.setVisibility(View.GONE);
        mSearchLayout.setVisibility(View.VISIBLE);
        mAnimationDrawable.stop();
    }
    //显示搜索不到数据
    public void setEmptyLayout() {
        mLoadingView.setVisibility(View.VISIBLE);
        mSearchLayout.setVisibility(View.GONE);
        mLoadingView.setImageResource(R.drawable.search_failed);//设置搜索失败图片
    }

    @OnClick(R.id.search_back)
    void OnBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
            mAnimationDrawable = null;
        }
        super.onBackPressed();
    }


    public static void launch(Activity activity, String params) {
        Intent mIntent = new Intent(activity, TotalStationSearchActivity.class);
        mIntent.putExtra(Const.MODULE_PARAMS, params);
        activity.startActivity(mIntent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimationDrawable != null) {
            mAnimationDrawable.stop();
            mAnimationDrawable = null;
        }
    }


    private static class SearchTabAdapter extends FragmentStatePagerAdapter {
        private List<String> titles;
        private List<Fragment> fragments;

        SearchTabAdapter(FragmentManager fm, List<String> titles, List<Fragment> fragments) {
            super(fm);
            this.titles = titles;
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
