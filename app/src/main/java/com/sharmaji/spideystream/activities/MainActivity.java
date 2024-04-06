package com.sharmaji.spideystream.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.sharmaji.spideystream.R;
import com.sharmaji.spideystream.utils.UrlUtils;
import com.sharmaji.spideystream.utils.Utils;

public class MainActivity extends AppCompatActivity {
    //Todo: Add support for newly added movies and series
    //Todo: Add episodes support
    //Todo: Add Watch history
    //Todo: Retry method & pick the best server
    //Todo: Cache the domains
    //Todo: Stream with the best domain
    // Currently used url & choose url feature via all stream urls

    private boolean isMovie = true;
    private boolean unchecked = true;
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
        findViewById(R.id.git_txt).setOnClickListener(v -> {
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

        TextInputEditText urlEdit = findViewById(R.id.textInputEditText);
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
        findViewById(R.id.btn_paste).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = Utils.getClipboardText(MainActivity.this);
                if (!url.isEmpty())
                    urlEdit.setText(url);
                else
                    Toast.makeText(MainActivity.this, "Empty Clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        goBtn.setOnClickListener(v -> {
            if (!unchecked) {
                String url = urlEdit.getText().toString();
                if (url.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter a valid url!", Toast.LENGTH_SHORT).show();
                } else {
                    runOnUiThread(()->{
                        setProgress(true);
                    });
                    handleURL(url,isMovie);
                }
            }else{
                Toast.makeText(this, "Is it a movie or series ?", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void setProgress(boolean isVisible){
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
                Intent intent = new Intent(MainActivity.this, StreamActivity.class);
                intent.putExtra("URL", streamUrl);
                startActivity(intent);
                runOnUiThread(()->{
                    setProgress(false);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(()->{
                    setProgress(false);
                });
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}