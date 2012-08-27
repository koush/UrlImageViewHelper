package com.koushikdutta.urlimageviewhelper;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

class WrapperDrawable extends Drawable {
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
    }
    
    @Override
    public int getIntrinsicHeight() {
        return mDrawable.getIntrinsicHeight();
    }
    
    @Override
    public int getIntrinsicWidth() {
        // TODO Auto-generated method stub
        return mDrawable.getIntrinsicWidth();
    }
}
