package com.example.photopaint.ui.components;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.photopaint.R;

public class PhotoViewer extends FrameLayout {

    private PhotoPaintView photoPaintView;
    private Button button;

    public PhotoViewer(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        button = new Button(getContext());
        button.setText("Start Paint");
        addView(button, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(photoPaintView == null){
                    photoPaintView = new PhotoPaintView(getContext(), BitmapFactory.decodeResource(getResources(), R.drawable.catstile), 0);
                    addView(photoPaintView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                }
                photoPaintView.init();
            }
        });
    }

}
