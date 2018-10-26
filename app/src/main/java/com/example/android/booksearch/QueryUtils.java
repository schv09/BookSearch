package com.example.android.booksearch;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving book data from the Google Books API.
 */
public final class QueryUtils {

    /** Tag for the log messages */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /** HTTP request response code for the current request. */
    private static int mResponseCode;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Google Books data set and return a list of {@link Book} objects.
     */
    public static List<Book> fetchBookData(String url) {

        Log.i(LOG_TAG, "fetchBookData(): starting to fetch data");

        // Create URL object
        URL queryURL = createURL(url);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponseString = "";

        try {
            jsonResponseString = makeHttpRequest(queryURL);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem closing input stream. ", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Book}s
        List<Book> books = extractBooks(jsonResponseString);

        // Return the list of {@link Book}s
        return books;
    }

    /**
     * This method creates  URL object from a given string
     *
     * @param url the string representation of a URL
     * @return URL object
     */
    private static URL createURL(String url) {

        URL queryURL = null;
        try {
            queryURL = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error creating URL. ", e);
        }
        return queryURL;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponseString = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponseString;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            int responseCode = urlConnection.getResponseCode();

            mResponseCode = responseCode;

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (responseCode == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponseString = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results. ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponseString;
    }

    /**
     * Returns the HTTP request response code for a this request.
     * @return the HTTP response code for this request
     */
    public static int getHttpRequestResponseCode() {
         return mResponseCode;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line = bufferedReader.readLine();

            while (line != null) {
                output.append(line);
                line = bufferedReader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link Book} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Book> extractBooks(String jsonResponseString) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponseString)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding books to
        List<Book> books = new ArrayList<>();

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject rootJsonObject = new JSONObject(jsonResponseString);

            // Extract the JSONArray associated with the key called "items",
            // which represents a list of items (or books).
            JSONArray listOfBooks = rootJsonObject.getJSONArray("items");

            // For each book in the listOfBooks JSON array, create an {@link Book} object
            for (int i = 0; i < listOfBooks.length(); i++) {

                // Get a single book at position i within the list of books
                JSONObject bookJsonObj = listOfBooks.getJSONObject(i);

                // For a given book, extract the JSONObject associated with the
                // key called "volumeInfo", which represents a list of all properties
                // for that book.
                JSONObject volumeInfoJsonObj = bookJsonObj.getJSONObject("volumeInfo");

                // Extract the value for the key called "title"
                String title = volumeInfoJsonObj.getString("title");

                // Extract the JSON array for the key called "authors", if there is a value for
                // that key. Return null otherwise.
                JSONArray authorsJsonArray = volumeInfoJsonObj.optJSONArray("authors");

                // Create an empty array list to later store author names
                ArrayList<String> authors = new ArrayList<>();

                // Check if an author's JSON array was found. If so, extract each string value from
                // the array and add it to the recently created array list.
                if (authorsJsonArray != null) {
                    for (int j = 0; j < authorsJsonArray.length(); j++) {
                        authors.add(authorsJsonArray.getString(j));
                    }
                }

                // Extract the double value for the key called "averageRating", if there is a value
                // for that key or it can be coerced to a double. Return -1 otherwise.
                double rating = volumeInfoJsonObj.optDouble("averageRating", -1);

                // Extract the int value for the key called "ratingsCount", if there is a value
                // for that key or it can be coerced to an int. Return -1 otherwise.
                int ratingsCount = volumeInfoJsonObj.optInt("ratingsCount", -1);

                // Extract the string value for the key called "infoLink", which is a link to the
                // complete information for this book
                String infoUrl = volumeInfoJsonObj.getString("infoLink");

                // Extract the JSON object for the key called "imageLinks". Extract
                // the string value for the key called "smallThumbnail", which is a link to a
                // small thumbnail for this book
                String smallThumbnailUrl = volumeInfoJsonObj.getJSONObject("imageLinks")
                        .getString("smallThumbnail");

                // Create a new {@link Book} object with the title, authors, average rating,
                // ratings count, information link and thumbnail link from the JSON response.
                Book book = new Book(title, authors, rating, ratingsCount, infoUrl,
                        smallThumbnailUrl);

                // Add the new {@link Book} to the list of books.
                books.add(book);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
        }
        // Return the list of books
        return books;
    }
}