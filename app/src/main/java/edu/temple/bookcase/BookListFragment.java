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
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class BookListFragment extends Fragment {

    private static final String BOOK_LIST = "bookList";

    private ArrayList<Book> books;

    private ArrayList<String> bookNames;

    private BookListListener mListener;

    private Context parent;

    //Initialize listView
    ListView listView;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(ArrayList<Book> books) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();

        args.putParcelableArrayList(BOOK_LIST, books);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getParcelableArrayList(BOOK_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);
        setBookNames();

        //Create Adapter
        ArrayAdapter <String> adapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_list_item_1, bookNames);

        listView = v.findViewById(R.id.listView);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = books.get(position);
                mListener.onBookListInteraction(book);
            }
        });

        return v;
    }

//    public void onItemSelected(int index) {
//        if (mListener != null) {
//            mListener.onBookListInteraction(index);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookListListener) {
            mListener = (BookListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
        parent = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setBookNames(){
        bookNames = new ArrayList<>();
        for(Book book: books){
            bookNames.add(book.getTitle());
        }
    }

    //Method to pass on the bookList to another fragment
    public ArrayList<Book> getBookList(){
        return books;
    }


    public interface BookListListener {
        public void onBookListInteraction(Book book);
    }
}
