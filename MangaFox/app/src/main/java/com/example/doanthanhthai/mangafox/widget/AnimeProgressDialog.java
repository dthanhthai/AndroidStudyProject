package com.example.doanthanhthai.mangafox.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.example.doanthanhthai.mangafox.R;

public class AnimeProgressDialog extends Dialog {

    private ImageView iv;

    public AnimeProgressDialog(@NonNull Context context) {
        super(context);
        WindowManager.LayoutParams wlmp = getWindow().getAttributes();
        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        iv = new ImageView(context);
        Glide.with(context).load(R.raw.levi_loading).into(iv);

        layout.addView(iv, params);
        addContentView(layout, params);
    }

    @Override
    public void show() {
        super.show();
    }
}
