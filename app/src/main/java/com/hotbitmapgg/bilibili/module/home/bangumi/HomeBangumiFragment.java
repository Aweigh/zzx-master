package com.hotbitmapgg.bilibili.module.home.bangumi;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hotbitmapgg.bilibili.adapter.section.HomeBangumiBannerSection;
import com.hotbitmapgg.bilibili.adapter.section.HomeBangumiBobySection;
import com.hotbitmapgg.bilibili.adapter.section.HomeBangumiItemSection;
import com.hotbitmapgg.bilibili.adapter.section.HomeBangumiNewSerialSection;
import com.hotbitmapgg.bilibili.adapter.section.HomeBangumiRecommendSection;
import com.hotbitmapgg.bilibili.base.RxLazyFragment;
import com.hotbitmapgg.bilibili.entity.AppContext;
import com.hotbitmapgg.bilibili.entity.ServerReply;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiAppIndexInfo;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiRecommendInfo;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.SnackbarUtil;
import com.hotbitmapgg.bilibili.widget.CustomEmptyView;
import com.hotbitmapgg.bilibili.widget.banner.BannerEntity;
import com.hotbitmapgg.bilibili.widget.sectioned.SectionedRecyclerViewAdapter;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hcc on 16/8/4 21:18
 * 100332338@qq.com
 * <p/>
 * 首页番剧界面
 */
public class HomeBangumiFragment extends RxLazyFragment {
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新内容的容器控件
    @BindView(R.id.recycle)
    RecyclerView mRecyclerView;//用于显示推荐页面的内容,包含:顶部的广告轮播,内容项列表(标题、3列视频项、内容栏底部)
    @BindView(R.id.empty_layout)
    CustomEmptyView mCustomEmptyView;//加载失败时用于显示错误信息和显示失败图片

    private boolean mIsRefreshing = false;
    private List<BannerEntity> bannerList = new ArrayList<>();//广告轮播对象集合
    private SectionedRecyclerViewAdapter mSectionedRecyclerViewAdapter;//RecyclerView控件的适配器
    private List<BangumiAppIndexInfo.ResultBean.AdBean.HeadBean> banners = new ArrayList<>();
    private List<BangumiAppIndexInfo.ResultBean.AdBean.BodyBean> bangumibobys = new ArrayList<>();
    private JSONObject _hotItems = null;//正在热播
    private JSONObject _lastestItems = null;//最新上映
    private JSONObject _recommendItems = null;//热门/推荐
    private int _catalogID = 0;//显示的类型ID

    public static HomeBangumiFragment newInstance(int cid) {
        HomeBangumiFragment inst = new HomeBangumiFragment();
        inst._catalogID = cid;
        return inst;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_home_bangumi;
    }

    @Override
    public void finishCreateView(Bundle state) {
        isPrepared = true;
        lazyLoad();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        initRefreshLayout();
        initRecyclerView();
        isPrepared = false;
    }

    @Override
    protected void initRecyclerView() {
        mSectionedRecyclerViewAdapter = new SectionedRecyclerViewAdapter();
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mSectionedRecyclerViewAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 3;
                    default:
                        return 1;
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(mSectionedRecyclerViewAdapter);
        setRecycleNoScroll();
    }

    @Override
    protected void initRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            mIsRefreshing = true;
            loadData();
        });
        //监控下拉刷新事件
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            clearData();
            loadData();
        });
    }

    private void clearData()
    {
        mIsRefreshing = true;
        if(banners!=null)
            banners.clear();
        if(bannerList!=null)
            bannerList.clear();
        if(bangumibobys!=null)
            bangumibobys.clear();

        _hotItems = null;
        _lastestItems = null;
        _recommendItems = null;
        mSectionedRecyclerViewAdapter.removeAllSections();
    }

    @Override
    protected void loadData()
    {
        Log.d(Const.LOG_TAG,"HomeBangumiFragment.loadData=>cid:" + _catalogID);
        RetrofitHelper.getZZXAPI().getVideoHome(_catalogID).compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"请求视频首页失败," + reply.Message());
                        initEmptyView();
                        return;
                    }
                    bannerList = BannerEntity.From(reply.GetJArray("adHead",null));
                    bangumibobys = BangumiAppIndexInfo.ResultBean.AdBean.BodyBean.From(reply.GetJArray("adBody",null));

                    /*
                    {
                        "hotItems":{"list"[],"count":xx},
                        "latestItems":{"list"[],"count":xx},
                        "recommendItems":{"list"[],"count":xx},
                        "navigations":[]
                    }
                    */
                    _hotItems = reply.GetJObject("hotItems",null);//正在热播
                    _lastestItems = reply.GetJObject("latestItems",null);//最新上映
                    _recommendItems = reply.GetJObject("recommendItems",null);//热门/推荐
                    finishTask();
                },throwable -> {
                    initEmptyView();
                    Log.e(Const.LOG_TAG,throwable.getMessage());
                });
    }

    @Override
    protected void finishTask() {
        mSwipeRefreshLayout.setRefreshing(false);
        mIsRefreshing = false;
        hideEmptyView();

        if(bannerList!=null && !bannerList.isEmpty())
            mSectionedRecyclerViewAdapter.addSection(new HomeBangumiBannerSection(bannerList));

        mSectionedRecyclerViewAdapter.addSection(new HomeBangumiItemSection(getActivity()));

        if(_hotItems!=null && _hotItems.has("list"))
        {//构建"新番连载"或"正在热播"区域
            JsonUtil.CopyAttribute(AppContext.VideoPageCfg,"hot_title",_hotItems,"title");
            HomeBangumiNewSerialSection section = new HomeBangumiNewSerialSection(getActivity(), _hotItems);
            mSectionedRecyclerViewAdapter.addSection(section);
        }

        if (bangumibobys!=null && !bangumibobys.isEmpty())
            mSectionedRecyclerViewAdapter.addSection(new HomeBangumiBobySection(getActivity(), bangumibobys));

        if(_lastestItems!=null && _lastestItems.has("list"))
        {//构建"x月新番"或"最新上映"区域
            JsonUtil.CopyAttribute(AppContext.VideoPageCfg,"latest_title",_lastestItems,"title");
            HomeBangumiNewSerialSection section = new HomeBangumiNewSerialSection(getActivity(), _lastestItems);
            mSectionedRecyclerViewAdapter.addSection(section);
        }

        if(_recommendItems!=null && _recommendItems.has("list"))
        {//构建"番剧推荐"或"热门/推荐"区域
            JsonUtil.CopyAttribute(AppContext.VideoPageCfg,"recommend_title",_recommendItems,"title");
            JSONArray itemArr = _recommendItems.optJSONArray("list");//recommendItems.list
            String title = JsonUtil.GetString(_recommendItems,"title","热门/推荐");

            List<BangumiRecommendInfo.ResultBean> objItemArr = BangumiRecommendInfo.ResultBean.From(itemArr);
            if(objItemArr!=null && objItemArr.size()>0)
            {
                HomeBangumiRecommendSection section = new HomeBangumiRecommendSection(getActivity(), objItemArr);
                section.setText(title);
                mSectionedRecyclerViewAdapter.addSection(section);
            }
        }

        mSectionedRecyclerViewAdapter.notifyDataSetChanged();
    }

    public void initEmptyView() {
        mSwipeRefreshLayout.setRefreshing(false);
        mCustomEmptyView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mCustomEmptyView.setEmptyImage(R.drawable.img_tips_error_load_error);
        mCustomEmptyView.setEmptyText("加载失败~(≧▽≦)~啦啦啦.");
        SnackbarUtil.showMessage(mRecyclerView, "数据加载失败,请重新加载或者检查网络是否链接");
    }

    public void hideEmptyView() {
        mCustomEmptyView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void setRecycleNoScroll() {
        mRecyclerView.setOnTouchListener((v, event) -> mIsRefreshing);
    }
}
