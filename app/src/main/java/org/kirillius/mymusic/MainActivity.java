package org.kirillius.mymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import org.kirillius.mymusic.core.AppLoader;
import org.kirillius.mymusic.fragments.LoginFragment;
import org.kirillius.mymusic.fragments.PlaylistFragment;

public class MainActivity extends AppCompatActivity implements OnFragmentRequested {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ( "org.kirillius.mymusic.ACCESS_TOKEN_INVALID".equals(intent.getAction()) ) {
                login();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        setSupportActionBar(toolbar);

        login();
    }

    /**
     * Shows whether login fragment of playlist
     */
    private void login() {
        if (!VKSdk.isLoggedIn()) {
            replaceFragmentWith(new LoginFragment(), LoginFragment.TAG);
        }
        else {
            showPlaylist();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Пользователь успешно авторизовался
                showPlaylist();
            }

            @Override
            public void onError(VKError error) {
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                login();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Shows user's playlist
     */
    private void showPlaylist() {
        replaceFragmentWith(new PlaylistFragment(), PlaylistFragment.TAG);
    }

    /**
     * Replaces current fragment with a specified one
     * @param fragment Fragment instance
     * @param tag String tag
     */
    private void replaceFragmentWith(Fragment fragment, String tag) {
        showFragment(fragment, tag, false);
    }

    /**
     * Shows a fragment. Allows to add specified fragment to the back stack
     * @param fragment Fragment instance
     * @param tag String tag
     * @param addToBackStack Boolean flag whether to remember transaction's state or not
     */
    private void showFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragments_container, fragment, tag);

        if ( addToBackStack ) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        int stacked = getFragmentManager().getBackStackEntryCount();
        if ( stacked > 0 ) {
            getFragmentManager().popBackStack();

            if (stacked == 1) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch ( id ) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(AppLoader.appContext).registerReceiver(
                mReceiver,
                new IntentFilter("org.kirillius.mymusic.ACCESS_TOKEN_INVALID")
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(AppLoader.appContext).unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void navigate(Fragment fragment, String tag) {
        showFragment(fragment, tag, true);
    }
}
