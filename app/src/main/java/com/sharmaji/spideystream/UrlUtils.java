package com.sharmaji.spideystream;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UrlUtils {
    private final String url;
    private String streamUrl;
    private String streamEndPoint;
    private boolean isMovie = true;
    List<String> workingHosts = new ArrayList<>();
    private OnUrlGenerationListener listener;
    private Context context;

    public interface OnUrlGenerationListener {
        void onUrlGenerated(String streamUrl);
        void onError(String message);
    }

    public UrlUtils(Context context, String url, Boolean isMovie, OnUrlGenerationListener listener) {
        this.url = url;
        this.isMovie = isMovie;
        this.listener = listener;
        this.context = context;
        getWorkingHost();
    }

//  Getting url type to generate API url
    private void getDbHostType() {
        if(url.contains("imdb.com")){
            generateStreamUrl(true);
        } else if (url.contains("themoviedb.org")) {
            generateStreamUrl(false);
        }else{
            listener.onError("Invalid Url Received!");
        }
    }

    private void generateStreamUrl(boolean isImdb) {
        String host = "https://vidsrc.net";
        if (!workingHosts.isEmpty()){
            host = workingHosts.get(3);
        }
        String id = getContentId(isImdb);
        if (id == null) {
            Log.e("Parser Error:", "Unable to parse content id from the url, it returned null;");
            listener.onError("Unable to parse url content ID!");
        }
        if (isMovie){
            streamUrl = host+"/embed/movie/"+id;
            streamEndPoint = "/embed/movie/"+id;
        }else{
            streamUrl = host+"/embed/tv/"+id;
            streamEndPoint = "/embed/tv/"+id;
        }
        Log.d("LogSerial", "generateStreamUrl: Generated!");
        listener.onUrlGenerated(streamUrl);
    }

    private String getContentId(boolean isImdb) {
        //Todo: Parse the urls to get content id
        if (isImdb){
            return IdParser.extractImdbId(url);
        }else{
            return IdParser.extractTmdbId(url);
        }
    }

    private void getWorkingHost() {
        Log.d("LogSerial", "getWorkingHost: initiated!");
        DomainParser parser = new DomainParser(domainList -> {
            workingHosts = domainList;
            Log.d("LogSerial", "getWorkingHost: Completed!");
            getDbHostType();
        });
        parser.executeParsing("https://vidsrc.domains/");
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public List<String> getWorkingHosts() {
        return workingHosts;
    }

    public String getStreamEndPoint() {
        return streamEndPoint;
    }
}
