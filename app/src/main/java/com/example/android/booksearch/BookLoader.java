package com.example.android.booksearch;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents a custom AsyncTaskLoader for a Book object.
 */

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    /** Tag for the log messages */
    private static final String LOG_TAG = BookLoader.class.getSimpleName();

    /** Query url */
    private String mUrl;

    /**
     * Constructs a new {@link BookLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public BookLoader(Context context, String url) {
        super(context);
        mUrl = url;
        Log.i(LOG_TAG, "BookLoader constructor called: new Loader created.");
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.i(LOG_TAG, "onStartLoading() --> forceLoad()");
    }

    @Override
    public List<Book> loadInBackground() {
        Log.i(LOG_TAG, "loadInBackground(): loading data in background");

        // If the query url is empty, return an empty array list. Otherwise, fetch data
        // using this url
        if (TextUtils.isEmpty(mUrl)) {
            return new ArrayList<Book>();
        } else {
            return QueryUtils.fetchBookData(mUrl);
        }
    }
}
