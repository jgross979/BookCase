package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListListener {

    private FragmentManager fm;
    private boolean singlePane;
    private List<String> books;

    //Initialize ArrayList of BookDetailsFragments
    private ArrayList<BookDetailsFragment> detailsFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set book names into the book list
        books = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.book_array)));

        //Check if in single pane mode
        singlePane = (findViewById(R.id.container_2) == null);

        //Add the viewPager of bookDetailsFragments
        ViewPagerFragment vpf = ViewPagerFragment.newInstance(books);

        if(singlePane && getSupportFragmentManager().findFragmentById(R.id.container) == null){
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, vpf)
                        .commit();
        }else{
            //Create the book list fragment
            BookListFragment blf = BookListFragment.newInstance(books);
            //Create the book details fragment
            BookDetailsFragment bdf = BookDetailsFragment.newInstance(books.get(0));
            fm = getSupportFragmentManager();
            fm.beginTransaction()
                .replace(R.id.container_2, blf)
                .replace(R.id.container, bdf)
                .commit();
        }





    }

    @Override
    public void onBookListInteraction(String bookName) {
        //Pass book title to details fragment
        BookDetailsFragment bdf = BookDetailsFragment.newInstance(bookName);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, bdf)
                .commit();

    }

}
