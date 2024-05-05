package com.sharmaji.spideystream.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sharmaji.spideystream.R;
import com.sharmaji.spideystream.adapters.HistoryAdapter;
import com.sharmaji.spideystream.databinding.ActivityMainBinding;
import com.sharmaji.spideystream.models.HistoryModel;
import com.sharmaji.spideystream.room.Repository;
import com.sharmaji.spideystream.utils.UrlUtils;
import com.sharmaji.spideystream.utils.Utils;

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
    private String title="";
    private String thumb_url ="";
    private String source_url="";

    private Repository repository;
    private boolean unchecked = true;
    private String streamUrl;
    private ActivityMainBinding binding;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        
        // Initializing View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initializing repo
        repository = new Repository(getApplication());

        binding.playContent.setOnClickListener(v -> {
            if (streamUrl!=null) {
                handleURL(streamUrl, isMovie);
            }
        });

        binding.gitImg.setOnClickListener(v -> {
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

        binding.typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
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
                binding.textInputEditText.setText(uri.toString());
            }
        }
        binding.textInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Utils.getClipboardText(MainActivity.this);
                if (!url.isEmpty())
                    binding.textInputEditText.setText(url);
                else
                    Toast.makeText(MainActivity.this, "Empty Clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        binding.textInputEditText.setInputType(InputType.TYPE_NULL);

        binding.btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Utils.getClipboardText(MainActivity.this);
                if (!url.isEmpty())
                    binding.textInputEditText.setText(url);
                else {
                    Toast.makeText(MainActivity.this, "Empty Clipboard", Toast.LENGTH_SHORT).show();
                    binding.textInputEditText.setText("");
                }
            }
        });
        binding.submitBtn.setOnClickListener(v -> {
            if (!unchecked) {
                String url = binding.textInputEditText.getText().toString();
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

        WebSettings webSettings = binding.imdbWebView.getSettings();
        webSettings.setSafeBrowsingEnabled(true);
        binding.imdbWebView.getSettings().setUserAgentString(Utils.userAgent);
        binding.imdbWebView.loadUrl("https://www.imdb.com");
//        binding.imdbWebView.loadUrl("https://www.themoviedb.org/");

        binding.imdbWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                setProgress(true);
                source_url = url;
                if(Utils.isValidUrl(url)){
                    binding.textInputEditText.setText(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setProgress(false);
                // JavaScript code to extract the content attribute of the meta tag with property="og:type"
                String javascript = "javascript:document.querySelector(\"meta[property='og:type']\").getAttribute(\"content\");";

                // JavaScript code to extract the title of the page
                String titleScript = "javascript:document.title;";

                // JavaScript code to extract the content attribute of the meta tag with property="twitter:image"
                String imageScript = "javascript:document.querySelector(\"meta[property='twitter:image']\").getAttribute(\"content\");";

                // Evaluate JavaScript code to extract title
                view.evaluateJavascript(titleScript, titleValue -> {
                    if (titleValue != null) {
                        title = titleValue.replaceAll("\"", "")
                                .replace("- IMDb","")
                                .replace("— The Movie Database (TMDB)","")
                                .replace("-",""); // Remove quotes
                    }
                });

                // Evaluate JavaScript code to extract image URL
                view.evaluateJavascript(imageScript, imageValue -> {
                    if (imageValue != null) {
                        thumb_url = imageValue.replaceAll("\"", ""); // Remove quotes
                    }
                });

                // Evaluate JavaScript code
                view.evaluateJavascript(javascript, value -> {
                    if (value != null) {
                        // Check if the value contains "video.movie" or "video.tv_show"
                        if (value.contains("video.movie")) {
                            streamUrl=url;
                            binding.textInputEditText.setText(url);
                            binding.typeGroup.check(R.id.movieRadio);
                            isMovie = true;
                            binding.playContent.setVisibility(View.VISIBLE);
                        } else if (value.contains("video.tv_show")) {
                            streamUrl=url;
                            binding.textInputEditText.setText(url);
                            binding.typeGroup.check(R.id.seriesRadio);
                            isMovie = false;
                            binding.playContent.setVisibility(View.VISIBLE);
                        } else {
                            binding.playContent.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        binding.switchImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.rvHistory.getVisibility()==View.GONE){
                    binding.imdbWebView.setVisibility(View.GONE);
                    binding.rvHistory.setVisibility(View.VISIBLE);
                    binding.gitImg.setVisibility(View.VISIBLE);
                    binding.btnClearHistory.setVisibility(View.VISIBLE);
                }else{
                    binding.imdbWebView.setVisibility(View.VISIBLE);
                    binding.rvHistory.setVisibility(View.GONE);
                    binding.gitImg.setVisibility(View.GONE);
                    binding.btnClearHistory.setVisibility(View.GONE);
                }
            }
        });
        dispatcher.addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                binding.imdbWebView.goBack();
            }
        });
        
        binding.searchToggle.setOnClickListener(v -> {
            if(binding.searchLayout.getVisibility()==View.GONE){
                binding.searchLayout.setVisibility(View.VISIBLE);
                binding.searchToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_up));
            }else{
                binding.searchToggle.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_down));
                binding.searchLayout.setVisibility(View.GONE);
            }
        });
    }


    private void setupRecyclerView() {
        LiveData<List<HistoryModel>> historyList = repository.getHistoryList();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        HistoryAdapter adapter = new HistoryAdapter(this, model -> {
            Intent intent = new Intent(MainActivity.this, StreamActivity.class);
            intent.putExtra("URL", model.getStream_url());
            intent.putExtra("SourceUrl",model.getSource_url_id());
            startActivity(intent);
        });
        historyList.observe(this, adapter::submitList);
        binding.rvHistory.setAdapter(adapter);

        binding.btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repository.deleteAll();
            }
        });
    }

    private void setProgress(boolean isVisible){
        title="";
        thumb_url="";
        Log.d("MainActivity", "setProgress: "+isVisible);
        if (isVisible)
            binding.progressLayout.setVisibility(View.VISIBLE);
        else
            binding.progressLayout.setVisibility(View.GONE);
    }

    private void handleURL(String url, boolean isMovie) {
        Log.d("LogSerial", "handleURL: initiated!");
        new UrlUtils(this, url, isMovie, new UrlUtils.OnUrlGenerationListener() {
            @Override
            public void onUrlGenerated(String streamUrl) {
                Log.d("LogSerial", "handleUrl > url Generated!");

                // Adding to watch history
                // Inserting a new history item
                if (title.isEmpty())
                    title = isMovie ? "Movie" : "Series";
                HistoryModel historyItem = new HistoryModel(source_url,title, Utils.getCurrentTimeAndDate(MainActivity.this), streamUrl, thumb_url);
                repository.insert(historyItem);

                Intent intent = new Intent(MainActivity.this, StreamActivity.class);
                intent.putExtra("URL", streamUrl);
                startActivity(intent);
                runOnUiThread(()->{
                    setProgress(false);
                    binding.textInputEditText.setText("");
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(()->{
                    setProgress(false);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    binding.textInputEditText.setText("");
                });
            }
        });
    }
}