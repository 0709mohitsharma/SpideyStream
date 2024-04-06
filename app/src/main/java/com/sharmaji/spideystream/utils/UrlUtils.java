package com.sharmaji.spideystream.utils;

import android.content.Context;
import android.util.Log;
import com.sharmaji.spideystream.parsers.DomainParser;
import com.sharmaji.spideystream.parsers.IdParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class UrlUtils {
    private final String url;
    private boolean isMovie = true;
    List<String> streamUrls;
    List<String> workingHosts = new ArrayList<>();
    private final OnUrlGenerationListener listener;
    private final Context context;

    public interface OnUrlGenerationListener {
        void onUrlGenerated(String streamUrl);
        void onError(String message);
    }

    public UrlUtils(Context context, String url, Boolean isMovie, OnUrlGenerationListener listener) {
        this.url = url;
        this.isMovie = isMovie;
        this.listener = listener;
        this.context = context;
        streamUrls = new ArrayList<>();
        getWorkingHost();
    }

//  Getting url type to generate API url
    private void getDbHostType() {
        // If working hosts are empty then set the cached hosts.
        if (!workingHosts.isEmpty()) PrefsHandler.setHosts(context, workingHosts);
        else workingHosts = PrefsHandler.getHosts(context);

        if(url.contains("imdb.com")){
            generateStreamUrl(true);
        } else if (url.contains("themoviedb.org")) {
            generateStreamUrl(false);
        }else{
            listener.onError("Invalid Url Received!");
        }
    }

    private void generateStreamUrl(boolean isImdb) {
        String host = PrefsHandler.getLastBestHost(context);
        String urlPrefix = genUrlPrefix(isImdb);

        if (!workingHosts.isEmpty()){
            for (String h : workingHosts) {
                streamUrls.add(h+urlPrefix);
            }
            // Generate all urls and then returns the best one
            new UrlTester(bestUrl -> {
                //Work with the best URL
                if (bestUrl == null || bestUrl.isEmpty()){
                    listener.onUrlGenerated(host+urlPrefix);
                    Log.e("UriUtils","The Default Url is: "+bestUrl);
                }else{
                    listener.onUrlGenerated(bestUrl);
                    Log.d("UriUtils","The BEst Url is: "+bestUrl);
                }
            }).testUrls(streamUrls.toArray(new String[0]));
        }else{
            streamUrls = PrefsHandler.getHosts(context);
        }

    }

    private String genUrlPrefix(boolean isImdb) {
        String id = getContentId(isImdb);
        String streamEndPoint = "";
        if (id == null) {
            Log.e("Parser Error:", "Unable to parse content id from the url, it returned null;");
            listener.onError("Unable to parse url content ID!");
        }
        if (isMovie){
            streamEndPoint = "/embed/movie/"+id;
        }else{
            streamEndPoint = "/embed/tv/"+id;
        }
        Log.d("LogSerial", "generateStreamUrl: Generated!");
        return streamEndPoint;
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

}
