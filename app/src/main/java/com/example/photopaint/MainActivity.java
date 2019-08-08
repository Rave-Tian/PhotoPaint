package com.example.photopaint;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import com.example.photopaint.ui.components.LayoutHelper;
import com.example.photopaint.ui.components.PhotoPaintView;

public class MainActivity extends Activity {
    private FrameLayout frameLayout;
    private PhotoPaintView photoPaintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.GRAY);
        setContentView(frameLayout);

        Button button = new Button(this);
        button.setText("Start Paint");
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(150, 150, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        frameLayout.addView(button, layoutParams);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(photoPaintView == null){
                    photoPaintView = new PhotoPaintView(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.adv_img_2), 0);
                    frameLayout.addView(photoPaintView, new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                }
                photoPaintView.init();
            }
        });
    }
}
