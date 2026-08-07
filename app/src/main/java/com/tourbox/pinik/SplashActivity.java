package com.tourbox.pinik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2500L;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private final Runnable openNextScreen =
            this::decideNextScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        handler.postDelayed(
                openNextScreen,
                SPLASH_DELAY
        );
    }

    private void decideNextScreen() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "PinikPreferences",
                        MODE_PRIVATE
                );

        boolean onboardingCompleted =
                preferences.getBoolean(
                        "onboarding_completed",
                        false
                );

        Intent intent;

        /*
         * Show onboarding only when the user has
         * not completed it before.
         */
        if (!onboardingCompleted) {

            intent = new Intent(
                    SplashActivity.this,
                    OnboardingActivity.class
            );

        } else {

            FirebaseAuth firebaseAuth =
                    FirebaseAuth.getInstance();

            FirebaseUser currentUser =
                    firebaseAuth.getCurrentUser();

            /*
             * Open Home directly when Firebase still
             * has a verified signed-in user.
             */
            if (currentUser != null
                    && currentUser.isEmailVerified()) {

                intent = new Intent(
                        SplashActivity.this,
                        HomeActivity.class
                );

            } else {

                /*
                 * Remove any unverified cached session.
                 */
                if (currentUser != null) {
                    firebaseAuth.signOut();
                }

                intent = new Intent(
                        SplashActivity.this,
                        LoginActivity.class
                );
            }
        }

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(openNextScreen);
        super.onDestroy();
    }
}