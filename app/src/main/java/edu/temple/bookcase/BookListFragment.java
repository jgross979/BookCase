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

    private static final String BOOK_ARRAY_LIST = "books";

    private List<String> bookList;

    private BookListListener mListener;

    private Context parent;

    //Initialize listView
    ListView listView;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(List<String> books) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();

        args.putStringArrayList(BOOK_ARRAY_LIST, (ArrayList<String>) books);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = getArguments().getStringArrayList(BOOK_ARRAY_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);

        //Create Adapter
        ArrayAdapter <String> adapter = new ArrayAdapter<String>(parent,
                android.R.layout.simple_list_item_1, bookList);

        listView = v.findViewById(R.id.listView);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String bookName = bookList.get(position);
                ((BookListListener) mListener).onBookListInteraction(bookName);
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


    public interface BookListListener {
        public void onBookListInteraction(String bookName);
    }
}
