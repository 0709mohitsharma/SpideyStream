package com.sharmaji.spideystream.models;

public class HostModel {
    int id;
    String host;

    public HostModel() {}

    public HostModel(int id, String host) {
        this.id = id;
        this.host = host;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
