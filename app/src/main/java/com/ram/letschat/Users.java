package com.ram.letschat;

/**
 * Created by RJGUP on 28-12-2017.
 */

public class Users {
    public String name;
    public String image;
    public String status;
    public String thumb_image;
    public boolean online;

    public Users(){

    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Users(String name, String image, String status, String thumb_image, boolean online) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
        this.online = online;


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
