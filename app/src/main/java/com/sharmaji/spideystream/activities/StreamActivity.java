package com.sharmaji.spideystream.activities;

import static android.net.http.NetworkException.ERROR_CONNECTION_RESET;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sharmaji.spideystream.R;
import com.sharmaji.spideystream.utils.PrefsHandler;
import com.sharmaji.spideystream.utils.Utils;

import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

public class StreamActivity extends AppCompatActivity {
    AdblockWebView webView;
    private String streamingUrl;
    private List<String> hosts;
    private boolean hasLoaded = false;
    private boolean hasSwitched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();
        hideSystemUI();
        hosts = PrefsHandler.getHosts(StreamActivity.this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stream);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Getting Stream url
        streamingUrl = getIntent().getStringExtra("URL");
        Timber.tag("Streaming Url").d(streamingUrl);

        // Initializing webView
        webView = findViewById(R.id.web_view);
        ProgressBar progressBar = findViewById(R.id.video_load_progress);
        webView.getSettings().setSafeBrowsingEnabled(true);
        webView.getSettings().setUserAgentString(Utils.userAgent);
        webView.loadUrl(streamingUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                hasLoaded = false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                hasLoaded = true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                if (error != null && error.getErrorCode() == ERROR_CONNECTION_RESET) {
                    webView.postDelayed(() -> {
                        webView.loadUrl(streamingUrl);
                    }, 1000);
                }

                int errorCode = error.getErrorCode();
                if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
                    Log.e("URL ERROR", "onReceivedError: File not found error!");
                    if (hasLoaded) {
                        switchContent(streamingUrl);
                        webView.loadUrl(streamingUrl);
                        Toast.makeText(StreamActivity.this, "404 Error: File not found", Toast.LENGTH_SHORT).show();
                    }
                }
//                else if (errorCode == WebViewClient.ERROR_CONNECT) {
//                    if (hosts.isEmpty()) {
//                        Toast.makeText(StreamActivity.this, "No suitable streaming host found!", Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        replaceHost(streamingUrl);
//                        webView.loadUrl(streamingUrl);
//                    }
//                }
                else {
                    Log.e("URL ERROR", "onReceivedError: " + errorCode);
//                    Toast.makeText(StreamActivity.this, "Error: " + errorCode, Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void switchContent(String streamingUrl) {
        if (hasSwitched){
            Toast.makeText(this, "Sorry, this content isn't available yet!", Toast.LENGTH_SHORT).show();
            finish();
        }
        hasSwitched = true;
        Toast.makeText(this, "Trying content switching...", Toast.LENGTH_SHORT).show();
        if (streamingUrl.contains("/embed/movie/")){
            streamingUrl.replace("/embed/movie/","/embed/tv/");
        }else{
            streamingUrl.replace("/embed/tv/","/embed/movie/");
        }
    }

    private void replaceHost(String urlString) {
        try {
            URL url = new URL(urlString);
            String oldHost = url.getHost();
            if (hosts.contains(oldHost)) {
                String newHost = hosts.get(0); // Assuming the first host in the list is the replacement host
                URL newUrl = new URL(url.getProtocol(), newHost, url.getPort(), url.getFile());
                String newUrlString = newUrl.toString();

                // Now replace the host of the URL in the original URL string
                urlString = urlString.replaceFirst(oldHost, newHost);
                streamingUrl = urlString;
                // Remove the used host from the list
                removeUsedHost(urlString);
            }
        } catch (MalformedURLException e) {
            // Handle malformed URL exception
            e.printStackTrace();
        }
    }
    private void removeUsedHost(String urlString) {
        try {
            URL url = new URL(urlString);
            String host = url.getHost();
            hosts.remove(host);
        } catch (MalformedURLException e) {
            // Handle malformed URL exception
            e.printStackTrace();
        }
    }

    private void setFullScreen() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    private void hideSystemUI() {
        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);

        WindowInsetsController controller = window.getInsetsController();
        if (controller != null) {
            // Hide both the status bar and the navigation bar
            controller.hide(WindowInsets.Type.systemBars());
            // Set behavior to show transient bars by swipe
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

}