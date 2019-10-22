package com.example.photopaint;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.example.photopaint.views.components.LayoutHelper;
import com.example.photopaint.views.components.PhotoPaintView;

public class MainActivity extends Activity {
    private FrameLayout frameLayout;
    private PhotoPaintView photoPaintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.GRAY);
        setContentView(frameLayout);

        photoPaintView = new PhotoPaintView(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.adv_img_2), 0);
        frameLayout.addView(photoPaintView, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        photoPaintView.init();
    }
}
