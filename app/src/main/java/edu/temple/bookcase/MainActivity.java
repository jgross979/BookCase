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
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
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
    private ArrayList<Book> books = new ArrayList<>();
    private ArrayList<BookDetailsFragment> detailsFragments;

    //Initialize JSON object
    public static JSONArray bookJSON;

    //Initialize worker thread
    Thread t;

    //Holds status of the containers
    private Fragment fragmentContainer;
    private Fragment fragmentContainer2;


    //Buttons
    Button searchButton;
    Button pauseButton;
    Button stopButton;

    //Text Boxes
    EditText searchText;

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


    /********SAVED STATE********/
    ArrayList<Integer> currentlyDownloaded = new ArrayList<>(); //An arrayList of currently downloaded books by id
    int currentlyPlaying; //Id of currently playing book (Use shared preference)
    int currentPosition; //Current position in current playing

    File file;
    int currentProgress = 0;
    int currentId = 1;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;



    public Handler progressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            if(nowPlaying.getText() == ""){
                int id = ((AudiobookService.BookProgress) msg.obj).getBookId();
                nowPlaying.setText(getTitle(id));
                seekBar.setMax(getDuration(id));
            }
            if(!paused){
                currentProgress ++;
                Log.d("PROGRESS",  "" + currentProgress);
                boolean isDownloaded = currentlyDownloaded.contains(currentId);

                if(isDownloaded){
                    Log.d("SOMETHING SAVED?", ""+ currentId);
                }
                editor.putInt("" + currentId, currentProgress);
                editor.commit();

                seekBar.setProgress(currentProgress);

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

//        Set info from memory
        retrieveCurrentlyDownloaded();
        for(int i: currentlyDownloaded){
            Log.d("CURRENT DOWNLOADS", ""+ i);
        }

        //Finds all necessary views
        config();

        //Load the previous search from shared preferences
        loadSearch();


        //Properly creates fragments based on device orientation
        configBooks();

        //Sets up event listeners
        configListeners();
    }

    public void retrieveCurrentlyDownloaded(){
        for(int i = 1; i < 8; i ++){
            File f = new File(getFilesDir(), ""+i);
            if(f.length() > 1000){
                currentlyDownloaded.add(i);
            }
        }
    }


    private void saveSearch() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(books);
        editor.putString("searchList", json);
        editor.apply();
    }

    private void loadSearch() {
        Gson gson = new Gson();
        String json = sharedPref.getString("searchList", null);
        Type type = new TypeToken<ArrayList<Book>>() {}.getType();
        books = gson.fromJson(json, type);

        if (books == null) {
            books = new ArrayList<>();
        }
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
                int isDownloaded = 0;
                if(currentlyDownloaded.contains(id)){
                    isDownloaded = 1;
                }

                Book temp = new Book(id, title, author, published, coverURL, duration, isDownloaded);

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

    public void downloadFile(int id){
        final String BOOK_DOWNLOAD_URL = "https://kamorris.com/lab/audlib/download.php?id="+ id;
        file = new File(getApplicationContext().getFilesDir(), "" + id);

        t = new Thread(){

            public void run(){

                try {
                    BufferedInputStream inputStream = new BufferedInputStream(new URL(BOOK_DOWNLOAD_URL).openStream());
                    FileOutputStream fileos = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    int byteContent;
                    while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                        fileos.write(data, 0, byteContent);
                    }
                } catch (IOException e) {
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

        currentProgress = 0;

        //Check if the file is in storage
        boolean isDownloaded = false;
        if(currentlyDownloaded.contains(id)){
            isDownloaded = true;
            currentId = id;
            currentProgress = sharedPref.getInt(""+currentId, 0);
        }

        //If it is downloaded, play from the the download. Otherwise, play by streaming
        if(isDownloaded){
            File f = new File(getFilesDir(), ""+id);
            Log.d("CURRENT POSITION", "" + sharedPref.getInt(""+id, 0));
            binder.play(f, currentProgress);
        }else{
            binder.play(id);
        }
        bookDuration = duration;
        bookID = id;
        binder.setProgressHandler(progressHandler);
        paused = false;
        seekBar.setMax(bookDuration);
        nowPlaying.setText("Now playing " + title + "...");
    }

    @Override
    public void onDownloadButtonInteraction(Book book) {
        //Download the file
        downloadFile(book.getId());

        //Add the id to the currently downloaded list
        currentlyDownloaded.add(book.getId());

        for(Book bk: books){
            if(bk.getId() == book.getId()){
                bk.setIsDownloaded(1);
            }
        }

        saveSearch();

    }

    @Override
    public void onDeleteButtonInteraction(int id, int duration, String title) {
       if(!currentlyDownloaded.contains(id)){
           Log.d("NOT DOWNLOADED", "That book is not downloaded and can't be deleted");
       }else{
           //Delete the actual file
           File f = new File(getFilesDir(), ""+id);
           f.delete();

           //Delete the reference from the currently downloaded list
           int index = currentlyDownloaded.indexOf(id);
           currentlyDownloaded.remove(index);

           for(Book bk: books){
               if(bk.getId() == id){
                   bk.setIsDownloaded(0);
               }
           }

           saveSearch();

           //Set current position to 0
           editor.putInt(""+id, 0);
       }
    }

    public String getTitle(int id){
        for(Book book: books){
            if(book.getId() == id){
                return book.getTitle();
            }
        }
        return "";
    }

    public int getDuration(int id){
        for(Book book: books){
            if(book.getId() == id){
                return book.getDuration();
            }
        }
        return 0;
    }

    public void config(){
        //Get status of the fragment containers
        fragmentContainer = getSupportFragmentManager().findFragmentById(R.id.container);
        fragmentContainer2 = getSupportFragmentManager().findFragmentById(R.id.container_2);


        //Set seekbar values
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMin(0);

        //Search bar and button
        searchText = findViewById(R.id.searchText);
        searchButton = findViewById(R.id.searchButton);

        //Audio playback views
        pauseButton = findViewById(R.id.pauseButton);
        stopButton = findViewById(R.id.stopButton);

        //Set now playing TextView
        nowPlaying = findViewById(R.id.nowPlaying);

        intent = new Intent(this, AudiobookService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);  //Bind service instead of start


        //init shared preferences
        sharedPref = getSharedPreferences("shared preferences", MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void configListeners(){
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the search text from the view
                String search = searchText.getText().toString();

                //Conduct the search
                setBookJSON(search);

                saveSearch();

                //Load new fragments
                loadFragments(singlePane);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get current position
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
                currentPosition = 0;
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


    public void configBooks(){
        //Set the bookList
        if(fragmentContainer == null && fragmentContainer2 == null){
//            setBookJSON("");
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

//        else if(fragmentContainer instanceof ViewPagerFragment){
//            books = ((ViewPagerFragment) fragmentContainer).getBookList();
//        }else if(fragmentContainer2 instanceof BookListFragment){
//            books = ((BookListFragment) fragmentContainer2).getBookList();
//        }


        //Check if in single pane mode
        singlePane = (findViewById(R.id.container_2) == null);

        //Load in fragments
        loadFragments(singlePane);
    }



}
