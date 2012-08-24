package com.koushikdutta.urlimageviewhelper;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

@SuppressLint("NewApi")
public class UrlLruCache extends LruCache<String, Drawable> implements UrlImageHashTable {
    public UrlLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Drawable value) {
        if (value instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable)value;
            Bitmap b = bd.getBitmap();
            if (b != null)
                return b.getByteCount();
        }
        return 0;
    }
}
