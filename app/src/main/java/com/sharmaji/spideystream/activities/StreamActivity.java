package com.sharmaji.spideystream.activities;

import android.app.ActionBar;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sharmaji.spideystream.R;

import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

import timber.log.Timber;

public class StreamActivity extends AppCompatActivity {
    AdblockWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();
        hideSystemUI();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stream);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Getting Stream url
        String url = getIntent().getStringExtra("URL");
        Timber.tag("Streaming Url").d(url);

        // Initializing webView
        webView = findViewById(R.id.web_view);
        ProgressBar progressBar = findViewById(R.id.video_load_progress);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }
        });
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