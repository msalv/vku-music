package org.kirillius.mymusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiAudio;
import com.vk.sdk.api.model.VkAudioArray;

import org.kirillius.mymusic.R;

/**
 * Created by Kirill on 14.02.2016.
 */
public class RecommendationsFragment extends PlaylistFragment {

    public final static String TAG = "RecommendationsFragment";

    public final static String ARG_ID = "ArgId";
    public final static String ARG_TITLE = "ArgTitle";

    private String song_id = null;

    public static RecommendationsFragment createInstance(String song_id, String title) {
        RecommendationsFragment fragment = new RecommendationsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_ID, song_id);
        args.putString(ARG_TITLE, title);

        fragment.setArguments(args);

        return fragment;
    }

    public RecommendationsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        String title = null;

        if ( getArguments() != null ) {
            title = getArguments().getString(ARG_TITLE);
        }

        mActionBar.setTitle(R.string.recommendations);
        mActionBar.setSubtitle(title);
        mActionBar.setDisplayHomeAsUpEnabled(true);

        mEmptyView.setText(R.string.no_recommendations);

        //todo: use another adapter to render different action button

        return rootView;
    }

    @Override
    protected void loadTracks() {

        if ( getArguments() != null ) {
            song_id = getArguments().getString(ARG_ID);
        }

        if ( song_id == null ) {
            mEmptyView.setVisibility(View.VISIBLE);
            return;
        }

        mCurrentRequest = new VKApiAudio().getRecommendations(VKParameters.from(
            "target_audio", song_id,
            "offset", 0,
            "count", ITEMS_COUNT
        ));

        mErrorView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);

        mCurrentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                mLoadingView.setVisibility(View.GONE);

                if (response.parsedModel instanceof VkAudioArray) {
                    updatePlaylist((VkAudioArray) response.parsedModel);
                } else {
                    mErrorView.setVisibility(View.VISIBLE);
                    showError(null);
                }
            }

            @Override
            public void onError(VKError error) {
                mLoadingView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                showError(error);
            }
        });
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
        mCurrentRequest = new VKApiAudio().getRecommendations(VKParameters.from(
                "target_audio", song_id,
                "offset", mAdapter.getItemCount(),
                "count", ITEMS_COUNT
        ));

        mAdapter.setIsLoading(true);

        mCurrentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                mAdapter.setIsLoading(false);

                if (response.parsedModel instanceof VkAudioArray) {
                    VkAudioArray data = (VkAudioArray) response.parsedModel;
                    appendTracks(data);
                } else {
                    mAdapter.onError();
                    showError(null);
                }
            }

            @Override
            public void onError(VKError error) {
                mAdapter.setIsLoading(false);
                mAdapter.onError();
                showError(error);
            }
        });
    }
}
