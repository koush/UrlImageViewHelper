package com.koushikdutta.test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class UrlImageViewHelperSample extends Activity {
    // turn a stream into a string
    private static String readToEnd(InputStream input) throws IOException
    {
        DataInputStream dis = new DataInputStream(input);
        byte[] stuff = new byte[1024];
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        int read = 0;
        while ((read = dis.read(stuff)) != -1)
        {
            buff.write(stuff, 0, read);
        }
        
        return new String(buff.toByteArray());
    }
    
    private ListView mListView;
    private MyAdapter mAdapter;
    
    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView iv;
            if (convertView == null)
                convertView = iv = new ImageView(UrlImageViewHelperSample.this);
            else
                iv = (ImageView)convertView;
            
            // yep, that's it. it handles the downloading and showing an interstitial image automagically.
            UrlImageViewHelper.setUrlDrawable(iv, getItem(position), R.drawable.loading);
            
            return iv;
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        final Button search = (Button)findViewById(R.id.search);
        final EditText searchText = (EditText)findViewById(R.id.search_text);
        
        mListView = (ListView)findViewById(R.id.results);
        mAdapter = new MyAdapter(this);
        mListView.setAdapter(mAdapter);
        
        search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // background the search call!
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            // clear existing results
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.clear();
                                }
                            });
                            
                            // do a google image search, get the ~10 paginated results
                            int start = 0;
                            while (start < 10) {
                                DefaultHttpClient client = new DefaultHttpClient();
                                HttpGet get = new HttpGet(String.format("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s&start=%d", Uri.encode(searchText.getText().toString()), start));
                                HttpResponse resp = client.execute(get);
                                HttpEntity entity = resp.getEntity();
                                InputStream is = entity.getContent();
                                final JSONObject json = new JSONObject(readToEnd(is));
                                is.close();
                                final JSONArray results = json.getJSONObject("responseData").getJSONArray("results");
                                final ArrayList<String> urls = new ArrayList<String>();
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject result = results.getJSONObject(i);
                                    urls.add(result.getString("url"));
                                }
                                
                                // add the results to the adapter
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (String url: urls) {
                                            mAdapter.add(url);
                                        }
                                    }
                                });
                                
                                start += urls.size();
                            }
                        }
                        catch (final Exception ex) {
                            // explodey error, lets toast it
                            runOnUiThread(new Runnable() {
                               @Override
                                public void run() {
                                   Toast.makeText(UrlImageViewHelperSample.this, ex.toString(), Toast.LENGTH_LONG).show();
                                } 
                            });
                        }
                    }
                };
                thread.start();
            }
        });
        
    }
}