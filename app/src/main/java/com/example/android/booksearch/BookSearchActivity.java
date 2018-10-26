package com.example.android.booksearch;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BookSearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    /** Tag for the log messages */
    private static final String LOG_TAG = BookSearchActivity.class.getName();

    /** Base url for Google Books API */
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    /** The complete query url for the current query */
    private String queryUrl;

    /** BookAdapter for list view of books */
    private BookAdapter mAdapter;

    /** Text view for when list view of books is empty */
    private TextView mEmptyTextView;

    /** Circular progress indicator to show when fetching data */
    private ProgressBar mProgressIndicator;

    /** Integer Id for Loader */
    private static final int BOOK_LOADER_ID = 0;

    /** Boolean flag to tell whether this is the first time this Activity is being created.
     * If this is true, the text displayed won't change to "No books found", since the user
     * won't have actually done any queries at that moment.
     */
    private boolean isFirstLoad = true;

    /** Key to add boolean flag to Bundle when saving state */
    private static final String IS_FIRST_LOAD = "isFirstLoad";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(LOG_TAG, "onCreate(): Activity was created");

        setContentView(R.layout.activity_main);

        Log.i(LOG_TAG, "isFirstLoad before Bundle: " + isFirstLoad);

        if (savedInstanceState != null) {
            // Restore value of flag from saved state.
            // Use string key to retrieve previously saved value and save it in desired variable.
            isFirstLoad = savedInstanceState.getBoolean(IS_FIRST_LOAD);
        }

        Log.i(LOG_TAG, "isFirstLoad after Bundle: " + isFirstLoad);

        // Find progress bar
        mProgressIndicator = (ProgressBar) findViewById(R.id.progress_indicator);

        // Set visibility to gone while the user launches the app and types a query
        mProgressIndicator.setVisibility(View.GONE);

        // Find a reference to the {@link ListView} in the layout
        ListView booksListView = (ListView) findViewById(R.id.list);

        // Create a new {@link ArrayAdapter} of books
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        booksListView.setAdapter(mAdapter);

        // Find a reference to the text view to use when the list view is empty
        mEmptyTextView = (TextView) findViewById(R.id.empty_text_view);

        booksListView.setEmptyView(mEmptyTextView);

        mEmptyTextView.setText(R.string.how_to);

        // Set a click listener for an item clicked and make it open the link tht shows complete
        // information on this book
        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the current book that was clicked on
                Book currentBook = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getUrl());

                // Create a new intent to view the book URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bookUri);

                //Check if activity can be started, if so, start it
                if (websiteIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(websiteIntent);
                }
            }
        });

        // Find a reference to search view
        SearchView querySearchView = (SearchView) findViewById(R.id.query_search_view);

        // Add submit button to search view
        querySearchView.setSubmitButtonEnabled(true);

        // Preparing Loader. Without the initialization of the Loader inside onCreate(), the items on
        // the list view disappear when rotating the device.
        Log.i(LOG_TAG, "initLoader(): initializing Loader 0.");
        getLoaderManager().initLoader(BOOK_LOADER_ID, null, this);

        // Set listener on changes and submission of the query terms in the search view
        // Only implement a listener for the query submission, since it's the only thing we are
        // interested in this case.
        querySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                // Get a reference to the ConnectivityManager to check state of network connectivity
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                // Get details on the currently active default data network
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                // Get connection status
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                // If there is a network connection, fetch the data
                if (isConnected) {

                    // Clear the adapter of previous data so that it only shows the data we are
                    // about to fetch.
                    mAdapter.clear();

                    // Make progress bar visible so that the user knows we are doing some work in
                    // the background
                    mProgressIndicator.setVisibility(View.VISIBLE);

                    // Hide empty text view by setting text to an empty string
                    mEmptyTextView.setText("");

                    // Remove all empty spaces form query string
                    queryUrl = GOOGLE_BOOKS_BASE_URL + s.replace(" ", "");

                    Log.i(LOG_TAG, "Query text: " + s);
                    Log.i(LOG_TAG, "Query url: " + queryUrl);

                    // Get a proper loader manager and initialize the loader. Pass in the int ID constant
                    // defined above and pass in null for the bundle. Pass in this activity for the
                    // LoaderCallbacks parameter (which is valid because this activity implements the
                    // LoaderCallbacks interface).
                    Log.i(LOG_TAG, "restartLoader(): restarting Loader 0.");
                    getLoaderManager().restartLoader(BOOK_LOADER_ID, null, BookSearchActivity.this);
                } else {
                    // Otherwise, display error
                    // First, hide loading indicator so error message will be clearly visible
                    // and we show we are done with the background work
                    mProgressIndicator.setVisibility(View.GONE);

                    // Update empty state with no connection error message
                    mEmptyTextView.setText(R.string.no_internet);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        Log.i(LOG_TAG, "onCreateLoader(): creating new Loader.");
        return new BookLoader(this, queryUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        // Hide progress indicator when loading in background has finished
        mProgressIndicator.setVisibility(View.GONE);

        // Check if this is the first time the Activity has been created, which signals that
        // the Loader is only being initialized and the user still hasn't typed any queries
        if (isFirstLoad) {
            // If true, don't continue with this method because we don't want to change the text
            // on the empty text view to anything else, since the user hasn't done any searches yet.

            // Change flag to false to signal that all subsequent times that the Activity
            // is being created are due to a configuration change and that the user has already
            // made at least one search. This means that it will be ok to start changing the empty
            // text view's text to "no books found" in case the books list is empty or null.
            isFirstLoad = false;
            Log.i(LOG_TAG, "Inside onLoadFinished(): isFirstLoad changed to false");
            return;
        }

        // Check if there was an error response code. If so, change text on empty text view to show this
        // and exit early.
        if (QueryUtils.getHttpRequestResponseCode() != 200) {
            mEmptyTextView.setText(R.string.bad_response_code);
            Log.i(LOG_TAG, "onLoadFinished(): Bad response code, exiting early.");
            return;
        }

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
            Log.i(LOG_TAG, "onLoadFinished(): Data was received and assigned to Adapter.");
        } else {
            // Otherwise, change the text on the empty text view to "no books found".
            mEmptyTextView.setText(R.string.no_books);
            Log.i(LOG_TAG, "onLoadFinished(): No Data was received (Empty or null results).");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        // Remove references to Loader data because it won't be available anymore.
        mAdapter.addAll(new ArrayList<Book>());

        Log.i(LOG_TAG, "onLoaderReset(): Activity is being popped from back stack. Data won't be available anymore. Removing references from Loader data.");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the state of the boolean flag isFirstLoad to retrieve it in all subsequent times
        // that the Activity is being created. This will make the flag be false every time the
        // device is rotated and the activity recreated or when the activity is restarted after
        // being stopped. This will enable, for all future searches, that the empty text view's text
        // shows that no books where found when there are no results for a given query.
        Log.i(LOG_TAG, "onSaveInstanceState() called.");
        Log.i(LOG_TAG, "isFirstLoad: " + isFirstLoad);

        // Use string key as first argument and the value we want to save as second argument
        outState.putBoolean(IS_FIRST_LOAD, isFirstLoad);
        super.onSaveInstanceState(outState);
    }

    // BORRAR ESTOS METODOS DE ABAJO PARA SUBMISSION Y AGREGARLOS DESPUES PARA MI USO PERSONAL
    // DEJAR LOS LOGS PORQUE NO ES REQUISITO DE LA RUBRIC BORRARLOS!
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(LOG_TAG, "onRestoreInstanceState() called. isFirstLoad: " + isFirstLoad);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart() called. isFirstLoad: " + isFirstLoad);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart() called. isFirstLoad: " + isFirstLoad);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume() called. isFirstLoad: " + isFirstLoad);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy() called.");
    }
}
