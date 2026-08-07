package com.tourbox.pinik;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ImageView imageOnboarding;

    private TextView textTopTitle;
    private TextView textTopDescription;
    private TextView textCardTitle;
    private TextView textCardDescription;

    private Button buttonNext;

    private View[] indicatorDots;

    private int currentPage = 0;

    private final String[] topTitles = {
            "Welcome to Pinik",
            "Decide Together",
            "Track Every Expense",
            "Travel Safe. Save Memories."
    };

    private final String[] topDescriptions = {
            "The perfect app for friends who love to travel together.",
            "Let every group member participate in important tour decisions.",
            "Keep tour budgets, payments and shared costs clear for everyone.",
            "Stay connected during your trip and preserve every special moment."
    };

    private final String[] cardTitles = {
            "Plan. Decide. Explore.",
            "Every Voice Matters.",
            "Simple and Transparent.",
            "Your Journey in One Place."
    };

    private final String[] cardDescriptions = {
            "All your tour plans, votes, locations, expenses and memories in one place.",
            "Create polls for destinations, dates, hotels, transport, food and activities.",
            "Record expenses, split shared costs and see who has paid or still owes money.",
            "Share live locations, save visited places, store hotels and create private photo albums."
    };

    /*
     * We use the same image temporarily.
     * Later, replace these with four separate onboarding images.
     */
    private final int[] onboardingImages = {
            R.drawable.pinik_splash_background,
            R.drawable.pinik_splash_background,
            R.drawable.pinik_splash_background,
            R.drawable.pinik_splash_background
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        connectViews();

        updatePage();

        buttonNext.setOnClickListener(view -> {

            if (currentPage < topTitles.length - 1) {

                currentPage++;
                updatePage();

            } else {

                completeOnboarding();
            }
        });
    }

    private void connectViews() {

        imageOnboarding = findViewById(R.id.imageOnboarding);

        textTopTitle =
                findViewById(R.id.textOnboardingTopTitle);

        textTopDescription =
                findViewById(R.id.textOnboardingTopDescription);

        textCardTitle =
                findViewById(R.id.textOnboardingCardTitle);

        textCardDescription =
                findViewById(R.id.textOnboardingCardDescription);

        buttonNext =
                findViewById(R.id.buttonOnboardingNext);

        indicatorDots = new View[]{
                findViewById(R.id.dotOne),
                findViewById(R.id.dotTwo),
                findViewById(R.id.dotThree),
                findViewById(R.id.dotFour)
        };
    }

    private void updatePage() {

        imageOnboarding.setImageResource(
                onboardingImages[currentPage]
        );

        textTopTitle.setText(
                topTitles[currentPage]
        );

        textTopDescription.setText(
                topDescriptions[currentPage]
        );

        textCardTitle.setText(
                cardTitles[currentPage]
        );

        textCardDescription.setText(
                cardDescriptions[currentPage]
        );

        if (currentPage == topTitles.length - 1) {

            buttonNext.setText("Get Started");

        } else {

            buttonNext.setText("Next");
        }

        updateIndicators();
    }

    private void updateIndicators() {

        for (int i = 0; i < indicatorDots.length; i++) {

            LinearLayout.LayoutParams params;

            if (i == currentPage) {

                params = new LinearLayout.LayoutParams(
                        dpToPx(28),
                        dpToPx(9)
                );

                indicatorDots[i].setBackgroundResource(
                        R.drawable.bg_dot_active
                );

            } else {

                params = new LinearLayout.LayoutParams(
                        dpToPx(9),
                        dpToPx(9)
                );

                indicatorDots[i].setBackgroundResource(
                        R.drawable.bg_dot_inactive
                );
            }

            if (i > 0) {
                params.setMarginStart(dpToPx(12));
            }

            indicatorDots[i].setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {

        float density =
                getResources().getDisplayMetrics().density;

        return Math.round(dp * density);
    }

    private void completeOnboarding() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "PinikPreferences",
                        MODE_PRIVATE
                );

        preferences.edit()
                .putBoolean("onboarding_completed", true)
                .apply();

        Intent intent = new Intent(
                OnboardingActivity.this,
                LoginActivity.class
        );

        startActivity(intent);

        finish();
    }
}