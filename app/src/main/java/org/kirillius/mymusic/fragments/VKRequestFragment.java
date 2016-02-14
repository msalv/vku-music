package org.kirillius.mymusic.fragments;

import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;

import org.kirillius.mymusic.R;
import org.kirillius.mymusic.core.AppLoader;

/**
 * Created by Kirill on 02.02.2016.
 */
public abstract class VKRequestFragment extends Fragment {

    protected VKRequest mCurrentRequest;
    protected Toast mCurrentToast;

    /**
     * Shows error if something went wrong during API request
     * @param error VKError
     */
    protected void showError(VKError error) {
        mCurrentRequest = null;

        if (error != null) {
            Log.e(this.toString(), error.toString());

            if ( error.errorCode != VKError.VK_CANCELED ) {
                if ( mCurrentToast != null ) {
                    mCurrentToast.cancel();
                }
                mCurrentToast = Toast.makeText(AppLoader.appContext, R.string.request_error, Toast.LENGTH_SHORT);
                mCurrentToast.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mCurrentRequest != null) {
            mCurrentRequest.cancel();
        }
        mCurrentRequest = null;
    }

    @Override
    public void onDestroyView() {
        if (mCurrentRequest != null) {
            mCurrentRequest.setRequestListener(null);
        }

        if ( mCurrentToast != null ) {
            mCurrentToast.cancel();
        }
        mCurrentToast = null;

        super.onDestroyView();
    }
}
