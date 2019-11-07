package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.aware.DiscoverySession;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListListener {

    private FragmentManager fm;
    private boolean singlePane;
    private ArrayList<Book> books;
    //Initialize ArrayList of BookDetailsFragments
    private ArrayList<BookDetailsFragment> detailsFragments;

    //Initialize JSON object
    public static JSONArray bookJSON;

    //Initialize worker thread
    Thread t;

    //Handler sets the JSON object for use
    public Handler displayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                bookJSON = new JSONArray((String) msg.obj);
//                Log.d("TITLE TITLE", bookJSON.getJSONObject(0).getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setBookJSON();
        //Wait for worker thread to finish gathering books from API
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Check if in single pane mode
        singlePane = (findViewById(R.id.container_2) == null);

        //Add the viewPager of bookDetailsFragments
        ViewPagerFragment vpf = ViewPagerFragment.newInstance(books);

        Fragment fragmentContainer = getSupportFragmentManager().findFragmentById(R.id.container);

        if(singlePane){
                getSupportFragmentManager().beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.container, vpf)
                        .commit();
        }else{
            //Create the book list fragment
            BookListFragment blf = BookListFragment.newInstance(books);
            fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .addToBackStack(null)
                .replace(R.id.container_2, blf)
                .commit();
        }





    }

    public ArrayList<Book> createBookList(JSONArray bookJSON){
        ArrayList<Book> bookList = new ArrayList<>();
        for(int i = 0; i < bookJSON.length(); i ++){
            try {
                int id = Integer.parseInt(bookJSON.getJSONObject(i).getString("book_id"));
                String title = bookJSON.getJSONObject(i).getString("title");
                String author = bookJSON.getJSONObject(i).getString("author");
                int published = Integer.parseInt(bookJSON.getJSONObject(i).getString("published"));
                String coverURL =  bookJSON.getJSONObject(i).getString("cover_url");

                Book temp = new Book(id, title, author, published, coverURL);

                bookList.add(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return bookList;
    }

    public void setBookJSON(){

        t = new Thread(){

            public void run(){
                URL url;

                try{
                    url = new URL("https://kamorris.com/lab/audlib/booksearch.php");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder builder =  new StringBuilder();
                    String response = reader.readLine();
                    while(response != null){
                        builder.append(response);
                        response = reader.readLine();
                    }
                    //Set values of JSONArray
                    try {
                        bookJSON = new JSONArray((String) builder.toString());
                        //Create arrayList of books from JSON
                        books = createBookList(bookJSON);
                        Log.d("HER HERE HERE", books.get(0).getTitle());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }

            }};
        t.start();
    }

    @Override
    public void onBookListInteraction(Book book) {
        //Pass book title to details fragment
        BookDetailsFragment bdf = BookDetailsFragment.newInstance(book);

        if(!singlePane){
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, bdf)
                    .commit();
        }

    }

}
