package com.example.android.booksearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Class that creates a custom ArrayAdapter to generate list items for book objects.
 */

public class BookAdapter extends ArrayAdapter<Book> {

    /**
     * Constructs a new {@link BookAdapter}.
     *
     * @param context of the app
     * @param books is the list of books, which is the data source of the adapter
     */
    public BookAdapter (Context context, List books){
        super(context, 0, books);
    }

    /**
     * Returns a list item view that displays information about the book at the given position
     * in the list of books.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Book currentBook = getItem(position);

        // Find image view for thumbnail
        ImageView bookThumbnailImageView = (ImageView) listItemView.findViewById(R.id.book_image);

        // Get URL that leads to thumbnail and remove curled edge effect on image's lower right
        // corner
        String smallThumbnailUrl = currentBook.getSmallThumbnailUrl().replace("&edge=curl", "");

        // Load image from internet and set it into image view
        Picasso.with(getContext()).load(smallThumbnailUrl).into(bookThumbnailImageView);

        TextView titleTextView = (TextView) listItemView.findViewById(R.id.title_text_view);

        titleTextView.setText(currentBook.getTitle());

        // Find authors text view
        TextView authorsTextView = (TextView) listItemView.findViewById(R.id.author_text_view);

        // If list of authors is empty, show that this information is not available. Otherwise
        // set text to authors array list without brackets at the beginning and end.
        if (currentBook.getAuthors().isEmpty()) {
            authorsTextView.setText(R.string.not_available);
        } else {
            String authors = currentBook.getAuthors().toString();
            authorsTextView.setText(authors.substring(1, authors.length() - 1));
        }

        // Find rating text view.
        TextView ratingTextView = (TextView) listItemView.findViewById(R.id.rating_text_view);

        //Get rating
        double rating = currentBook.getRating();

        // Check if a rating was found. -1 means that there was no rating found or that the value
        // couldn't be coerced to a double.
        if (rating == -1) {
            ratingTextView.setText(R.string.not_available);
        } else {
            ratingTextView.setText(rating + "");
        }

        // Find ratings count text view
        TextView ratingsCountTextView = (TextView) listItemView.findViewById(R.id.ratings_count_text_view);

        // Get ratings count
        int ratingsCount = currentBook.getRatingsCount();

        // Check if a ratings count was found. -1 means that there was no ratings count found or
        // that the value couldn't be coerced to an int.
        if (ratingsCount == -1) {
            ratingsCountTextView.setText(R.string.not_available);
        } else {
            ratingsCountTextView.setText("(" + ratingsCount + ")");
        }
        return listItemView;
    }
}
