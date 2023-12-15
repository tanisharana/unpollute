package com.example.trashdetection.model;

public class User {
    String id;
    String username;
    String imageURL;
    String status;
    String bio;
    String search;


    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getStatus() {
        return status;
    }

    public String getBio() {
        return bio;
    }

    public String getSearch() {
        return search;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
