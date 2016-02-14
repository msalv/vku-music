package org.kirillius.mymusic.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiAudio;
import com.vk.sdk.api.model.VKApiGetAudioResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VkAudioArray;

import org.kirillius.mymusic.R;
import org.kirillius.mymusic.core.AndroidUtils;
import org.kirillius.mymusic.ui.ErrorView;
import org.kirillius.mymusic.ui.adapters.EndlessScrollAdapter;
import org.kirillius.mymusic.ui.adapters.PlaylistAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends VKRequestFragment {

    public final static String TAG = "PlaylistFragment";

    protected ActionBar mActionBar;

    private LinearLayoutManager mLayoutManager;
    private PlaylistAdapter mAdapter;

    private View mLoadingView;
    private ErrorView mErrorView;
    protected TextView mEmptyView;

    private final static int ITEMS_COUNT = 10;

    public PlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        mActionBar.setTitle(R.string.app_name);
        mActionBar.setSubtitle(null);

        mLoadingView = rootView.findViewById(R.id.loading_view);
        mErrorView = (ErrorView)rootView.findViewById(R.id.error_view);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.audio_list);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PlaylistAdapter();

        recyclerView.setAdapter(mAdapter);

        mErrorView.setOnRetryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTracks();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mAdapter.isLoading() || mAdapter.hasError()) {
                    return;
                }

                if (dy > 0) {
                    if (mAdapter.getItemCount() >= mAdapter.getTotalCount()) {
                        return;
                    }

                    int onScreen = mLayoutManager.getChildCount();
                    int scrolled = mLayoutManager.findFirstVisibleItemPosition();
                    int border = (int) Math.floor((mAdapter.getItemCount() - onScreen) * 0.75);

                    // fetch more when scroll over 75% of already shown items
                    if (scrolled >= border) {

                        if (mCurrentRequest == null) {
                            loadMoreTracks();
                        }
                    }
                }
            }
        });

        mAdapter.setOnErrorClickListener(new EndlessScrollAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                loadMoreTracks();
            }
        });

        mAdapter.setOnItemClickListener(new EndlessScrollAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                View buttons = itemView.findViewById(R.id.buttons);

                int visibility = buttons.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
                buttons.setVisibility(visibility);
            }
        });

        loadTracks();

        return rootView;
    }

    /**
     * Loads list of audio tracks
     */
    protected void loadTracks() {

        mCurrentRequest = new VKRequest("execute.getAudio", VKParameters.from(
                "count", ITEMS_COUNT
        ), VKApiGetAudioResponse.class);

        mErrorView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);

        mCurrentRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                mLoadingView.setVisibility(View.GONE);

                if (response.parsedModel instanceof VKApiGetAudioResponse) {
                    updatePlaylist((VKApiGetAudioResponse) response.parsedModel);
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
     */
    protected void updatePlaylist(VKApiGetAudioResponse response) {

        mCurrentRequest = null;

        if (!TextUtils.isEmpty(response.username)) {
            mActionBar.setTitle(response.username);
        }

        int total = response.items.getCount();

        mActionBar.setSubtitle( getResources().getQuantityString(R.plurals.songs, total, total) );

        mAdapter.setItems(response.items);
        mAdapter.setTotalCount(total);

        if ( response.items.isEmpty() ) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Loads more of audio tracks
     */
    protected void loadMoreTracks() {
        mCurrentRequest = new VKApiAudio().get(VKParameters.from(
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

    /**
     * Appends tracks to the playlist
     */
    protected void appendTracks(VkAudioArray items) {
        mCurrentRequest = null;

        mAdapter.addItems(items);
    }

    @Override
    public void onDestroyView() {
        mActionBar = null;
        mAdapter = null;
        mLayoutManager = null;

        mLoadingView = null;
        mErrorView = null;
        mEmptyView = null;

        super.onDestroyView();
    }
}
