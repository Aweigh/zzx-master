package com.hotbitmapgg.bilibili.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hotbitmapgg.bilibili.utils.CommonUtil;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.bilibili.adapter.helper.AbsRecyclerViewAdapter;
import com.hotbitmapgg.bilibili.entity.search.SearchArchiveInfo;
import com.hotbitmapgg.bilibili.utils.NumberUtil;

import org.json.JSONObject;

import java.util.List;

/**
 * 综合搜索结果Adapter
 * AWEIGH:资源记录项适配器
 */
public class ArchiveResultsAdapter extends AbsRecyclerViewAdapter {
    /*by="Aweigh" date="2018/7/23 15:12"
      [{"id":xxx,"type":xxx,"title":"xxx","cover":"xxxx","playCount":0,"barrageCount":0},{...},..]
      该对象其实是外面传入List<T>对象的引用
      之后可以通过修改引用对象数组内容，加上调用适配器的notifyDataSetChanged通知界面更新
    */
    private List<JSONObject> _recordArr = null;

    public ArchiveResultsAdapter(RecyclerView recyclerView,List<JSONObject> records) {
        super(recyclerView);
        _recordArr = records;//获取数组对象,之后修改该数组就能修改界面了
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext()).
                inflate(R.layout.item_search_archive, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            JSONObject record = _recordArr.get(position);

            String coverURL = JsonUtil.GetZZXImageURL(record,"cover",true);
            Glide.with(getContext())
                    .load(coverURL)//archiveBean.getCover())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bili_default_image_tv)
                    .dontAnimate()
                    .into(itemViewHolder.mVideoPic);

            itemViewHolder.mVideoTitle.setText(JsonUtil.GetString(record,"title"));
            itemViewHolder.mVideoPlayNum.setText(JsonUtil.GetNumberFormat(record,"playCount"));
            itemViewHolder.mVideoReviewNum.setText(JsonUtil.GetNumberFormat(record,"barrageCount"));
            String author = JsonUtil.GetString(record,"author");//UP主名称或用户名称
            if(!CommonUtil.isNullOrWhiteSpace(author))
                itemViewHolder.mUserName.setText(author);
            else
                itemViewHolder.mUserName.setVisibility(View.GONE);
            String duration = JsonUtil.GetString(record,"duration");//视频时长
            if(!CommonUtil.isNullOrWhiteSpace(duration))
                itemViewHolder.mDuration.setText(duration);
            else
                itemViewHolder.mDuration.setText("--:--");
        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return _recordArr!=null ? _recordArr.size() : 0;
    }


    public class ItemViewHolder extends ClickableViewHolder {

        ImageView mVideoPic;
        TextView mVideoTitle;
        TextView mVideoPlayNum;
        TextView mVideoReviewNum;
        TextView mUserName;
        TextView mDuration;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mVideoPic = $(R.id.item_img);
            mVideoTitle = $(R.id.item_title);
            mVideoPlayNum = $(R.id.item_play);
            mVideoReviewNum = $(R.id.item_review);
            mUserName = $(R.id.item_user_name);
            mDuration = $(R.id.item_duration);
        }
    }
}
