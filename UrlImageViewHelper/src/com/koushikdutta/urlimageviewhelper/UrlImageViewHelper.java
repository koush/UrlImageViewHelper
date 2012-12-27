package com.koushikdutta.urlimageviewhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.http.NameValuePair;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;
import android.view.WindowManager;

public final class UrlImageViewHelper {
    private static void clog(String format, Object... args) {
        String log;
        if (args.length == 0)
            log = format;
        else
            log = String.format(format, args);
        if (Constants.LOG_ENABLED)
            Log.i(Constants.LOGTAG, log);
    }

    public static int copyStream(final InputStream input, final OutputStream output) throws IOException {
        final byte[] stuff = new byte[1024];
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
    private static void prepareResources(final Context context) {
        if (mMetrics != null) {
            return;
        }
        mMetrics = new DisplayMetrics();
        //final Activity act = (Activity)context;
        //act.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
        .getDefaultDisplay().getMetrics(mMetrics);
        final AssetManager mgr = context.getAssets();
        mResources = new Resources(mgr, mMetrics, context.getResources().getConfiguration());
    }

    private static boolean mUseBitmapScaling = true;
    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself.
     * Doing this will conserve memory.
     * @param useBitmapScaling Toggle for smart resizing.
     */
    public static void setUseBitmapScaling(boolean useBitmapScaling) {
        mUseBitmapScaling = useBitmapScaling;
    }
    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself.
     * Doing this will conserve memory.
     */
    public static boolean getUseBitmapScaling() {
        return mUseBitmapScaling;
    }

    private static Drawable loadDrawableFromStream(final Context context, final String url, final String filename, final int targetWidth, final int targetHeight) {
        prepareResources(context);

//        Log.v(Constants.LOGTAG,targetWidth);
//        Log.v(Constants.LOGTAG,targetHeight);
        FileInputStream stream = null;
        clog("Decoding: " + url + " " + filename);
        try {
            BitmapFactory.Options o = null;
            if (mUseBitmapScaling) {
                o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                stream = new FileInputStream(filename);
                BitmapFactory.decodeStream(stream, null, o);
                stream.close();
                int scale = 0;
                while ((o.outWidth >> scale) > targetWidth || (o.outHeight >> scale) > targetHeight) {
                    scale++;
                }
                o = new Options();
                o.inSampleSize = 1 << scale;
            }
            stream = new FileInputStream(filename);
            final Bitmap bitmap = BitmapFactory.decodeStream(stream, null, o);
            clog(String.format("Loaded bitmap (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));
            final BitmapDrawable bd = new BitmapDrawable(mResources, bitmap);
            return new ZombieDrawable(url, bd);
        } catch (final IOException e) {
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w(Constants.LOGTAG, "Failed to close FileInputStream", e);
                }
            }
        }
    }

    public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
    public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
    public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
    public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
    public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
    public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
    public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView} once it finishes loading.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    public static void loadUrlDrawable(final Context context, final String url) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs);
    }

    public static void loadUrlDrawable(final Context context, final String url, final long cacheDurationMs) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs, final UrlImageViewCallback callback) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, callback);
    }

    private static boolean isNullOrEmpty(final CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s.equals("NULL"));
    }

    private static boolean mHasCleaned = false;

    public static String getFilenameForUrl(final String url) {
        return url.hashCode() + ".urlimage";
    }

    /**
     * Clear out cached images.
     * @param context
     * @param age The max age of a file. Files older than this age
     *              will be removed.
     */
    public static void cleanup(final Context context, long age) {
        if (mHasCleaned) {
            return;
        }
        mHasCleaned = true;
        try {
            // purge any *.urlimage files over a week old
            final String[] files = context.getFilesDir().list();
            if (files == null) {
                return;
            }
            for (final String file : files) {
                if (!file.endsWith(".urlimage")) {
                    continue;
                }

                final File f = new File(context.getFilesDir().getAbsolutePath() + '/' + file);
                if (System.currentTimeMillis() > f.lastModified() + CACHE_DURATION_ONE_WEEK) {
                    f.delete();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear out all cached images older than a week.
     * The same as calling cleanup(context, CACHE_DURATION_ONE_WEEK);
     * @param context
     */
    public static void cleanup(final Context context) {
        cleanup(context, CACHE_DURATION_ONE_WEEK);
    }
    
    private static boolean checkCacheDuration(File file, long cacheDurationMs) {
        return cacheDurationMs == CACHE_DURATION_INFINITE || System.currentTimeMillis() < file.lastModified() + cacheDurationMs;
    }
    
    public static Drawable getImmediateMutableDrawable(String url) {
        Drawable ret = null;
        if (mDeadCache != null)
            ret = mDeadCache.get(url);
        if (ret != null)
            return ret;
        if (mLiveCache != null)
            ret = mLiveCache.get(url);
        if (ret != null && ret instanceof ZombieDrawable) {
            ZombieDrawable zd = (ZombieDrawable)ret;
            return zd.getBitmapDrawable().mutate();
        }
        return null;
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs, final UrlImageViewCallback callback) {
        cleanup(context);
        // disassociate this ImageView from any pending downloads
        if (isNullOrEmpty(url)) {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                imageView.setImageDrawable(defaultDrawable);
            }
            return;
        }

        final int tw;
        final int th;
        if (mMetrics == null)
            prepareResources(context);
        tw = mMetrics.widthPixels;
        th = mMetrics.heightPixels;

        final String filename = context.getFileStreamPath(getFilenameForUrl(url)).getAbsolutePath();
        final File file = new File(filename);

        if (mDeadCache == null) {
            mDeadCache = new UrlLruCache(getHeapSize(context) / 8);
        }
        Drawable drawable;
        final BitmapDrawable bd = mDeadCache.remove(url);
        if (bd != null) {
            // this drawable was resurrected, it should not be in the live cache
            clog("zombie load: " + url);
            Assert.assertTrue(url, !mAllCache.contains(bd));
            drawable = new ZombieDrawable(url, bd);
        } else {
            drawable = mLiveCache.get(url);
        }

        if (drawable != null) {
            clog("Cache hit on: " + url);
            // if the file age is older than the cache duration, force a refresh.
            // note that the file must exist, otherwise it is using a default.
            // not checking for file existence would do a network call on every
            // 404 or failed load.
            if (file.exists() && !checkCacheDuration(file, cacheDurationMs)) {
                clog("Cache hit, but file is stale. Forcing reload: " + url);
                if (drawable instanceof ZombieDrawable)
                    ((ZombieDrawable)drawable).headshot();
                drawable = null;
            }
            else {
                clog("Using cached: " + url);
            }
        }

        if (drawable != null) {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                imageView.setImageDrawable(drawable);
            }
            if (callback != null) {
                callback.onLoaded(imageView, drawable, url, true);
            }
            return;
        }

        // oh noes, at this point we definitely do not have the file available in memory
        // let's prepare for an asynchronous load of the image.

        // null it while it is downloading
        // since listviews reuse their views, we need to
        // take note of which url this view is waiting for.
        // This may change rapidly as the list scrolls or is filtered, etc.
        clog("Waiting for " + url + " " + imageView);
        if (imageView != null) {
            imageView.setImageDrawable(defaultDrawable);
            mPendingViews.put(imageView, url);
        }

        final ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
        if (currentDownload != null) {
            // Also, multiple vies may be waiting for this url.
            // So, let's maintain a list of these views.
            // When the url is downloaded, it sets the imagedrawable for
            // every view in the list. It needs to also validate that
            // the imageview is still waiting for this url.
            if (imageView != null) {
                currentDownload.add(imageView);
            }
            return;
        }

        final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
        if (imageView != null) {
            downloads.add(imageView);
        }
        mPendingDownloads.put(url, downloads);

        final int targetWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
        final int targetHeight = th <= 0 ? Integer.MAX_VALUE : th;
        final Loader loader = new Loader() {
            @Override
            public void run() {
                try {
                    result = loadDrawableFromStream(context, url, filename, targetWidth, targetHeight);
                }
                catch (final Exception ex) {
                    if (Constants.LOG_ENABLED)
                        Log.e(Constants.LOGTAG, "Error loading " + url, ex);
                }
            }
        };

        final Runnable completion = new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
                Drawable usableResult = loader.result;
                if (usableResult == null) {
                    clog("No usable result, defaulting " + url);
                    usableResult = defaultDrawable;
                }
                mPendingDownloads.remove(url);
                mLiveCache.put(url, usableResult);
                if (callback != null && imageView == null)
                    callback.onLoaded(null, loader.result, url, false);
                int waitingCount = 0;
                for (final ImageView iv: downloads) {
                    // validate the url it is waiting for
                    final String pendingUrl = mPendingViews.get(iv);
                    if (!url.equals(pendingUrl)) {
                        clog("Ignoring out of date request to update view for " + url + " " + pendingUrl + " " + iv);
                        continue;
                    }
                    waitingCount++;
                    mPendingViews.remove(iv);
                    if (usableResult != null) {
//                        System.out.println(String.format("imageView: %dx%d, %dx%d", imageView.getMeasuredWidth(), imageView.getMeasuredHeight(), imageView.getWidth(), imageView.getHeight()));
                        iv.setImageDrawable(usableResult);
//                        System.out.println(String.format("imageView: %dx%d, %dx%d", imageView.getMeasuredWidth(), imageView.getMeasuredHeight(), imageView.getWidth(), imageView.getHeight()));
                        if (callback != null && iv == imageView)
                            callback.onLoaded(iv, loader.result, url, false);
                    }
                }
                clog("Populated: " + waitingCount);
            }
        };


        if (file.exists()) {
            try {
                if (checkCacheDuration(file, cacheDurationMs)) {
                    clog("File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");

                    final AsyncTask<Void, Void, Void> fileloader = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            loader.run();
                            return null;
                        }
                        @Override
                        protected void onPostExecute(final Void result) {
                            completion.run();
                        }
                    };
                    executeTask(fileloader);
                    return;
                }
                else {
                    clog("File cache has expired. Refreshing.");
                }
            }
            catch (final Exception ex) {
            }
        }

        mDownloader.download(context, url, filename, loader, completion);
    }

    private static abstract class Loader implements Runnable {
        Drawable result;
    }

    public static interface UrlDownloader {
        public void download(Context context, String url, String filename, Runnable loader, Runnable completion);
    }

    private static UrlDownloader mDefaultDownloader = new UrlDownloader() {
        @Override
        public void download(final Context context, final String url, final String filename, final Runnable loader, final Runnable completion) {
            final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    try {
                        InputStream is = null;
                        if (url.startsWith(ContentResolver.SCHEME_CONTENT)) {
                            final ContentResolver cr = context.getContentResolver();
                            if (url.startsWith(ContactsContract.Contacts.CONTENT_URI.toString())) {
                                is = ContactsContract.Contacts.openContactPhotoInputStream(cr, Uri.parse(url));
                            } else {
                                is = cr.openInputStream(Uri.parse(url));
                            }
                        }
                        else {
                            String thisUrl = url;
                            HttpURLConnection urlConnection;
                            while (true) {
                                final URL u = new URL(thisUrl);
                                urlConnection = (HttpURLConnection)u.openConnection();
                                urlConnection.setInstanceFollowRedirects(true);

                                if (mRequestPropertiesCallback != null) {
                                    final ArrayList<NameValuePair> props = mRequestPropertiesCallback.getHeadersForRequest(context, url);
                                    if (props != null) {
                                        for (final NameValuePair pair: props) {
                                            urlConnection.addRequestProperty(pair.getName(), pair.getValue());
                                        }
                                    }
                                }

                                if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM)
                                    break;
                                thisUrl = urlConnection.getHeaderField("Location");
                            }

                            if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                                clog("Response Code: " + urlConnection.getResponseCode());
                                return null;
                            }
                            is = urlConnection.getInputStream();
                        }

                        if (is != null) {
                            final FileOutputStream fos = new FileOutputStream(filename);
                            copyStream(is, fos);
                            fos.close();
                            is.close();
                        }
                        loader.run();
                        return null;
                    }
                    catch (final Throwable e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(final Void result) {
                    completion.run();
                }
            };

            executeTask(downloader);
        }
    };

    public static interface RequestPropertiesCallback {
        public ArrayList<NameValuePair> getHeadersForRequest(Context context, String url);
    }

    private static RequestPropertiesCallback mRequestPropertiesCallback;

    public static RequestPropertiesCallback getRequestPropertiesCallback() {
        return mRequestPropertiesCallback;
    }

    public static void setRequestPropertiesCallback(final RequestPropertiesCallback callback) {
        mRequestPropertiesCallback = callback;
    }


    public static void useDownloader(final UrlDownloader downloader) {
        mDownloader = downloader;
    }

    public static void useDefaultDownloader() {
        mDownloader = mDefaultDownloader;
    }

    public static UrlDownloader getDefaultDownloader() {
        return mDownloader;
    }

    private static UrlImageCache mLiveCache = UrlImageCache.getInstance();

    private static UrlLruCache mDeadCache;
    private static HashSet<BitmapDrawable> mAllCache = new HashSet<BitmapDrawable>();
    private static int getHeapSize(final Context context) {
        return ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;
    }

    private static class ZombieDrawable extends WrapperDrawable {
        public ZombieDrawable(final String url, final BitmapDrawable drawable) {
            super(drawable);
            mUrl = url;

            mAllCache.add(drawable);
            mDeadCache.remove(url);
            mLiveCache.put(url, this);
        }

        String mUrl;

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            if (!mHeadshot)
                mDeadCache.put(mUrl, mDrawable);
            mAllCache.remove(mDrawable);
            mLiveCache.remove(mUrl);
            clog("Zombie GC event " + mUrl);
        }
        
        // kill this zombie, forever.
        private boolean mHeadshot = false;
        public void headshot() {
            clog("BOOM! Headshot: " + mUrl);
            mHeadshot = true;
            mLiveCache.remove(mUrl);
            mAllCache.remove(mDrawable);
        }
    }

    private static UrlDownloader mDownloader = mDefaultDownloader;

    private static void executeTask(final AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT < Constants.HONEYCOMB) {
            task.execute();
        } else {
            executeTaskHoneycomb(task);
        }
    }

    @TargetApi(Constants.HONEYCOMB)
    private static void executeTaskHoneycomb(final AsyncTask<Void, Void, Void> task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
