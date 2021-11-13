package com.l_es.kiril_stickers;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class KirillsStickers extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
