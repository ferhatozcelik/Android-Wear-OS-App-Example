package com.ferhatozcelik.mycodes;

import android.graphics.Bitmap;

public class Data {

    String title;
    String data;
    String image;

    public Data(String title, String data, String image) {
        this.title = title;
        this.data = data;
        this.image = image;
    }

    public Data() {

    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
