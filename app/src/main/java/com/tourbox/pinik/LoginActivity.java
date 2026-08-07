package com.tourbox.pinik;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ScrollView loginScrollView;

    private EditText editTextEmail;
    private EditText editTextPassword;

    private Button buttonLogin;

    private TextView textForgotPassword;
    private TextView textCreateAccount;

    private FirebaseAuth firebaseAuth;

    private boolean wasKeyboardVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        connectViews();
        setupFocusScrolling();
        setupClickListeners();
        setupKeyboardListener();
    }

    /**
     * Connects Java variables with XML views.
     */
    private void connectViews() {

        loginScrollView =
                findViewById(R.id.loginScrollView);

        editTextEmail =
                findViewById(R.id.editTextEmail);

        editTextPassword =
                findViewById(R.id.editTextPassword);

        buttonLogin =
                findViewById(R.id.buttonLogin);

        textForgotPassword =
                findViewById(R.id.textForgotPassword);

        textCreateAccount =
                findViewById(R.id.textCreateAccount);
    }

    /**
     * Sets up button and link actions.
     */
    private void setupClickListeners() {

        buttonLogin.setOnClickListener(view ->
                loginUser()
        );

        textForgotPassword.setOnClickListener(view -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    ForgotPasswordActivity.class
            );

            startActivity(intent);
        });

        textCreateAccount.setOnClickListener(view -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    /**
     * Reads the login details and signs in with Firebase.
     */
    private void loginUser() {

        String email =
                editTextEmail
                        .getText()
                        .toString()
                        .trim();

        /*
         * Do not trim passwords because spaces
         * may be part of the password.
         */
        String password =
                editTextPassword
                        .getText()
                        .toString();

        if (!validateLoginInput(email, password)) {
            return;
        }

        setLoginLoading(true);

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(
                        this,
                        loginTask -> {

                            if (!loginTask.isSuccessful()) {

                                setLoginLoading(false);

                                showLoginError(
                                        loginTask.getException()
                                );

                                return;
                            }

                            FirebaseUser user =
                                    firebaseAuth.getCurrentUser();

                            if (user == null) {

                                setLoginLoading(false);

                                Toast.makeText(
                                        LoginActivity.this,
                                        "User information could not be loaded.",
                                        Toast.LENGTH_LONG
                                ).show();

                                firebaseAuth.signOut();

                                return;
                            }

                            checkEmailVerification();
                        }
                );
    }

    /**
     * Validates the email and password.
     */
    private boolean validateLoginInput(
            String email,
            String password
    ) {

        editTextEmail.setError(null);
        editTextPassword.setError(null);

        if (email.isEmpty()) {

            editTextEmail.setError(
                    "Email is required."
            );

            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            editTextEmail.setError(
                    "Enter a valid email address."
            );

            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {

            editTextPassword.setError(
                    "Password is required."
            );

            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Reloads the Firebase user and checks whether
     * the email address has been verified.
     */
    private void checkEmailVerification() {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            setLoginLoading(false);

            Toast.makeText(
                    LoginActivity.this,
                    "User information could not be loaded.",
                    Toast.LENGTH_LONG
            ).show();

            firebaseAuth.signOut();

            return;
        }

        currentUser.reload()
                .addOnCompleteListener(
                        this,
                        reloadTask -> {

                            setLoginLoading(false);

                            if (!reloadTask.isSuccessful()) {

                                firebaseAuth.signOut();

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Could not check your email verification status.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            FirebaseUser refreshedUser =
                                    firebaseAuth.getCurrentUser();

                            if (refreshedUser == null) {

                                firebaseAuth.signOut();

                                Toast.makeText(
                                        LoginActivity.this,
                                        "User information could not be loaded.",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            if (!refreshedUser.isEmailVerified()) {

                                showEmailVerificationDialog(
                                        refreshedUser
                                );

                                return;
                            }

                            openHomePage();
                        }
                );
    }

    /**
     * Shows a dialog when the user's email
     * has not been verified.
     */
    private void showEmailVerificationDialog(
            FirebaseUser user
    ) {

        AlertDialog verificationDialog =
                new AlertDialog.Builder(
                        LoginActivity.this
                )
                        .setTitle(
                                "Email Not Verified"
                        )
                        .setMessage(
                                "Please verify your email before logging in. "
                                        + "Check your inbox and spam folder.\n\n"
                                        + "You can also resend the verification email."
                        )
                        .setPositiveButton(
                                "Resend Email",
                                (dialog, which) ->
                                        resendVerificationEmail(user)
                        )
                        .setNegativeButton(
                                "Close",
                                (dialog, which) -> {

                                    firebaseAuth.signOut();
                                    dialog.dismiss();
                                }
                        )
                        .setCancelable(false)
                        .create();

        verificationDialog.show();
    }

    /**
     * Sends another verification email.
     */
    private void resendVerificationEmail(
            FirebaseUser user
    ) {

        if (user == null) {

            firebaseAuth.signOut();

            Toast.makeText(
                    LoginActivity.this,
                    "User information could not be loaded.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        setLoginLoading(true);

        user.sendEmailVerification()
                .addOnCompleteListener(
                        this,
                        emailTask -> {

                            setLoginLoading(false);

                            /*
                             * The user must verify the email
                             * before remaining signed in.
                             */
                            firebaseAuth.signOut();

                            if (emailTask.isSuccessful()) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Verification email sent. Check your inbox and spam folder.",
                                        Toast.LENGTH_LONG
                                ).show();

                            } else {

                                showVerificationEmailError(
                                        emailTask.getException()
                                );
                            }
                        }
                );
    }

    /**
     * Displays an error when the verification
     * email cannot be sent.
     */
    private void showVerificationEmailError(
            Exception exception
    ) {

        String message =
                "The verification email could not be sent. Please try again.";

        if (exception
                instanceof FirebaseTooManyRequestsException) {

            message =
                    "Too many requests were made. Please wait a few minutes and try again.";

        } else if (exception != null
                && exception.getMessage() != null) {

            message =
                    "The verification email could not be sent: "
                            + exception.getMessage();
        }

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Opens HomeActivity after successful login.
     */
    private void openHomePage() {

        Toast.makeText(
                LoginActivity.this,
                "Login successful!",
                Toast.LENGTH_SHORT
        ).show();

        Intent intent = new Intent(
                LoginActivity.this,
                HomeActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    /**
     * Displays Firebase login errors.
     */
    private void showLoginError(
            Exception exception
    ) {

        String message =
                "Login failed. Please check your email and password.";

        if (exception
                instanceof FirebaseAuthInvalidUserException) {

            message =
                    "No account was found with this email address.";

        } else if (exception
                instanceof FirebaseAuthInvalidCredentialsException) {

            message =
                    "The email or password is incorrect.";

        } else if (exception
                instanceof FirebaseTooManyRequestsException) {

            message =
                    "Too many login attempts. Please wait a few minutes and try again.";

        } else if (exception != null
                && exception.getMessage() != null) {

            message =
                    exception.getMessage();
        }

        Toast.makeText(
                LoginActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Controls the Login button loading state.
     */
    private void setLoginLoading(
            boolean loading
    ) {

        buttonLogin.setEnabled(
                !loading
        );

        buttonLogin.setText(
                loading
                        ? "Logging in..."
                        : "Log In"
        );

        editTextEmail.setEnabled(
                !loading
        );

        editTextPassword.setEnabled(
                !loading
        );

        textForgotPassword.setEnabled(
                !loading
        );

        textCreateAccount.setEnabled(
                !loading
        );
    }

    /**
     * Sets up automatic scrolling when an
     * input field receives focus.
     */
    private void setupFocusScrolling() {

        editTextEmail.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollFieldAboveKeyboard(
                                editTextEmail
                        );
                    }
                }
        );

        editTextEmail.setOnClickListener(
                view ->
                        scrollFieldAboveKeyboard(
                                editTextEmail
                        )
        );

        editTextPassword.setOnFocusChangeListener(
                (view, hasFocus) -> {

                    if (hasFocus) {

                        scrollFieldAboveKeyboard(
                                editTextPassword
                        );
                    }
                }
        );

        editTextPassword.setOnClickListener(
                view ->
                        scrollFieldAboveKeyboard(
                                editTextPassword
                        )
        );
    }

    /**
     * Scrolls the selected field above the keyboard.
     */
    private void scrollFieldAboveKeyboard(
            View field
    ) {

        loginScrollView.postDelayed(
                () -> {

                    Rect fieldRectangle =
                            new Rect();

                    field.getDrawingRect(
                            fieldRectangle
                    );

                    loginScrollView
                            .offsetDescendantRectToMyCoords(
                                    field,
                                    fieldRectangle
                            );

                    int scrollPosition =
                            fieldRectangle.top
                                    - dpToPx(40);

                    loginScrollView.smoothScrollTo(
                            0,
                            Math.max(
                                    scrollPosition,
                                    0
                            )
                    );

                },
                300
        );
    }

    /**
     * Returns the page to the original position
     * when the keyboard closes.
     */
    private void setupKeyboardListener() {

        View rootView =
                findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(
                rootView,
                (view, windowInsets) -> {

                    boolean keyboardVisible =
                            windowInsets.isVisible(
                                    WindowInsetsCompat.Type.ime()
                            );

                    if (wasKeyboardVisible
                            && !keyboardVisible) {

                        loginScrollView.postDelayed(
                                () ->
                                        loginScrollView
                                                .smoothScrollTo(
                                                        0,
                                                        0
                                                ),
                                150
                        );
                    }

                    wasKeyboardVisible =
                            keyboardVisible;

                    return windowInsets;
                }
        );

        rootView.post(
                () ->
                        ViewCompat.requestApplyInsets(
                                rootView
                        )
        );
    }

    /**
     * Converts dp into pixels.
     */
    private int dpToPx(
            int dp
    ) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return Math.round(
                dp * density
        );
    }
}