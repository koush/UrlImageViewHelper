package com.koushikdutta.urlimageviewhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

public final class UrlImageViewHelper {
    private static final String LOGTAG = "UrlImageViewHelper";
    public static int copyStream(InputStream input, OutputStream output) throws IOException
    {
        byte[] stuff = new byte[1024];
        int read = 0;
        int total = 0;
        while ((read = input.read(stuff)) != -1)
        {
            output.write(stuff, 0, read);
            total += read;
        }
        return total;
    }

    static Resources mResources;
    static DisplayMetrics mMetrics;
    private static void prepareResources(Context context) {
        if (mMetrics != null)
            return;
        mMetrics = new DisplayMetrics();
        Activity act = (Activity)context;
        act.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        AssetManager mgr = context.getAssets();
        mResources = new Resources(mgr, mMetrics, context.getResources().getConfiguration());
    }

    private static BitmapDrawable loadDrawableFromStream(Context context, InputStream stream) {
        prepareResources(context);
        final Bitmap bitmap = BitmapFactory.decodeStream(stream);
        //Log.i(LOGTAG, String.format("Loaded bitmap (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));
        return new BitmapDrawable(mResources, bitmap);
    }

    public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
    public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
    public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
    public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
    public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
    public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
    public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

    public static void setUrlDrawable(final ImageView imageView, final String url, int defaultResource) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    public static void loadUrlDrawable(final Context context, final String url) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, null);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, int defaultResource, long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs);
    }

    public static void loadUrlDrawable(final Context context, final String url, long cacheDurationMs) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, null);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable, long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, null);
    }

    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, int defaultResource, long cacheDurationMs) {
        Drawable d = null;
        if (defaultResource != 0)
            d = imageView.getResources().getDrawable(defaultResource);
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, null);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, int defaultResource, UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable, UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, int defaultResource, long cacheDurationMs, UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, long cacheDurationMs, UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, callback);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable, long cacheDurationMs, UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, callback);
    }

    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, int defaultResource, long cacheDurationMs, UrlImageViewCallback callback) {
        Drawable d = null;
        if (defaultResource != 0)
            d = imageView.getResources().getDrawable(defaultResource);
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, callback);
    }

    private static boolean isNullOrEmpty(CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s.equals("NULL"));
    }

    private static boolean mHasCleaned = false;

    public static String getFilenameForUrl(String url) {
        return "" + url.hashCode() + ".urlimage";
    }

    private static void cleanup(Context context) {
        if (mHasCleaned)
            return;
        mHasCleaned = true;
        try {
            // purge any *.urlimage files over a week old
            String[] files = context.getFilesDir().list();
            if (files == null)
                return;
            for (String file : files) {
                if (!file.endsWith(".urlimage"))
                    continue;

                File f = new File(context.getFilesDir().getAbsolutePath() + '/' + file);
                if (System.currentTimeMillis() > f.lastModified() + CACHE_DURATION_ONE_WEEK)
                    f.delete();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final Drawable defaultDrawable, long cacheDurationMs, final UrlImageViewCallback callback) {
        cleanup(context);
        // disassociate this ImageView from any pending downloads
        if (imageView != null)
            mPendingViews.remove(imageView);

        if (isNullOrEmpty(url)) {
            if (imageView != null)
                imageView.setImageDrawable(defaultDrawable);
            return;
        }

        final UrlImageCache cache = UrlImageCache.getInstance();
        Drawable drawable = cache.get(url);
        if (drawable != null) {
            //Log.i(LOGTAG, "Cache hit on: " + url);
            if (imageView != null)
                imageView.setImageDrawable(drawable);
            if (callback != null)
                callback.onLoaded(imageView, drawable, url, true);
            return;
        }

        final String filename = getFilenameForUrl(url);

        File file = context.getFileStreamPath(filename);
        if (file.exists()) {
            try {
                if (cacheDurationMs == CACHE_DURATION_INFINITE || System.currentTimeMillis() < file.lastModified() + cacheDurationMs) {
                    //Log.i(LOGTAG, "File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");
                    FileInputStream  fis = context.openFileInput(filename);
                    drawable = loadDrawableFromStream(context, fis);
                    fis.close();
                    if (imageView != null)
                        imageView.setImageDrawable(drawable);
                    cache.put(url, drawable);
                    if (callback != null)
                        callback.onLoaded(imageView, drawable, url, true);
                    return;
                }
                else {
                    //Log.i(LOGTAG, "File cache has expired. Refreshing.");
                }
            }
            catch (Exception ex) {
            }
        }

        // null it while it is downloading
        if (imageView != null)
            imageView.setImageDrawable(defaultDrawable);

        // since listviews reuse their views, we need to 
        // take note of which url this view is waiting for.
        // This may change rapidly as the list scrolls or is filtered, etc.
        //Log.i(LOGTAG, "Waiting for " + url);
        if (imageView != null)
            mPendingViews.put(imageView, url);

        ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
        if (currentDownload != null) {
            // Also, multiple vies may be waiting for this url.
            // So, let's maintain a list of these views.
            // When the url is downloaded, it sets the imagedrawable for
            // every view in the list. It needs to also validate that
            // the imageview is still waiting for this url.
            if (imageView != null)
                currentDownload.add(imageView);
            return;
        }

        final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
        if (imageView != null)
            downloads.add(imageView);
        mPendingDownloads.put(url, downloads);

        AsyncTask<Void, Void, BitmapDrawable> downloader = new AsyncTask<Void, Void, BitmapDrawable>() {
            @Override
            protected BitmapDrawable doInBackground(Void... params) {
                AndroidHttpClient client = AndroidHttpClient.newInstance(context.getPackageName());
                try {
                    HttpGet get = new HttpGet(url);
                    final HttpParams httpParams = new BasicHttpParams();
                    HttpClientParams.setRedirecting(httpParams, true);
                    get.setParams(httpParams);
                    HttpResponse resp = client.execute(get);
                    int status = resp.getStatusLine().getStatusCode();
                    if(status != HttpURLConnection.HTTP_OK){
//                        Log.i(LOGTAG, "Couldn't download image from Server: " + url + " Reason: " + resp.getStatusLine().getReasonPhrase() + " / " + status);
                        return null;
                    }
                    HttpEntity entity = resp.getEntity();
//                    Log.i(LOGTAG, url + " Image Content Length: " + entity.getContentLength());
                    InputStream is = entity.getContent();
                    FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    copyStream(is, fos);
                    fos.close();
                    is.close();
                    FileInputStream  fis = context.openFileInput(filename);
                    return loadDrawableFromStream(context, fis);
                }
                catch (Exception ex) {
//                    Log.e(LOGTAG, "Exception during Image download of " + url, ex);
                    return null;
                }
                finally {
                    client.close();
                }
            }

            protected void onPostExecute(BitmapDrawable result) {
                Drawable usableResult = result;
                if (usableResult == null)
                    usableResult = defaultDrawable;
                mPendingDownloads.remove(url);
                cache.put(url, usableResult);
                for (ImageView iv: downloads) {
                    // validate the url it is waiting for
                    String pendingUrl = mPendingViews.get(iv);
                    if (!url.equals(pendingUrl)) {
                        //Log.i(LOGTAG, "Ignoring out of date request to update view for " + url);
                        continue;
                    }
                    mPendingViews.remove(iv);
                    if (usableResult != null) {
                        final Drawable newImage = usableResult;
                        final ImageView imageView = iv;
                        imageView.setImageDrawable(newImage);
                        if (callback != null)
                            callback.onLoaded(imageView, result, url, false);
                    }
                }
            }
        };
        downloader.execute();
    }

    private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
