package com.hotbitmapgg.bilibili.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hotbitmapgg.bilibili.adapter.helper.AbsRecyclerViewAdapter;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiDetailsInfo;
import com.hotbitmapgg.bilibili.utils.JsonUtil;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * 番剧详情分季版本adapter
 */

public class BangumiDetailsSeasonsAdapter extends AbsRecyclerViewAdapter {
    private int layoutPosition = 0;
    private List<JSONObject> _seasonVerArr;

    public BangumiDetailsSeasonsAdapter(RecyclerView recyclerView, List<JSONObject> seasonVerArr) {
        super(recyclerView);
        this._seasonVerArr = seasonVerArr;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext())
                .inflate(R.layout.item_bangumi_details_seasons, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            if(_seasonVerArr!=null && _seasonVerArr.size()>position)
            {
                String name = _seasonVerArr.get(position).optString("name");
                itemViewHolder.mSeasons.setText(name);
            }

            if (position == layoutPosition) {
                itemViewHolder.mCardView.setForeground(
                        getContext().getResources().getDrawable(R.drawable.bg_selection));
                itemViewHolder.mSeasons.setTextColor(
                        getContext().getResources().getColor(R.color.colorPrimary));
            } else {
                itemViewHolder.mCardView.setForeground(
                        getContext().getResources().getDrawable(R.drawable.bg_normal));
                itemViewHolder.mSeasons.setTextColor(
                        getContext().getResources().getColor(R.color.font_normal));
            }
        }
        super.onBindViewHolder(holder, position);
    }

    public void notifyItemForeground(int clickPosition) {
        layoutPosition = clickPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return _seasonVerArr == null ? 0 : _seasonVerArr.size();
    }

    private class ItemViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {

        CardView mCardView;
        TextView mSeasons;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCardView = $(R.id.card_view);
            mSeasons = $(R.id.tv_seasons);
        }
    }
}
