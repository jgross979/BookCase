package edu.temple.bookcase;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ViewPagerFragment extends Fragment {


    //Global structures and variables
    private ViewPager viewPager;
    private ArrayList<Book> books;
    protected ArrayList<BookDetailsFragment> detailsFragments = new ArrayList<>();

    //Global keys
    private static final String BOOK_LIST = "bookList";


    public ViewPagerFragment() {
        // Required empty public constructor
    }

    public static ViewPagerFragment newInstance(ArrayList<Book> books) {
        ViewPagerFragment fragment = new ViewPagerFragment();
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
            createDetailFragmentList(books);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_view_pager, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.viewPager);

        viewPager.setAdapter(buildAdapter());

        return v;
    }

    //creates the BookDetailsFragments and places them into the arrayList
    public void createDetailFragmentList(List<Book> books){
        detailsFragments = new ArrayList<>();

        for(Book book: books){
            BookDetailsFragment bdf = BookDetailsFragment.newInstance(book);
            detailsFragments.add(bdf);
        }

    }

    //Method to pass on the bookList to another fragment
    public ArrayList<Book> getBookList(){
        return books;
    }

    private PagerAdapter buildAdapter() {
        return(new ViewFragmentAdapter(getActivity(), getChildFragmentManager()));
    }


    class ViewFragmentAdapter extends FragmentStatePagerAdapter {

        Context ctxt = null;

        public ViewFragmentAdapter(Context ctxt, FragmentManager fm) {
            super(fm);
            this.ctxt = ctxt;
        }

        public Fragment getItem(int position) {
            return detailsFragments.get(position);
        }

        @Override
        public int getCount() {
            return detailsFragments.size();
        }

        @Override
        public int getItemPosition(Object object) { return PagerAdapter.POSITION_NONE; }
    }


}
