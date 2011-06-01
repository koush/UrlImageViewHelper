/*
 * UrlImageCache.java
 * 
 * Copyright 2010, BuddyTV. All Rights Reserved
 * Created 12/13/2010 Mike Markley
 */
package com.koushikdutta.test;

import android.graphics.drawable.Drawable;

public final class UrlImageCache extends SoftReferenceHashTable<String, Drawable> {
    private static UrlImageCache mInstance = new UrlImageCache();
    
    public static UrlImageCache getInstance() {
        return mInstance;
    }
    
    private UrlImageCache() {
    }
}
