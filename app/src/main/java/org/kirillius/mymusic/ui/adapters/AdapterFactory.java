package org.kirillius.mymusic.ui.adapters;

import android.app.Fragment;

import org.kirillius.mymusic.fragments.PlaylistFragment;
import org.kirillius.mymusic.fragments.RecommendationsFragment;

/**
 * Created by Kirill on 15.02.2016.
 */
public class AdapterFactory {

    public static PlaylistAdapter create(Fragment parent) {

        if (parent instanceof RecommendationsFragment) {
            return new RecommendationsAdapter();
        }

        if (parent instanceof PlaylistFragment) {
            return new PlaylistAdapter();
        }

        return null;
    }

}
