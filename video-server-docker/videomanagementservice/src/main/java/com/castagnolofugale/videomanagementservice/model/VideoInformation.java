package com.castagnolofugale.videomanagementservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "video-informations")
public class VideoInformation {

    @Id
    private ObjectId _id;

    private String videoName;

    private String authorName;

    @JsonCreator
    public VideoInformation(String videoName, String authorName){
        this.videoName = videoName;
        this.authorName = authorName;
    }

    @JsonGetter("_id")
    public String get_id_string(){
        return _id.toHexString();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }


}
