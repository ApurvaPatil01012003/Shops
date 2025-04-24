package com.exam.shops;

import android.content.res.Configuration;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Tutorial extends AppCompatActivity {
    WebView YoutubeWeb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tutorial);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        YoutubeWeb = findViewById(R.id.YoutubeWeb);
        String video = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/DUg2SWWK18I?si=K-oWaq2th01BCfkZ\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>";
        YoutubeWeb.loadData(video, "text/html", "utf-8");
        YoutubeWeb.getSettings().setJavaScriptEnabled(true);
        YoutubeWeb.setWebChromeClient(new WebChromeClient());


    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Get the new configuration
        int orientation = newConfig.orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Adjust layout parameters for landscape
            // Example:
            YoutubeWeb.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels;
            YoutubeWeb.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels;
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Adjust layout parameters for portrait
            // Example:
            YoutubeWeb.getLayoutParams().width = getResources().getDisplayMetrics().widthPixels;
            YoutubeWeb.getLayoutParams().height = getResources().getDisplayMetrics().heightPixels;
        }
    }
}
