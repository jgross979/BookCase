package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainer;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.aware.DiscoverySession;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

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

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookListListener, BookDetailsFragment.BookDetailsListener {

    //Fragment variables
    private FragmentManager fm;
    private boolean singlePane;

    //List of books and BookDetailsfragments
    private ArrayList<Book> books;
    private ArrayList<BookDetailsFragment> detailsFragments;

    //Initialize JSON object
    public static JSONArray bookJSON;

    //Initialize worker thread
    Thread t;

    //Holds status of the containers
    private Fragment fragmentContainer;
    private Fragment fragmentContainer2;

    /********AUDIO SERVICE SETUP********/
    //Initialize Audio Service Binder
    AudiobookService.MediaControlBinder binder;
    boolean connected;
    boolean paused;

    Intent intent;
    SeekBar seekBar;
    TextView nowPlaying;

    int bookDuration;
    int bookID;


    //TODO Look at the instructions and make a version of the handler provided
    public Handler progressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            if(!paused){
                AudiobookService.BookProgress progress = (AudiobookService.BookProgress) msg.obj;
                int prog = progress.getProgress();
                seekBar.setProgress(prog);
            }
            return true;
        }
    });


    //Mechanism to let client know it is connected to service
    //Also gives a tool to speak to the service
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            binder = (AudiobookService.MediaControlBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
            binder = null;
        }
    };

    /****************************************/

    //Handler sets the JSON object for use
    public Handler displayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                bookJSON = new JSONArray((String) msg.obj);
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

        //Get status of the fragment containers
        fragmentContainer = getSupportFragmentManager().findFragmentById(R.id.container);
        fragmentContainer2 = getSupportFragmentManager().findFragmentById(R.id.container_2);

        //Search bar and button
        final EditText searchText = findViewById(R.id.searchText);
        Button searchButton = findViewById(R.id.searchButton);

        //Audio playback views
        final Button pauseButton = findViewById(R.id.pauseButton);
        Button stopButton = findViewById(R.id.stopButton);

        //Set seekbar values
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMin(0);

        //Set now playing TextView
        nowPlaying = findViewById(R.id.nowPlaying);

        intent = new Intent(this, AudiobookService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);  //Bind service instead of start

        //Set the bookList
        if(fragmentContainer == null && fragmentContainer2 == null){
            setBookJSON("");
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else if(fragmentContainer instanceof ViewPagerFragment){
            books = ((ViewPagerFragment) fragmentContainer).getBookList();
        }else if(fragmentContainer2 instanceof BookListFragment){
            books = ((BookListFragment) fragmentContainer2).getBookList();
        }


        //Check if in single pane mode
        singlePane = (findViewById(R.id.container_2) == null);

        //Load in fragments
        loadFragments(singlePane);



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchText.getText().toString();
                setBookJSON(search);
                loadFragments(singlePane);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binder.pause();
                if(paused == false){
                    paused = true;
                    pauseButton.setText(R.string.cont);
                }else{
                    paused = false;
                    pauseButton.setText(R.string.pause);
                }

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Stop using the stop method
                binder.stop();

                //Stop the actual service that's running
                stopService(intent);
                pauseButton.setText(R.string.pause);
                seekBar.setProgress(0);
                nowPlaying.setText("");
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser && connected){
                    binder.play(bookID, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Necessary for the listener
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Necessary for the listener
            }
        });






    }

    public void loadFragments(boolean singlePane){
        //Add the viewPager of bookDetailsFragments
        ViewPagerFragment vpf = ViewPagerFragment.newInstance(books);


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
                int duration = bookJSON.getJSONObject(i).getInt("duration");

                Book temp = new Book(id, title, author, published, coverURL, duration);

                bookList.add(temp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return bookList;
    }

    public void setBookJSON(String search){
        final String urlString = "https://kamorris.com/lab/audlib/booksearch.php?search=" + search;
        t = new Thread(){

            public void run(){

                URL url;

                try{
                    url = new URL(urlString);
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

        Fragment bdf =  getSupportFragmentManager().findFragmentById(R.id.container);

        //If Book Details fragment already exists
        if(bdf instanceof BookDetailsFragment && !singlePane){
            ((BookDetailsFragment) bdf).displayBook(book);
        }else if(!singlePane){
            //Create new book details fragment
            bdf = BookDetailsFragment.newInstance(book);
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.container, bdf)
                    .commit();
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(serviceConnection); //unbind from service to prevent memory leaks
        nowPlaying.setText("");
    }

    @Override
    public void onPlayButtonInteraction(int id, int duration, String title) {
        this.startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);  //Bind service instead of start
        binder.play(id);
        bookDuration = duration;
        bookID = id;
        binder.setProgressHandler(progressHandler);
        paused = false;
        seekBar.setMax(bookDuration);
        nowPlaying.setText("Now playing " + title + "...");
    }



}
