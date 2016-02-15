package org.kirillius.mymusic.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;

import org.kirillius.mymusic.R;

/**
 * Created by Kirill on 27.01.2016.
 */
public class ErrorView extends FrameLayout {

    private Button mRetryButton;

    public ErrorView(Context context) {
        super(context);
        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mRetryButton = new Button(context);
        mRetryButton.setText(R.string.try_again);

        this.addView(mRetryButton);

        LayoutParams lp = (LayoutParams)mRetryButton.getLayoutParams();
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
    }

    public void setOnRetryClickListener(OnClickListener listener) {
        mRetryButton.setOnClickListener(listener);
    }
}
