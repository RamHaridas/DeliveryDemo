package com.white.delivery.ui.home;

import android.graphics.Bitmap;

import java.net.URI;

public class ImageContainer {
    int number;
    URI imageuri;
    Bitmap imageBitmap;
    public ImageContainer(int number, URI imageuri, Bitmap imageBitmap) {
        this.number = number;
        this.imageuri = imageuri;
        this.imageBitmap = imageBitmap;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public URI getImageuri() {
        return imageuri;
    }

    public void setImageuri(URI imageuri) {
        this.imageuri = imageuri;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
}
