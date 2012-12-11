package com.koushikdutta.urlimageviewhelper;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class WrapperDrawable extends Drawable {
    public WrapperDrawable(BitmapDrawable drawable) {
        mDrawable = drawable;
    }
    
    BitmapDrawable mDrawable;
    
    public WrapperDrawable(WrapperDrawable drawable) {
        this(drawable.mDrawable);
    }

    @Override
    public void draw(Canvas canvas) {
        mDrawable.draw(canvas);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getOpacity();
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawable.setColorFilter(cf);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        mDrawable.setBounds(left, top, right, bottom);
        super.setBounds(left, top, right, bottom);
    }
    
    @Override
    public void setBounds(Rect r) {
        mDrawable.setBounds(r);
        super.setBounds(r);
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }
    
    @Override
    public int getIntrinsicWidth() {
        return mDrawable.getIntrinsicWidth();
    }
    
    /**
     * Returns the underlying {@link BitmapDrawable}.
     * @return An instance of {@link BitmapDrawable}
     */
    @Deprecated
    public BitmapDrawable toBitmapDrawable() {
        return mDrawable;
    }
}
