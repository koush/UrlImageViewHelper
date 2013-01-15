package com.koushikdutta.urlimageviewhelper;

import java.io.File;
import java.net.URI;

import android.content.Context;
import android.os.AsyncTask;

public class FileUrlDownloader implements UrlDownloader {
    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final Runnable completion) {
        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    callback.onDownloadComplete(FileUrlDownloader.this, null, new File(new URI(url)).getAbsolutePath());
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

        UrlImageViewHelper.executeTask(downloader);
    }

    @Override
    public boolean allowCache() {
        return false;
    }
    
    @Override
    public boolean canDownloadUrl(String url) {
        return url.startsWith("file:/");
    }
}
