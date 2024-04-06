package com.sharmaji.spideystream.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "watch_history")
public class HistoryModel {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String time;
    private String url;

    public HistoryModel(String name, String time, String url) {
        this.name = name;
        this.time = time;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}