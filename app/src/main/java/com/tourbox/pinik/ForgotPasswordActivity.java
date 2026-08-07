package com.tourbox.pinik;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity
        extends AppCompatActivity {

    private EditText editTextResetEmail;
    private Button buttonSendResetLink;
    private ProgressBar progressReset;
    private TextView textBackToLogin;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(
                R.layout.activity_forgot_password
        );

        firebaseAuth = FirebaseAuth.getInstance();

        editTextResetEmail =
                findViewById(R.id.editTextResetEmail);

        buttonSendResetLink =
                findViewById(R.id.buttonSendResetLink);

        progressReset =
                findViewById(R.id.progressReset);

        textBackToLogin =
                findViewById(R.id.textBackToLogin);

        buttonSendResetLink.setOnClickListener(
                view -> sendResetLink()
        );

        textBackToLogin.setOnClickListener(
                view -> finish()
        );
    }

    private void sendResetLink() {

        String email = editTextResetEmail
                .getText()
                .toString()
                .trim();

        if (email.isEmpty()) {
            editTextResetEmail.setError(
                    "Enter your email address."
            );

            editTextResetEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            editTextResetEmail.setError(
                    "Enter a valid email address."
            );

            editTextResetEmail.requestFocus();
            return;
        }

        setLoading(true);

        firebaseAuth
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    setLoading(false);

                    if (task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "If an account exists for this email, a reset link has been sent. Check your inbox and spam folder.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Exception exception = task.getException();

                    if (exception
                            instanceof FirebaseNetworkException) {

                        Toast.makeText(
                                this,
                                "Check your internet connection and try again.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "The reset link could not be sent. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void setLoading(boolean loading) {

        progressReset.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        buttonSendResetLink.setEnabled(!loading);
    }
}