package com.hotbitmapgg.bilibili.module.search;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.util.Log;

import com.hotbitmapgg.bilibili.adapter.ArchiveHeadBangumiAdapter;
import com.hotbitmapgg.bilibili.adapter.ArchiveResultsAdapter;
import com.hotbitmapgg.bilibili.adapter.helper.EndlessRecyclerOnScrollListener;
import com.hotbitmapgg.bilibili.adapter.helper.HeaderViewRecyclerAdapter;
import com.hotbitmapgg.bilibili.base.RxLazyFragment;
import com.hotbitmapgg.bilibili.entity.search.SearchArchiveInfo;
import com.hotbitmapgg.bilibili.module.home.bangumi.BangumiDetailsActivity;
import com.hotbitmapgg.bilibili.module.video.VideoDetailsActivity;
import com.hotbitmapgg.bilibili.network.RetrofitHelper;
import com.hotbitmapgg.bilibili.utils.Const;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.bilibili.entity.ServerReply;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 综合搜索结果界面
 */
public class ArchiveResultsFragment extends RxLazyFragment {
    @BindView(R.id.recycle)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    ImageView mEmptyView;
    @BindView(R.id.iv_search_loading)
    ImageView mLoadingView;

    private String _keyword = null;
    private int pageNum = 1;
    private int pageSize = 10;
    private View loadMoreView;
    private HeaderViewRecyclerAdapter mHeaderViewRecyclerAdapter;
    private ArchiveHeadBangumiAdapter archiveHeadBangumiAdapter;
    /*by="Aweigh" date="2018/7/23 14:12"
      这里必须初始化一个数组对象，下面是通过修改该数组内容并加上界面变化通知去实现界面更新的
    */
    private List<JSONObject> _videoArr = new ArrayList<>();//影视记录列表
    private List<JSONObject> _resourceArr = new ArrayList<>();//资源记录列表

    public static ArchiveResultsFragment newInstance(String keyword) {
        ArchiveResultsFragment fragment = new ArchiveResultsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Const.MODULE_PARAMS, keyword);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_archive_results;
    }

    @Override
    public void finishCreateView(Bundle state) {
        _keyword = getArguments().getString(Const.MODULE_PARAMS);
        isPrepared = true;
        lazyLoad();
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        }
        initRecyclerView();
        loadData();
        isPrepared = false;
    }

    @Override
    protected void initRecyclerView()
    {
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        ArchiveResultsAdapter mAdapter = new ArchiveResultsAdapter(mRecyclerView, _videoArr);//将影视记录列表对象传递给适配器
        mHeaderViewRecyclerAdapter = new HeaderViewRecyclerAdapter(mAdapter);
        mRecyclerView.setAdapter(mHeaderViewRecyclerAdapter);
        createHeadView();
        createLoadMoreView();
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int i) {
                pageNum++;
                loadData();
                loadMoreView.setVisibility(View.VISIBLE);
            }
        });
        mAdapter.setOnItemClickListener((position, holder) -> {
            JSONObject record = _videoArr.get(position);//下面根据不同的记录类型，跳转到视频或影视详情页
            int type = JsonUtil.GetInt(record,"type",0x0);
            if(type == Const.ITEM_RESOURCE){
                VideoDetailsActivity.launch(getActivity(),record);
            }
            else if(type == Const.ITEM_VIDEO){
                long videoID = JsonUtil.GetInt64(record,"id",0x0);
                BangumiDetailsActivity.launch(getActivity(),videoID);
            }
        });
    }


    @Override
    protected void loadData() {
        RetrofitHelper.getZZXAPI().searchRecordBy(_keyword,0x3,pageNum,pageSize).
                compose(bindToLifecycle()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                subscribe(response -> {
                    ServerReply reply = new ServerReply(response);
                    if(!reply.IsSucceed()){
                        Log.e(Const.LOG_TAG,"根据关键字搜索失败," + reply.Message());
                        return;
                    }

                    _videoArr.clear();
                    _resourceArr.clear();
                    List<JSONObject> tempArr = reply.GetJObjArray("itemArr");
                    if(tempArr == null||tempArr.size()<pageSize){
                        loadMoreView.setVisibility(View.GONE);
                        mHeaderViewRecyclerAdapter.removeFootView();
                    }
                    else {//将获取到的数据添加到数组对象容器中,切记不能给_recordArr重新声明对象实例
                        for (JSONObject item:tempArr){
                            int type = JsonUtil.GetInt(item,"type",Const.ITEM_UNKNOWN);
                            if(type == Const.ITEM_VIDEO){
                                _videoArr.add(item);
                            }
                            else {
                                _resourceArr.add(item);
                            }
                        }
                    }
                    finishTask();
                },throwable -> {
                    Log.e(Const.LOG_TAG,throwable.getMessage());
                    showEmptyView();
                    loadMoreView.setVisibility(View.GONE);
                });
    }


    @Override
    protected void finishTask() {
        if (_videoArr != null) {
            if (_videoArr.size() == 0) {
                showEmptyView();
            } else {
                hideEmptyView();
            }
        }
        /*by="Aweigh" date="2018/7/23 11:58"
          下面是通知界面_recordArr数组中的数据发生变化了，要更新界面。
        */
        loadMoreView.setVisibility(View.GONE);
        archiveHeadBangumiAdapter.notifyDataSetChanged();
        if (pageNum * pageSize - pageSize - 1 > 0) {
            mHeaderViewRecyclerAdapter.notifyItemRangeChanged(pageNum * pageSize - pageSize - 1, pageSize);
        } else {
            mHeaderViewRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void createHeadView() {
        /*by="Aweigh" date="2018/7/23 14:05"
          创建和绑定视图头部：默认排序+全部时长+全部分区
        */
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_search_archive_head_view, mRecyclerView, false);
        RecyclerView mHeadBangumiRecycler = (RecyclerView) headView.findViewById(R.id.search_archive_bangumi_head_recycler);
        mHeadBangumiRecycler.setHasFixedSize(false);
        mHeadBangumiRecycler.setNestedScrollingEnabled(false);
        mHeadBangumiRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        archiveHeadBangumiAdapter = new ArchiveHeadBangumiAdapter(mHeadBangumiRecycler, _resourceArr);//视图前面显示影视记录集合
        mHeadBangumiRecycler.setAdapter(archiveHeadBangumiAdapter);
        mHeaderViewRecyclerAdapter.addHeaderView(headView);
    }

    private void createLoadMoreView() {
        loadMoreView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_load_more, mRecyclerView, false);
        mHeaderViewRecyclerAdapter.addFooterView(loadMoreView);
        loadMoreView.setVisibility(View.GONE);
    }

    public void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
    }

    public void hideEmptyView() {
        mEmptyView.setVisibility(View.GONE);
    }
}
