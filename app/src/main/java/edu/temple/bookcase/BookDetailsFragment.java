package edu.temple.bookcase;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class BookDetailsFragment extends Fragment {

    private static final String BOOK_NAME = "bookName";

    private String bookName;


    public BookDetailsFragment() {
        // Required empty public constructor
    }


    public static BookDetailsFragment newInstance(String bookName) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle args = new Bundle();

        args.putString(BOOK_NAME, bookName);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookName = getArguments().getString(BOOK_NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_details, container, false);

        displayBook(bookName, v);

        return v;
    }

    public void displayBook(String title, View v){
        TextView bookTitleText = v.findViewById(R.id.bookTitleText);

        bookTitleText.setText(bookName);
    }






}
