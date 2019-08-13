package com.example.photopaint;

import android.graphics.Bitmap;

public class MessageWrap {

    public final Bitmap message;

    public static MessageWrap getInstance(Bitmap message) {
        return new MessageWrap(message);
    }

    private MessageWrap(Bitmap message) {
        this.message = message;
    }
}