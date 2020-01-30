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
    private String user;

    private VideoInformationStatus status;

    @JsonCreator
    public VideoInformation(String videoName, String authorName, String user){
        this.videoName = videoName;
        this.authorName = authorName;
        this.user=user;
        this.status = VideoInformationStatus.WAITINGUPLOAD;
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

    public String getUser() {
        return user;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setUser(String user) { this.user = user;}
    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public VideoInformationStatus getStatus() {
        return status;
    }

    public void setStatus(VideoInformationStatus status) {
        this.status = status;
    }
}
