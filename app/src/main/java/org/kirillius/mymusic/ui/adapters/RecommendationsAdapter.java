package org.kirillius.mymusic.ui.adapters;

import android.support.v7.widget.RecyclerView;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.model.VKApiAudio;

import org.kirillius.mymusic.R;

/**
 * Created by Kirill on 14.02.2016.
 */
public class RecommendationsAdapter extends PlaylistAdapter {

    private int userId;

    public RecommendationsAdapter() {
        super();
        userId = Integer.valueOf(VKAccessToken.currentToken().userId);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ItemHolder) {
            ItemHolder vh = (ItemHolder) holder;

            VKApiAudio track = getItem(position);

            if (track.owner_id != userId) {
                vh.action.setImageResource(R.drawable.ic_playlist_add_black_24dp);
            } else {
                vh.action.setImageResource(R.drawable.ic_playlist_add_check_black_24dp);
            }
        }
    }
}
