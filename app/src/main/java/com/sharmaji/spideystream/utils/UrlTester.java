package com.sharmaji.spideystream.utils;

import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UrlTester {

    public interface OnUrlTestListener {
        void onUrlTestComplete(String bestUrl);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final OnUrlTestListener listener;

    public UrlTester(OnUrlTestListener listener) {
        this.listener = listener;
    }

    public void testUrls(String[] urls) {
        List<Future<String>> futures = new ArrayList<>();

        for (String url : urls) {
            Callable<String> task = createUrlTestTask(url);
            futures.add(executor.submit(task));
        }

        executor.shutdown();

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if (result != null) {
                    notifyListener(result);
                    return;
                }
            } catch (Exception e) {
                // Handle exception gracefully
                e.printStackTrace();
            }
        }

        // Notify listener that no URL was accessible
        notifyListener(null);
    }

    private Callable<String> createUrlTestTask(String url) {
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(5000); // Set connection timeout to 5 seconds
                connection.setReadTimeout(5000); // Set read timeout to 5 seconds
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Close the connection after use
                    connection.disconnect();
                    long endTime = System.currentTimeMillis();
                    long timeTaken = endTime - startTime;
                    return url;
                }
            } catch (IOException e) {
                // URL is not accessible
                // Handle exception gracefully
                // e.printStackTrace();
            }
            return null;
        };
    }

    private void notifyListener(final String bestUrl) {
        handler.post(() -> listener.onUrlTestComplete(bestUrl != null ? bestUrl : ""));
    }
}
