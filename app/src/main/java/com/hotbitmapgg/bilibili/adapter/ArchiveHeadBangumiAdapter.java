package com.hotbitmapgg.bilibili.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;
import com.hotbitmapgg.bilibili.adapter.helper.AbsRecyclerViewAdapter;
import com.hotbitmapgg.bilibili.entity.search.SearchArchiveInfo;

import org.json.JSONObject;

import java.util.List;

/**
 * 综合搜索头部番剧Adapter
 * AWEIGH：影视记录项适配器
 */
public class ArchiveHeadBangumiAdapter extends AbsRecyclerViewAdapter {
    /*by="Aweigh" date="2018/7/23 15:12"
      [{"id":xxx,"type":xxx,"title":"xxx","cover":"xxxx","playCount":0,"barrageCount":0},{...},..]
      该对象其实是外面传入List<T>对象的引用
      之后可以通过修改引用对象数组内容，加上调用适配器的notifyDataSetChanged通知界面更新
    */
    private List<JSONObject> _recordArr = null;

    public ArchiveHeadBangumiAdapter(RecyclerView recyclerView,List<JSONObject> records) {
        super(recyclerView);
        this._recordArr = records;//获取数组对象,之后修改该数组就能修改界面了
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext()).
                inflate(R.layout.item_archive_head_bangumi, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            JSONObject record = _recordArr.get(position);

            String coverURL = JsonUtil.GetZZXImageURL(record,"cover",true);
            Glide.with(getContext())
                    .load(coverURL)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bili_default_image_tv)
                    .dontAnimate()
                    .into(itemViewHolder.mBangumiPic);

            itemViewHolder.mBangumiTitle.setText(JsonUtil.GetString(record,"title"));
            if (JsonUtil.GetBool(record,"isFinish",false)) {
                itemViewHolder.mBangumiCount.setText( JsonUtil.GetInt(record,"newest_ep_index",-1)+ "话全");
            } else {
                itemViewHolder.mBangumiCount.setText( "更新至第" + JsonUtil.GetInt(record,"newest_ep_index",-1) + "话");
            }
            itemViewHolder.mBangumiDetails.setText(JsonUtil.GetString(record,"desc"));
        }

        super.onBindViewHolder(holder, position);
    }


    @Override
    public int getItemCount() {
        return _recordArr.size();
    }


    public class ItemViewHolder extends ClickableViewHolder {

        ImageView mBangumiPic;
        TextView mBangumiTitle;
        TextView mBangumiDetails;
        TextView mBangumiCount;


        public ItemViewHolder(View itemView) {
            super(itemView);
            mBangumiPic = $(R.id.item_img);
            mBangumiTitle = $(R.id.item_title);
            mBangumiDetails = $(R.id.item_details);
            mBangumiCount = $(R.id.item_count);
        }
    }
}
