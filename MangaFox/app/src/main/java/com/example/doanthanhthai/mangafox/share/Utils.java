package com.example.doanthanhthai.mangafox.share;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by DOAN THANH THAI on 7/9/2018.
 */

public class Utils {
    public static boolean isTextEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static int[] getScreenSize(Context context) {
//        DisplayMetrics metrics = new DisplayMetrics();
//        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
//
//For mobile
//        int height = Math.max(metrics.widthPixels, metrics.heightPixels);
//        int width = Math.min(metrics.widthPixels, metrics.heightPixels);

        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        int[] screenSizes = {point.x, point.y};
        return screenSizes;
    }
}
