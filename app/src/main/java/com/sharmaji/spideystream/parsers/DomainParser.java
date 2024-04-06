package com.sharmaji.spideystream.parsers;

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

public class DomainParser {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final OnParseFinishListener listener;

    // Interface for the finish listener
    public interface OnParseFinishListener {
        void onParseFinish(List<String> domainList);
    }

    // Constructor
    public DomainParser(OnParseFinishListener listener) {
        this.listener = listener;
    }

    public void executeParsing(String url) {
        executor.execute(() -> {
            List<String> domainList = doInBackground(url);
            onPostExecute(domainList);
        });
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
