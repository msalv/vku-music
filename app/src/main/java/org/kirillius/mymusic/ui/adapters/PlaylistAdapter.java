package org.kirillius.mymusic.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vk.sdk.api.model.VKApiAudio;

import org.kirillius.mymusic.R;
import org.kirillius.mymusic.core.DurationFormatter;

/**
 * Created by Kirill on 14.02.2016.
 */
public class PlaylistAdapter extends EndlessScrollAdapter<VKApiAudio> {

    private StringBuilder mStringBuilder;

    private OnItemClickListener onActionButtonClicked;
    public int currentItemId = -1;

    public PlaylistAdapter() {
        mStringBuilder = new StringBuilder();
    }

    public void setOnActionButtonClicked(OnItemClickListener onActionButtonClicked) {
        this.onActionButtonClicked = onActionButtonClicked;
    }

    @Override
    public EndlessScrollAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        Holder holder;

        switch (viewType) {
            case ITEM_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false);
                holder = new ItemHolder(view, this.clickListener, this.onActionButtonClicked);
                break;

            default:
                holder = super.onCreateViewHolder(parent, viewType);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if ( holder instanceof ItemHolder ) {
            ItemHolder vh = (ItemHolder) holder;

            VKApiAudio track = getItem(position);

            if ( position == currentItemId ) {
                vh.buttons_container.setVisibility(View.VISIBLE);
            }
            else {
                vh.buttons_container.setVisibility(View.GONE);
            }

            vh.artist.setText(track.artist);
            vh.track.setText(track.title);

            vh.duration.setText(DurationFormatter.format(mStringBuilder, track.duration));
        }
    }

    /**
     * Audio track view holder
     */
    public static class ItemHolder extends EndlessScrollAdapter.ItemHolder {

        public TextView artist;
        public TextView track;
        public TextView duration;

        public View buttons_container;

        public ItemHolder(View itemView, OnItemClickListener clickListener, final OnItemClickListener onAction) {
            super(itemView, clickListener);

            artist = (TextView) itemView.findViewById(R.id.artist);
            track = (TextView) itemView.findViewById(R.id.track);
            duration = (TextView) itemView.findViewById(R.id.duration);

            buttons_container = itemView.findViewById(R.id.buttons);

            View action = itemView.findViewById(R.id.action);

            if ( onAction != null ) {
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAction.onItemClick(v, getLayoutPosition());
                    }
                });
            }
        }
    }
}
