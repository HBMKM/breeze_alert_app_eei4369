package com.s23010175.breezealert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class TipsAndGuidesActivity extends AppCompatActivity {

    private LinearLayout articlesSection, videosSection, infographicsSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_and_guides);

        // Top bar actions
        ImageView backBtn = findViewById(R.id.tipsBackButton);
        ImageView homeBtn = findViewById(R.id.tipsHomeButton);

        backBtn.setOnClickListener(v -> finish());
        homeBtn.setOnClickListener(v ->
                startActivity(new Intent(TipsAndGuidesActivity.this, HomeActivity.class)));

        // Sections
        videosSection = findViewById(R.id.videosSection);
        infographicsSection = findViewById(R.id.infographicsSection);
        articlesSection = findViewById(R.id.articlesSection);

        // Show all by default
        showAll();

        // Filter buttons
        Button btnAll = findViewById(R.id.btnAll);
        Button btnVideos = findViewById(R.id.btnVideos);
        Button btnInfographics = findViewById(R.id.btnInfographics);
        Button btnArticles = findViewById(R.id.btnArticles);

        btnAll.setOnClickListener(v -> showAll());
        btnVideos.setOnClickListener(v -> showOnly("video"));
        btnInfographics.setOnClickListener(v -> showOnly("infographic"));
        btnArticles.setOnClickListener(v -> showOnly("article"));

        // Video clicks
        findViewById(R.id.videoItem1).setOnClickListener(v ->
                openUrl("https://youtu.be/cA3oj3NK2AY"));
        findViewById(R.id.videoItem2).setOnClickListener(v ->
                openUrl("https://youtu.be/_u8EBlC05Cw"));
        findViewById(R.id.videoItem3).setOnClickListener(v ->
                openUrl("https://youtu.be/PqSsoOBz3bQ"));
        findViewById(R.id.videoItem4).setOnClickListener(v ->
                openUrl("https://youtu.be/J6pDbzz9djI"));

        // Article clicks
        findViewById(R.id.articleItem1).setOnClickListener(v ->
                openUrl("https://www.telstra.com.au/connected/mobiles/how-to-beach-proof-your-phone"));
        findViewById(R.id.articleItem2).setOnClickListener(v ->
                openUrl("https://oceancityvacation.com/resources/blog/how-to-beach-proof-your-phone-and-other-electronic-devices/"));
        findViewById(R.id.articleItem3).setOnClickListener(v ->
                openUrl("https://www.netnanny.com/blog/15-ingenious-hacks-to-protect-your-phone-at-the-beach/"));

        // Infographic clicks -> show popup dialog with image
        findViewById(R.id.infoImg1).setOnClickListener(v -> showInfographic(R.drawable.infographic1));
        findViewById(R.id.infoImg2).setOnClickListener(v -> showInfographic(R.drawable.infographic2));
        findViewById(R.id.infoImg3).setOnClickListener(v -> showInfographic(R.drawable.infographic3));
        findViewById(R.id.infoImg4).setOnClickListener(v -> showInfographic(R.drawable.infographic4));
        findViewById(R.id.infoImg5).setOnClickListener(v -> showInfographic(R.drawable.infographic5));
    }

    private void showAll() {
        videosSection.setVisibility(View.VISIBLE);
        infographicsSection.setVisibility(View.VISIBLE);
        articlesSection.setVisibility(View.VISIBLE);
    }

    private void showOnly(String type) {
        videosSection.setVisibility(type.equals("video") ? View.VISIBLE : View.GONE);
        infographicsSection.setVisibility(type.equals("infographic") ? View.VISIBLE : View.GONE);
        articlesSection.setVisibility(type.equals("article") ? View.VISIBLE : View.GONE);
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(i);
    }

    private void showInfographic(int imageRes) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(imageRes);
        imageView.setAdjustViewBounds(true);

        new AlertDialog.Builder(this)
                .setView(imageView)
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
    }
}


