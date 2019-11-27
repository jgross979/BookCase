package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;


public class Book implements Parcelable {

    private int id;
    private String title;
    private String author;
    private int published;
    private String coverURL;
    private int duration;

    public Book(int id, String title, String author, int published, String coverURL, int duration){
        this.id = id;
        this.title = title;
        this.author = author;
        this.published = published;
        this.coverURL = coverURL;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublished() {
        return published;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public int getDuration() { return duration;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(published);
        dest.writeString(coverURL);
        dest.writeInt(duration);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    private Book(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.author = in.readString();
        this.published = in.readInt();
        this.coverURL = in.readString();
        this.duration = in.readInt();
    }
}
