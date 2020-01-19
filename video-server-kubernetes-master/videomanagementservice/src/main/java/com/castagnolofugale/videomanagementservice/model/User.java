package com.castagnolofugale.videomanagementservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private ObjectId _id;

    @Email
    @NotNull(message = "The email parameter must not be blank!")
    private String email;

    @NotNull(message = "The username parameter must not be blank!")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "The password parameter must not be blank!")
    private String password;

    private List<String> roles;

    @JsonCreator
    public User(String username, String email, String password){
        this.email = email;
        this.username = username;
        this.password = password;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
