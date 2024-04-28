package com.sharmaji.spideystream.parsers;

import android.os.AsyncTask;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DomainParser {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final OnParseFinishListener listener;

    // Interface for the finish listener
    public interface OnParseFinishListener {
        void onParseFinish(List<String> domainList);
    }

    // Constructor
    public DomainParser(OnParseFinishListener listener) {
        this.listener = listener;
    }

    public void executeParsing(String url, boolean addTimeout) {
        Future<List<String>> future = executor.submit(() -> doInBackground(url));
        try {
            List<String> domainList;
            if (addTimeout) {
                domainList = future.get(5, TimeUnit.SECONDS); // Timeout set to 5 seconds
            } else {
                domainList = future.get();
            }
            onPostExecute(domainList);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true); // Cancel the task if it exceeds the timeout
            Log.e("DomainParser", "Error parsing domain: " + e.getMessage());
            onPostExecute(null); // Notify listener with null list
        }
    }

    protected List<String> doInBackground(String url) {
        List<String> domainList = new ArrayList<>();

        try {
            // Fetch HTML content from the provided URL
            Document document = Jsoup.connect(url).get();

            // Extract domains from the m-list div containing "live" word
            Elements mListItems = document.select("ul.m-list li a:has(span.live-text)");
            for (Element listItem : mListItems) {
                String domain = listItem.attr("href");
                domainList.add(domain);
                Log.d("Fetched Domain", domain);
            }
        } catch (IOException e) {
            Log.e("DomainParser", "Error fetching HTML content: " + e.getMessage());
        }

        return domainList;
    }

    protected void onPostExecute(List<String> domainList) {
        // Notify the listener that parsing is finished
        if (listener != null) {
            listener.onParseFinish(domainList);
        }
    }
}