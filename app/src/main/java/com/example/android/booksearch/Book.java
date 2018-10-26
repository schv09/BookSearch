package com.example.android.booksearch;

import java.util.ArrayList;

/**
 * Class that represents a Book object.
 */

public class Book {

    /** Title of this book */
    private String mTitle;

    /** List of authors of this book */
    private ArrayList<String> mAuthors;

    /** Average rating given by reviewers */
    private double mRating;

    /** Amount of people that have reviewed this book */
    private int mRatingsCount;

    /** URL that leads to complete information for this book */
    private String mInfoUrl;

    /** URL that leads to a thumbnail for this book */
    private String mSmallThumbnailUrl;

    /**
     * Constructor for a new Book object
     * @param title Title of this book
     * @param authors Author or authors of this book
     * @param rating Average rating
     * @param ratingsCount Amount of people that have reviewed
     * @param url URL leading to complete information
     * @param smallThumbnailUrl URL leading to thumbnail
     */
    public Book(String title, ArrayList<String> authors, double rating, int ratingsCount,
                String url, String smallThumbnailUrl) {
        mTitle = title;
        mAuthors = authors;
        mRating = rating;
        mRatingsCount = ratingsCount;
        mInfoUrl = url;
        mSmallThumbnailUrl = smallThumbnailUrl;
    }

    /**
     * Returns the book title
     * @return book title
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns authors list
     * @return authors list
     */
    public ArrayList<String> getAuthors() {
        return mAuthors;
    }

    /**
     * Returns average rating
     * @return average rating
     */
    public double getRating() {
        return mRating;
    }

    /**
     * Return amount of people who left a review
     * @return amount of people who rated
     */
    public int getRatingsCount() {
        return mRatingsCount;
    }

    /**
     * Returns a URL leading to complete information on this book
     * @return URL to complete information
     */
    public String getUrl() {
        return mInfoUrl;
    }

    /**
     * Returns a URL leading to a thumbnail for this book
     * @return URL to thumbnail
     */
    public String getSmallThumbnailUrl() {
        return mSmallThumbnailUrl;
    }
}
