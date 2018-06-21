package com.hotbitmapgg.bilibili.adapter.section;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiAppIndexInfo;
import com.hotbitmapgg.bilibili.module.home.bangumi.BangumiDetailsActivity;
import com.hotbitmapgg.bilibili.module.home.bangumi.NewBangumiSerialActivity;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.bilibili.utils.NumberUtil;
import com.hotbitmapgg.bilibili.widget.sectioned.StatelessSection;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 首页番剧新番连载Section
 * "新番连载"或"正在热播"自定义控件
 */

public class HomeBangumiNewSerialSection extends StatelessSection
{
    private Context mContext;
    private List<BangumiAppIndexInfo.ResultBean.SerializingBean> newBangumiSerials;
    private String _headTitle = "新番连载";
    private String _moreText = "所有连载";
    private String _newest_ep_index_format = "更新至第%d话";
    private String _watching_count_format = "%d人在看";

    public HomeBangumiNewSerialSection(Context context,JSONObject cfg)
    {
        super(R.layout.layout_home_bangumi_new_serial_head, R.layout.layout_home_bangumi_new_serial_body);
        this.mContext = context;
        if(cfg!=null)
        {
            //文本信息配置
            _headTitle = JsonUtil.GetString(cfg,"title","正在热播");
            _moreText = JsonUtil.GetString(cfg,"moreText","更多..");
            _newest_ep_index_format = JsonUtil.GetString(cfg,"newest_ep_index_format","更新至第%d话");
            _watching_count_format = JsonUtil.GetString(cfg,"watching_count_format","%d人在看");

            JSONArray itemArr = cfg.optJSONArray("list");//hotItems.list
            this.newBangumiSerials = BangumiAppIndexInfo.ResultBean.SerializingBean.From(itemArr);
        }
    }

    @Override
    public int getContentItemsTotal() {
        return newBangumiSerials.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new ItemViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        BangumiAppIndexInfo.ResultBean.SerializingBean serializingBean = newBangumiSerials.get(position);

        //从网络下载图片并加载到控件中
        Glide.with(mContext)
                .load(serializingBean.getCover(true))
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.bili_default_image_tv)
                .dontAnimate()
                .into(itemViewHolder.mImage);

        itemViewHolder.mTitle.setText(serializingBean.getTitle());
        if(serializingBean.getWatching_count()>=0) {
            String text = String.format(_watching_count_format,serializingBean.getWatching_count());
            itemViewHolder.mPlay.setText(text);
        }

        if(serializingBean.getNewest_ep_index()>=0) {
            String text = String.format(_newest_ep_index_format,serializingBean.getNewest_ep_index());
            itemViewHolder.mUpdate.setText(text);
        }
        //点击视频项跳转到BangumiDetailsActivity页面
        itemViewHolder.mCardView.setOnClickListener(v -> BangumiDetailsActivity.launch(
                (Activity) mContext, serializingBean.getSeason_id()));
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HomeBangumiNewSerialSection.HeaderViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HomeBangumiNewSerialSection.HeaderViewHolder headerViewHolder
                = (HomeBangumiNewSerialSection.HeaderViewHolder) holder;

        headerViewHolder.tv_title.setText(_headTitle);
        headerViewHolder.mAllSerial.setText(_moreText);
        headerViewHolder.mAllSerial.setOnClickListener(v -> mContext.startActivity(
                new Intent(mContext, NewBangumiSerialActivity.class)));
    }


    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_title)
        TextView tv_title;

        @BindView(R.id.tv_all_serial)
        TextView mAllSerial;

        HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        LinearLayout mCardView;

        @BindView(R.id.item_img)
        ImageView mImage;

        @BindView(R.id.item_title)
        TextView mTitle;

        @BindView(R.id.item_play)
        TextView mPlay;

        @BindView(R.id.item_update)
        TextView mUpdate;


        public ItemViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
