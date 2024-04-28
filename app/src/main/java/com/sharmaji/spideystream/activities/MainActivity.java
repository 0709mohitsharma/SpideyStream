package com.sharmaji.spideystream.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.sharmaji.spideystream.R;
import com.sharmaji.spideystream.adapters.HistoryAdapter;
import com.sharmaji.spideystream.models.HistoryModel;
import com.sharmaji.spideystream.room.Repository;
import com.sharmaji.spideystream.utils.UrlUtils;
import com.sharmaji.spideystream.utils.Utils;

import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Todo: Add support for newly added movies and series
    //Todo: Add episodes support
    //Todo: Add Watch history
    //Todo: Retry method & pick the best server
    //Todo: Cache the domains
    //Todo: Stream with the best domain
    // Currently used url & choose url feature via all stream urls
    private final OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();

    private boolean isMovie = true;
    private TextInputEditText urlEdit;
    private AdblockWebView imdbWebView;
    private Repository repository;
    private boolean unchecked = true;
    private RecyclerView recyclerView;
    private CardView play;
    private String streamUrl;

    private LinearLayout progressLayout;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new Repository(getApplication());
        play = findViewById(R.id.playContent);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (streamUrl!=null) {
                    handleURL(streamUrl, isMovie);
                }
            }
        });
        findViewById(R.id.git_img).setOnClickListener(v -> {
            // Create an Intent with ACTION_VIEW and the URI of the link
            String githubUrl = "https://github.com/Mohit-Sharmaji";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl));

            // Try to open the URI
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Handle the case where no application is available to handle the Intent
                Toast.makeText(this, "No application available to open the link: " + githubUrl, Toast.LENGTH_SHORT).show();
            }
        });

        setupRecyclerView();

        urlEdit = findViewById(R.id.textInputEditText);
        Button goBtn = findViewById(R.id.submitBtn);
        progressLayout = findViewById(R.id.progressLayout);

        RadioGroup radioGroup = findViewById(R.id.typeGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.movieRadio) {
                // Movie radio button is selected
                isMovie = true;
                unchecked = false;
            } else if (checkedId == R.id.seriesRadio) {
                // Series radio button is selected
                isMovie = false;
                unchecked = false;
            }else{
                unchecked = true;
            }
        });

        // Handle intent data
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            // Get the data URI from the intent
            Uri uri = getIntent().getData();

            // Check if URI is not null and if it belongs to themoviedb.org or imdb.com
            if (uri != null) {
                urlEdit.setText(uri.toString());
            }
        }
        findViewById(R.id.textInputEditText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Utils.getClipboardText(MainActivity.this);
                if (!url.isEmpty())
                    urlEdit.setText(url);
                else
                    Toast.makeText(MainActivity.this, "Empty Clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        urlEdit.setInputType(InputType.TYPE_NULL);

        findViewById(R.id.btn_paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Utils.getClipboardText(MainActivity.this);
                if (!url.isEmpty())
                    urlEdit.setText(url);
                else {
                    Toast.makeText(MainActivity.this, "Empty Clipboard", Toast.LENGTH_SHORT).show();
                    urlEdit.setText("");
                }
            }
        });
        goBtn.setOnClickListener(v -> {
            if (!unchecked) {
                String url = urlEdit.getText().toString();
                if (url.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a valid url!", Toast.LENGTH_SHORT).show();
                } else {
                    setProgress(true);
                    handleURL(url,isMovie);
                }
            }else{
                Toast.makeText(this, "Is it a movie or series ?", Toast.LENGTH_SHORT).show();
            }
        });

        imdbWebView = findViewById(R.id.imdbWebView);
        imdbWebView.getSettings().setSafeBrowsingEnabled(true);
        imdbWebView.getSettings().setUserAgentString(Utils.userAgent);

        imdbWebView.loadUrl("https://www.imdb.com");
        imdbWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                setProgress(true);
                if(Utils.isValidUrl(url)){
                    urlEdit.setText(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setProgress(false);
                // JavaScript code to extract the content attribute of the meta tag with property="og:type"
                String javascript = "javascript:document.querySelector(\"meta[property='og:type']\").getAttribute(\"content\");";

                // Evaluate JavaScript code
                view.evaluateJavascript(javascript, value -> {
                    if (value != null) {
                        // Check if the value contains "video.movie" or "video.tv_show"
                        if (value.contains("video.movie")) {
                            streamUrl=url;
                            urlEdit.setText(url);
                            radioGroup.check(R.id.movieRadio);
                            isMovie = true;
                            play.setVisibility(View.VISIBLE);
                        } else if (value.contains("video.tv_show")) {
                            streamUrl=url;
                            urlEdit.setText(url);
                            radioGroup.check(R.id.seriesRadio);
                            isMovie = false;
                            play.setVisibility(View.VISIBLE);
                        } else {
                            play.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        findViewById(R.id.switch_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView.getVisibility()==View.GONE){
                    imdbWebView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.git_img).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_clear_history).setVisibility(View.VISIBLE);
                }else{
                    imdbWebView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.git_img).setVisibility(View.GONE);
                    findViewById(R.id.btn_clear_history).setVisibility(View.GONE);
                }
            }
        });
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                imdbWebView.goBack();
            }
        });

        LinearLayout searchLayout = findViewById(R.id.searchLayout);
        ImageView searchToggle = findViewById(R.id.search_toggle);
        searchToggle.setOnClickListener(v -> {
            if(searchLayout.getVisibility()==View.GONE){
                searchLayout.setVisibility(View.VISIBLE);
                searchToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_up));
            }else{
                searchToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_down));
                searchLayout.setVisibility(View.GONE);
            }
        });
    }


    private void setupRecyclerView() {
        LiveData<List<HistoryModel>> historyList = repository.getHistoryList();
        recyclerView = findViewById(R.id.rvHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        HistoryAdapter adapter = new HistoryAdapter(this, model -> {
            Intent intent = new Intent(MainActivity.this, StreamActivity.class);
            intent.putExtra("URL", model.getUrl());
            startActivity(intent);
        });
        historyList.observe(this, adapter::submitList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btn_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repository.deleteAll();
            }
        });
    }

    private void setProgress(boolean isVisible){
        Log.d("MainActivity", "setProgress: "+isVisible);
        if (isVisible)
            progressLayout.setVisibility(View.VISIBLE);
        else
            progressLayout.setVisibility(View.GONE);
    }

    private void handleURL(String url, boolean isMovie) {
        Log.d("LogSerial", "handleURL: initiated!");
        new UrlUtils(this, url, isMovie, new UrlUtils.OnUrlGenerationListener() {
            @Override
            public void onUrlGenerated(String streamUrl) {
                Log.d("LogSerial", "handleUrl > url Generated!");

                // Adding to watch history
                // Inserting a new history item
                String title = isMovie ? "Movie" : "Series";
                HistoryModel historyItem = new HistoryModel(title, Utils.getCurrentTimeAndDate(MainActivity.this), streamUrl);
                repository.insert(historyItem);

                Intent intent = new Intent(MainActivity.this, StreamActivity.class);
                intent.putExtra("URL", streamUrl);
                startActivity(intent);
                runOnUiThread(()->{
                    setProgress(false);
                    urlEdit.setText("");
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(()->{
                    setProgress(false);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    urlEdit.setText("");
                });
            }
        });
    }
}