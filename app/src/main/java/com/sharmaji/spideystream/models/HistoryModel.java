package com.sharmaji.spideystream.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "watch_history")
public class HistoryModel {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    private String source_url_id;
    private String name;
    private String time;
    private String stream_url;
    private boolean isAvailable =  false;
    private String thumb_url;

    public HistoryModel(String source_url_id, String name, String time, String stream_url, String thumb_url) {
        this.source_url_id = source_url_id;
        this.name = name;
        this.time = time;
        this.stream_url = stream_url;
        this.thumb_url = thumb_url;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getSource_url_id() {
        return source_url_id;
    }

    public void setSource_url_id(String source_url_id) {
        this.source_url_id = source_url_id;
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

    public String getStream_url() {
        return stream_url;
    }

    public void setStream_url(String stream_url) {
        this.stream_url = stream_url;
    }
}