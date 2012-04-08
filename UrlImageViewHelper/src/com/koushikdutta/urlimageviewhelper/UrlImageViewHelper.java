package com.koushikdutta.urlimageviewhelper;

import android.annotation.TargetApi;
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
import android.widget.ImageView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

@TargetApi(8)
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
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS);
    }

    public static void loadUrlDrawable(final Context context, final String url) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, int defaultResource, long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs);
    }

    public static void loadUrlDrawable(final Context context, final String url, long cacheDurationMs) {
        setUrlDrawable(context, null, url, null, cacheDurationMs);
    }

    public static void setUrlDrawable(final ImageView imageView, final String url, Drawable defaultDrawable, long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs);
    }

    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, int defaultResource, long cacheDurationMs) {
        Drawable d = null;
        if (defaultResource != 0)
            d = imageView.getResources().getDrawable(defaultResource);
        setUrlDrawable(context, imageView, url, d, cacheDurationMs);
    }

    private static boolean isNullOrEmpty(CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s.equals("NULL"));
    }

    private static boolean mHasCleaned = false;

    public static String getFilenameForUrl(String url) {
        return "" + url.hashCode() + ".urlimage";
    }
    
    private static class UrlDrawableFetcher extends AsyncTask<Void, Void, Drawable> {
        
        private Context mContext;
        private final ImageView mImageView;
        private final Drawable mDefaultDrawable;
        private final long mCacheDuration;
        private final String mUrl;

        public UrlDrawableFetcher(final Context context, final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs)
        {
            mContext = context;
            mImageView = imageView;
            mUrl = url;
            mDefaultDrawable = defaultDrawable;
            mCacheDuration = cacheDurationMs;
        }

        @Override
        protected Drawable doInBackground(Void... urls) {
            final String filename = getFilenameForUrl(mUrl);

            Drawable drawable = checkFilesystem(filename);
            if (drawable != null)
            {
                return drawable;
            }

            ArrayList<ImageView> currentDownload = mPendingDownloads.get(mUrl);
            if (currentDownload != null) {
                // Also, multiple vies may be waiting for this url.
                // So, let's maintain a list of these views.
                // When the url is downloaded, it sets the imagedrawable for
                // every view in the list. It needs to also validate that
                // the imageview is still waiting for this url.
                if (mImageView != null)
                    currentDownload.add(mImageView);
                return null;
            }
            final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
            if (mImageView != null) {
                downloads.add(mImageView);
            }
            mPendingDownloads.put(mUrl, downloads);
            
            return getFileFromInternet(filename);
        }

        private Drawable getFileFromInternet(final String filename) {
            AndroidHttpClient client = null;
            InputStream is = null;
            FileOutputStream fos = null;
            FileInputStream fis = null;
            try {
                client = AndroidHttpClient.newInstance(mContext.getPackageName());
                HttpGet get = new HttpGet(mUrl);
                final HttpParams httpParams = new BasicHttpParams();
                HttpClientParams.setRedirecting(httpParams, true);
                get.setParams(httpParams);
                HttpResponse resp = client.execute(get);
                int status = resp.getStatusLine().getStatusCode();
                if(status != HttpURLConnection.HTTP_OK){
//                    Log.i(LOGTAG, "Couldn't download image from Server: " + url + " Reason: " + resp.getStatusLine().getReasonPhrase() + " / " + status);
                    return null;
                }
                HttpEntity entity = resp.getEntity();
//                Log.i(LOGTAG, url + " Image Content Length: " + entity.getContentLength());
                is = entity.getContent();
                fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                copyStream(is, fos);
                fos.close();
                is.close();
                fis = mContext.openFileInput(filename);
                return loadDrawableFromStream(mContext, fis);
            }
            catch (Exception ex) {
//                Log.e(LOGTAG, "Exception during Image download of " + url, ex);
                return null;
            }
            finally {
                if(client != null) { client.close(); }
                safeClose(is);
                safeClose(fos);
                safeClose(fis);
            }
        }
        
        private static void safeClose(Closeable c) {
            if (c == null) { return; }
            try
            {
                c.close();
            }
            catch (IOException e)
            {
                //Log.e(LOGTAG, "Error closing file", e);
            }
        }

        private Drawable checkFilesystem(final String filename) {
            File file = mContext.getFileStreamPath(filename);

            if (file.exists()) {
                try {
                    if (mCacheDuration == CACHE_DURATION_INFINITE || System.currentTimeMillis() < file.lastModified() + mCacheDuration) {
                        //Log.i(LOGTAG, "File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");
                        FileInputStream  fis = mContext.openFileInput(filename);
                        BitmapDrawable drawable = loadDrawableFromStream(mContext, fis);
                        fis.close();
                        return drawable;
                    }
                    else {
                        //Log.i(LOGTAG, "File cache has expired. Refreshing.");
                    }
                }
                catch (Exception ex) {
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Drawable result) {
            if (result == null)
                result = mDefaultDrawable;
            final UrlImageCache cache = UrlImageCache.getInstance();
            mPendingDownloads.remove(mUrl);
            cache.put(mUrl, result);
            final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
            for (ImageView iv: downloads) {
                // validate the url it is waiting for
                String pendingUrl = mPendingViews.get(iv);
                if (!mUrl.equals(pendingUrl)) {
                    //Log.i(LOGTAG, "Ignoring out of date request to update view for " + url);
                    continue;
                }
                mPendingViews.remove(iv);
                if (result != null) {
                    final Drawable newImage = result;
                    final ImageView imageView = iv;
                    imageView.setImageDrawable(newImage);
                }
            }
            mImageView.setImageDrawable(result);
        }
        
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

    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final Drawable defaultDrawable, long cacheDurationMs) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                cleanup(context);
            }}).start();
        
        
        // disassociate this ImageView from any pending downloads
        if (imageView != null)
            mPendingViews.remove(imageView);

        if (isNullOrEmpty(url)) {
            if (imageView != null)
                imageView.setImageDrawable(defaultDrawable);
            return;
        }

        final UrlImageCache cache = UrlImageCache.getInstance();
        Drawable d = cache.get(url);
        if (d != null) {
            //Log.i(LOGTAG, "Cache hit on: " + url);
            if (imageView != null)
                imageView.setImageDrawable(d);
            return;
        }
        
        // null it while it is downloading
        if (imageView != null) {
            imageView.setImageDrawable(defaultDrawable);
            mPendingViews.put(imageView, url);
        }

        UrlDrawableFetcher fetcher = new UrlDrawableFetcher(context, imageView, url, defaultDrawable, cacheDurationMs);
        fetcher.execute();
        // XXX: PUT HERE THE ASYNCTASK


        // since listviews reuse their views, we need to 
        // take note of which url this view is waiting for.
        // This may change rapidly as the list scrolls or is filtered, etc.
        //Log.i(LOGTAG, "Waiting for " + url);
    }

    private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
