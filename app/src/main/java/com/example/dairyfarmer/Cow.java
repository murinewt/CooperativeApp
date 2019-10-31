package com.example.dairyfarmer;

public class Cow {
    public String cowName, tagId, cowOrBull, dateOfB, details, image, url;

    public Cow() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Cow(String cowName, String tagId,String cowOrBull,String dateOfB, String cowDetails, String image, String url) {
        this.cowName = cowName;
        this.tagId = tagId;
        this.cowOrBull= cowOrBull;
        this.dateOfB = dateOfB;
        this.details = cowDetails;
        this.image = image;
        this.url = url;
    }
    public String getCowName() {
        return cowName;
    }
    public String getTagId() {
        return tagId;
    }
    public String getCowOrBull() {
        return cowOrBull;
    }
    public String getDateOfB() {
        return dateOfB;
    }
    public String getCowDetails() {
        return details;
    }
    public String getImage() {
        return image;
    }
    public String getImageURL() {
        return url;
    }

}
