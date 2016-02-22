package org.kirillius.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;

import org.kirillius.mymusic.R;
import org.kirillius.mymusic.ui.adapters.EndlessScrollAdapter;

import java.lang.ref.WeakReference;

/**
 * Created by Kirill on 14.02.2016.
 */
public class RecommendationsFragment extends PlaylistFragment {

    public final static String TAG = "RecommendationsFragment";

    public final static String ARG_TRACK = "ArgTrack";

    private String song_id = null;

    public static RecommendationsFragment createInstance(VKApiAudio track) {
        RecommendationsFragment fragment = new RecommendationsFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_TRACK, track);

        fragment.setArguments(args);

        return fragment;
    }

    public RecommendationsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        VKApiAudio track = null;

        if ( getArguments() != null ) {
            track = getArguments().getParcelable(ARG_TRACK);
        }

        this.song_id = new StringBuilder().append(track.owner_id).append("_").append(track.id).toString();

        mActionBar.setTitle(R.string.recommendations);
        mActionBar.setSubtitle(track.title);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mEmptyView.setText(R.string.no_recommendations);

        mAdapter.setOnActionButtonClicked(new EndlessScrollAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                saveTrack(position);
            }
        });

        return rootView;
    }

    /**
     * Adds a track to the user's playlist
     * @param position
     */
    private void saveTrack(int position) {
        VKApiAudio item = mAdapter.getItem(position);

        mCurrentRequest = new com.vk.sdk.api.methods.VKApiAudio().add(VKParameters.from(
            "audio_id", item.id,
            "owner_id", item.owner_id
        ));

        mCurrentRequest.executeWithListener(new AudioAddedListener(this, item, position));
    }

    @Override
    protected void loadTracks() {

        if ( song_id == null ) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }

        mCurrentRequest = new com.vk.sdk.api.methods.VKApiAudio().getRecommendations(VKParameters.from(
            "target_audio", song_id,
            "offset", 0,
            "count", ITEMS_COUNT
        ));

        mErrorView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);

        mCurrentRequest.executeWithListener(new RecommendationsLoadedListener(this));
    }

    /**
     * Updates playlist with new tracks
     * @param items
     */
    private void updatePlaylist(VkAudioArray items) {
        mCurrentRequest = null;

        mAdapter.setItems(items);
        mAdapter.setTotalCount(items.getCount());

        if ( items.isEmpty() ) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void loadMoreTracks() {
        mCurrentRequest = new com.vk.sdk.api.methods.VKApiAudio().getRecommendations(VKParameters.from(
                "target_audio", song_id,
                "offset", mAdapter.getItemCount(),
                "count", ITEMS_COUNT
        ));

        mAdapter.setIsLoading(true);

        mCurrentRequest.executeWithListener(new PlaylistFragment.MoreTracksLoadedListener(this));
    }

    /**
     * Get recommendations request listener with weak reference to the fragment
     */
    private static class RecommendationsLoadedListener extends VKRequest.VKRequestListener {

        private WeakReference<RecommendationsFragment> fragmentWeakReference;

        public RecommendationsLoadedListener(RecommendationsFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onComplete(VKResponse response) {

            RecommendationsFragment fragment = fragmentWeakReference.get();

            if ( fragment == null ) {
                return;
            }

            fragment.mLoadingView.setVisibility(View.GONE);

            if (response.parsedModel instanceof VkAudioArray) {
                fragment.updatePlaylist((VkAudioArray) response.parsedModel);
            } else {
                fragment.mErrorView.setVisibility(View.VISIBLE);
                fragment.showError(null);
            }
        }

        @Override
        public void onError(VKError error) {
            RecommendationsFragment fragment = fragmentWeakReference.get();

            if ( fragment == null ) {
                return;
            }

            fragment.mLoadingView.setVisibility(View.GONE);
            fragment.mErrorView.setVisibility(View.VISIBLE);
            fragment.showError(error);
        }
    }

    /**
     * Add audio request listener with weak reference to the fragment
     */
    private static class AudioAddedListener extends VKRequest.VKRequestListener {

        private WeakReference<RecommendationsFragment> fragmentWeakReference;
        private VKApiAudio track;
        private int position;

        public AudioAddedListener(RecommendationsFragment fragment, VKApiAudio track, int position) {
            fragmentWeakReference = new WeakReference<>(fragment);
            this.track = track;
            this.position = position;
        }

        @Override
        public void onComplete(VKResponse response) {

            RecommendationsFragment fragment = fragmentWeakReference.get();

            if ( fragment == null ) {
                return;
            }

            this.track.owner_id = Integer.valueOf(VKAccessToken.currentToken().userId);
            fragment.mAdapter.notifyItemChanged(this.position);

            if (fragment.mCurrentToast != null) {
                fragment.mCurrentToast.cancel();
            }
            fragment.mCurrentToast = Toast.makeText(fragment.getActivity(), R.string.track_saved, Toast.LENGTH_SHORT);
            fragment.mCurrentToast.show();
        }

        @Override
        public void onError(VKError error) {
            RecommendationsFragment fragment = fragmentWeakReference.get();

            if ( fragment != null ) {
                fragment.showError(error);
            }
        }
    }
}
