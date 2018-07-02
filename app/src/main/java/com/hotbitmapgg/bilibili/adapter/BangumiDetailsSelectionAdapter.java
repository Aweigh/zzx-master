package com.hotbitmapgg.bilibili.adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hotbitmapgg.bilibili.adapter.helper.AbsRecyclerViewAdapter;
import com.hotbitmapgg.bilibili.entity.bangumi.BangumiDetailsInfo;
import com.hotbitmapgg.ohmybilibili.R;

import org.json.JSONObject;

import java.util.List;

/**
 * 番剧选集adapter
 */

public class BangumiDetailsSelectionAdapter extends AbsRecyclerViewAdapter {
    private int layoutPosition = 0;
    private List<JSONObject> _resourceArr = null;

    public BangumiDetailsSelectionAdapter(RecyclerView recyclerView, List<JSONObject> resArr) {
        super(recyclerView);
        this._resourceArr = resArr;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(
                LayoutInflater.from(getContext()).inflate(R.layout.item_bangumi_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            if(_resourceArr!=null && _resourceArr.size()>=position)
            {
                JSONObject item = _resourceArr.get(position);
                itemViewHolder.mIndex.setText(item.optString("name"));
                itemViewHolder.mTitle.setText(item.optString("title"));
            }

            if (position == layoutPosition) {
                itemViewHolder.mCardView.setForeground(
                        getContext().getResources().getDrawable(R.drawable.bg_selection));
                itemViewHolder.mTitle.setTextColor(
                        getContext().getResources().getColor(R.color.colorPrimary));
                itemViewHolder.mIndex.setTextColor(
                        getContext().getResources().getColor(R.color.colorPrimary));
            } else {
                itemViewHolder.mCardView.setForeground(
                        getContext().getResources().getDrawable(R.drawable.bg_normal));
                itemViewHolder.mTitle.setTextColor(
                        getContext().getResources().getColor(R.color.black_alpha_45));
                itemViewHolder.mIndex.setTextColor(
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
        return _resourceArr == null? 0: _resourceArr.size();
    }

    public class ItemViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {

        CardView mCardView;
        TextView mIndex;
        TextView mTitle;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCardView = $(R.id.card_view);
            mIndex = $(R.id.tv_index);
            mTitle = $(R.id.tv_title);
        }
    }
}
