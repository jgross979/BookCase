package edu.temple.bookcase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;


public class BookDetailsFragment extends Fragment {

    private BookDetailsListener mListener;

    //Keys
    private static final String BOOK_ID = "bookID";
    private static final String BOOK_TITLE = "bookTitle";
    private static final String BOOK_AUTHOR = "bookAuthor";
    private static final String BOOK_PUBLISHED = "bookPublished";
    private static final String BOOK_COVER_URL = "bookCoverURL";
    private static final String BOOK_DURATION = "bookDuration";

    //Book Variables
    private int bookID;
    private String bookTitle;
    private String bookAuthor;
    private int bookPublished;
    private String bookCoverURL;
    private int bookDuration;

    //Views
    TextView titleText;
    TextView authorText;
    TextView bookPublishedText;
    ImageView bookCover;

    Button playButton;



    public BookDetailsFragment() {
        // Required empty public constructor
    }


    public static BookDetailsFragment newInstance(Book book) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();

        args.putInt(BOOK_ID, book.getId());
        args.putString(BOOK_TITLE, book.getTitle());
        args.putString(BOOK_AUTHOR, book.getAuthor());
        args.putInt(BOOK_PUBLISHED, book.getPublished());
        args.putString(BOOK_COVER_URL, book.getCoverURL());
        args.putInt(BOOK_DURATION, book.getDuration());

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookID = getArguments().getInt(BOOK_ID);
            bookTitle = getArguments().getString(BOOK_TITLE);
            bookAuthor = getArguments().getString(BOOK_AUTHOR);
            bookPublished = getArguments().getInt(BOOK_PUBLISHED);
            bookCoverURL = getArguments().getString(BOOK_COVER_URL);
            bookDuration = getArguments().getInt(BOOK_DURATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);
        titleText = v.findViewById(R.id.titleText);
        authorText = v.findViewById(R.id.authorText);
        bookCover = v.findViewById(R.id.bookCover);
        bookPublishedText = v.findViewById(R.id.publishedText);

        playButton = v.findViewById(R.id.playButton);

        //Set text and the image URL
        titleText.setText(bookTitle);
        authorText.setText(bookAuthor);
        bookPublishedText.setText(Integer.toString(bookPublished));

        //Set up error logging for picasso
        Picasso.get().setLoggingEnabled(true);
        //Load URL image into book cover
        Picasso.get().load(bookCoverURL).fit().into(bookCover);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlayButtonInteraction(bookID, bookDuration, bookTitle);
            }
        });


//        displayBook(bookName, v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookDetailsListener) {
            mListener = (BookDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    //Used to update the fragment without generating a new fragment
    public void displayBook(Book book){
         titleText.setText(book.getTitle());
         authorText.setText(book.getAuthor());
         bookPublishedText.setText(Integer.toString(book.getPublished()));
         Picasso.get().load(book.getCoverURL()).fit().into(bookCover);
    }

    public interface BookDetailsListener{
        public void onPlayButtonInteraction(int id, int duration, String title);
    }






}
