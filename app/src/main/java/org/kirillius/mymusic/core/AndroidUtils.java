package org.kirillius.mymusic.core;

/**
 * Created by Kirill on 31.01.2016.
 */
public class AndroidUtils {

    public static float density = 1;

    static {
        density = AppLoader.appContext.getResources().getDisplayMetrics().density;
    }

    public static int dp(float value) {
        return (int)Math.ceil(density * value);
    }
}