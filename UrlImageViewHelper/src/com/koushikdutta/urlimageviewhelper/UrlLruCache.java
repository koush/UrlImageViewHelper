package com.koushikdutta.urlimageviewhelper;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;

@SuppressLint("NewApi")
public class UrlLruCache extends LruCache<String, BitmapDrawable> {
    public UrlLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, BitmapDrawable value) {
        if (value instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable)value;
            Bitmap b = bd.getBitmap();
            if (b != null)
                return b.getByteCount();
        }
        return 0;
    }
}
