package com.koushikdutta.urlimageviewhelper;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Callback that is invoked with a success/failure after attempting to
 * load a drawable from an url.
 * @author koush
 *
 */
public interface UrlImageViewCallback {
    /**
     * 
     * @param imageView ImageView for the load request.
     * @param loadedDrawable The drawable that was loaded by the request.
     *                          If the drawable failed to load, this will be null.
     * @param url The url that was loaded.
     * @param loadedFromCache This will indicate whether the load operation result came from cache, or was retrieved.
     */
    void onLoaded(ImageView imageView, Drawable loadedDrawable, String url, boolean loadedFromCache);
}
