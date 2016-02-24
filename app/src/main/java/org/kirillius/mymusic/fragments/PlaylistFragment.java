package org.kirillius.mymusic.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiAudio;
import com.vk.sdk.api.model.Identifiable;
import com.vk.sdk.api.model.VKApiGetAudioResponse;
import com.vk.sdk.api.model.VkAudioArray;

import org.kirillius.mymusic.OnFragmentRequested;
import org.kirillius.mymusic.PlayerService;
import org.kirillius.mymusic.R;
import org.kirillius.mymusic.core.AppLoader;
import org.kirillius.mymusic.ui.ErrorView;
import org.kirillius.mymusic.ui.adapters.AdapterFactory;
import org.kirillius.mymusic.ui.adapters.EndlessScrollAdapter;
import org.kirillius.mymusic.ui.adapters.PlaylistAdapter;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends VKRequestFragment {

    public final static String TAG = "PlaylistFragment";
    public static final String BROADCAST_ACTION = "org.kirillius.mymusic.PLAYLIST_BROADCAST_ACTION";

    protected ActionBar mActionBar;

    private LinearLayoutManager mLayoutManager;
    protected PlaylistAdapter mAdapter;

    protected View mLoadingView;
    protected ErrorView mErrorView;
    protected TextView mEmptyView;

    protected final static int ITEMS_COUNT = 30;

    private PlaylistBroadcastReceiver mReceiver;

    private OnFragmentRequested onFragmentRequested;

    public PlaylistFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();

        mActionBar.setTitle(R.string.app_name);
        mActionBar.setSubtitle(null);
        mActionBar.setDisplayHomeAsUpEnabled(false);

        mLoadingView = rootView.findViewById(R.id.loading_view);
        mErrorView = (ErrorView)rootView.findViewById(R.id.error_view);
        mEmptyView = (TextView) rootView.findViewById(R.id.empty_view);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.audio_list);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        mAdapter = AdapterFactory.create(this);

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
                int prev = mAdapter.currentItemId;
                mAdapter.currentItemId = (prev == position) ? -1 : position;

                mAdapter.notifyItemChanged(position);
                if (prev != -1) {
                    mAdapter.notifyItemChanged(prev);
                }
            }
        });

        mAdapter.setOnActionButtonClicked(new EndlessScrollAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                com.vk.sdk.api.model.VKApiAudio track = mAdapter.getItem(position);

                if (track == null) {
                    return;
                }

                RecommendationsFragment f = RecommendationsFragment.createInstance(track);
                onFragmentRequested.navigate(f, RecommendationsFragment.TAG);
            }
        });

        mAdapter.setOnPlayButtonClicked(new EndlessScrollAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                Identifiable track = mAdapter.getItem(position);

                // show pause icon immediately
                mAdapter.currentPlayingId = track.getId();
                mAdapter.notifyDataSetChanged();

                Intent intent = new Intent(getActivity(), PlayerService.class);

                intent.putParcelableArrayListExtra(PlayerService.EXTRA_TRACKS, mAdapter.toArrayList());
                intent.putExtra(PlayerService.EXTRA_POSITION, position);
                intent.putExtra(PlayerService.EXTRA_TOTAL, mAdapter.getTotalCount());

                getActivity().startService(intent);
            }
        });

        mReceiver = new PlaylistBroadcastReceiver(this);
        AppLoader.appContext.registerReceiver(mReceiver, new IntentFilter(BROADCAST_ACTION));

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

        mCurrentRequest.executeWithListener(new TracksLoadedListener(this));
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

        if ( total > 0 ) {
            mActionBar.setSubtitle(getResources().getQuantityString(R.plurals.songs, total, total));
        }
        else {
            mActionBar.setSubtitle(R.string.no_songs);
        }

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

        mCurrentRequest.executeWithListener(new MoreTracksLoadedListener(this));
    }

    /**
     * Appends tracks to the playlist
     */
    protected void appendTracks(VkAudioArray items) {
        mCurrentRequest = null;

        mAdapter.addItems(items);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if ( activity instanceof OnFragmentRequested ) {
            onFragmentRequested = (OnFragmentRequested)activity;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentRequested");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentRequested = null;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            AppLoader.appContext.unregisterReceiver(mReceiver);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Request listener with weak reference to the fragment
     */
    private static class TracksLoadedListener extends VKRequest.VKRequestListener {

        private WeakReference<PlaylistFragment> fragmentWeakReference;

        public TracksLoadedListener(PlaylistFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onComplete(VKResponse response) {

            PlaylistFragment fragment = fragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.mLoadingView.setVisibility(View.GONE);

            if (response.parsedModel instanceof VKApiGetAudioResponse) {
                fragment.updatePlaylist((VKApiGetAudioResponse) response.parsedModel);
            }
            else {
                fragment.mErrorView.setVisibility(View.VISIBLE);
                fragment.showError(null);
            }
        }

        @Override
        public void onError(VKError error) {
            PlaylistFragment fragment = fragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.mLoadingView.setVisibility(View.GONE);
            fragment.mErrorView.setVisibility(View.VISIBLE);
            fragment.showError(error);
        }
    }

    /**
     * Request listener with weak reference to the fragment
     */
    protected static class MoreTracksLoadedListener extends VKRequest.VKRequestListener {

        private WeakReference<PlaylistFragment> fragmentWeakReference;

        public MoreTracksLoadedListener(PlaylistFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onComplete(VKResponse response) {

            PlaylistFragment fragment = fragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.mAdapter.setIsLoading(false);

            if (response.parsedModel instanceof VkAudioArray) {
                VkAudioArray data = (VkAudioArray) response.parsedModel;
                fragment.appendTracks(data);
            }
            else {
                fragment.mAdapter.onError();
                fragment.showError(null);
            }
        }

        @Override
        public void onError(VKError error) {
            PlaylistFragment fragment = fragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            fragment.mAdapter.setIsLoading(false);
            fragment.mAdapter.onError();
            fragment.showError(error);
        }
    }

    /**
     * Local broadcast receiver with weak reference to the fragment
     */
    private static class PlaylistBroadcastReceiver extends BroadcastReceiver {

        private WeakReference<PlaylistFragment> fragmentWeakReference;

        public PlaylistBroadcastReceiver(PlaylistFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int track_id = intent.getIntExtra(PlayerService.TRACK_ID, -1);

            PlaylistFragment fragment = fragmentWeakReference.get();

            if ( fragment != null && fragment.mAdapter != null ) {
                fragment.mAdapter.currentPlayingId = track_id;
                fragment.mAdapter.notifyDataSetChanged();
            }
        }
    }
}
