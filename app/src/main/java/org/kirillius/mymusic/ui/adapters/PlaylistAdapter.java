package org.kirillius.mymusic.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vk.sdk.api.model.VKApiAudio;

import org.kirillius.mymusic.R;
import org.kirillius.mymusic.core.DurationFormatter;

/**
 * Created by Kirill on 14.02.2016.
 */
public class PlaylistAdapter extends EndlessScrollAdapter<VKApiAudio> {

    private StringBuilder mStringBuilder;

    public PlaylistAdapter() {
        mStringBuilder = new StringBuilder();
    }

    @Override
    public EndlessScrollAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        Holder holder;

        switch (viewType) {
            case ITEM_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_item, parent, false);
                holder = new ItemHolder(view, this.clickListener);
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

        public ItemHolder(View itemView, OnItemClickListener clickListener) {
            super(itemView, clickListener);

            artist = (TextView) itemView.findViewById(R.id.artist);
            track = (TextView) itemView.findViewById(R.id.track);
            duration = (TextView) itemView.findViewById(R.id.duration);
        }
    }
}
