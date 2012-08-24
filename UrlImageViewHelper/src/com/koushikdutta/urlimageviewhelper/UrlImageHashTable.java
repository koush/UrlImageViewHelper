package com.koushikdutta.urlimageviewhelper;

import android.graphics.drawable.Drawable;

public interface UrlImageHashTable {
    public Drawable put(String key, Drawable value);
    public Drawable get(String key);
}
