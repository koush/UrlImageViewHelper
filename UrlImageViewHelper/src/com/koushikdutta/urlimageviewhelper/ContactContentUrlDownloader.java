package com.koushikdutta.urlimageviewhelper;

import java.io.InputStream;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

public class ContactContentUrlDownloader implements UrlDownloader {
    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final Runnable completion) {
        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    final ContentResolver cr = context.getContentResolver();
                    InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(cr, Uri.parse(url));
                    callback.onDownloadComplete(ContactContentUrlDownloader.this, is, null);
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
        return url.startsWith(ContactsContract.Contacts.CONTENT_URI.toString());
    }
}
