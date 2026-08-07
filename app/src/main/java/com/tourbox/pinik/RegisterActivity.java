package com.tourbox.pinik;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    /*
     * Gap between the Confirm Password field
     * and the top of the keyboard.
     */
    private static final int KEYBOARD_GAP_DP = 24;

    private View registerMain;
    private NestedScrollView registerScrollView;

    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private Button buttonRegister;
    private ProgressBar progressRegister;
    private TextView textBackToLogin;

    private FirebaseAuth firebaseAuth;

    private boolean wasKeyboardVisible = false;

    private int originalPaddingLeft;
    private int originalPaddingTop;
    private int originalPaddingRight;
    private int originalPaddingBottom;

    private Runnable pendingScrollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();

        // Firebase will use the phone/app language for authentication emails.
        firebaseAuth.useAppLanguage();

        connectViews();
        saveOriginalPadding();
        setupClickListeners();
        setupKeyboardAwareScrolling();
    }

    private void connectViews() {

        registerMain = findViewById(R.id.registerMain);
        registerScrollView = findViewById(R.id.registerScrollView);

        editTextName = findViewById(R.id.editTextRegisterName);
        editTextEmail = findViewById(R.id.editTextRegisterEmail);
        editTextPassword = findViewById(R.id.editTextRegisterPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        buttonRegister = findViewById(R.id.buttonRegister);
        progressRegister = findViewById(R.id.progressRegister);
        textBackToLogin = findViewById(R.id.textBackToLogin);
    }

    private void saveOriginalPadding() {

        originalPaddingLeft = registerScrollView.getPaddingLeft();
        originalPaddingTop = registerScrollView.getPaddingTop();
        originalPaddingRight = registerScrollView.getPaddingRight();
        originalPaddingBottom = registerScrollView.getPaddingBottom();

        registerScrollView.setClipToPadding(false);
    }

    private void setupClickListeners() {

        buttonRegister.setOnClickListener(view -> registerUser());
        textBackToLogin.setOnClickListener(view -> finish());
    }

    private void setupKeyboardAwareScrolling() {

        setupAutoScrollForField(editTextName, false);
        setupAutoScrollForField(editTextEmail, false);
        setupAutoScrollForField(editTextPassword, false);
        setupAutoScrollForField(editTextConfirmPassword, true);

        ViewCompat.setOnApplyWindowInsetsListener(
                registerMain,
                (view, windowInsets) -> {

                    boolean keyboardVisible =
                            windowInsets.isVisible(WindowInsetsCompat.Type.ime());

                    int keyboardHeight =
                            windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;

                    if (keyboardVisible) {

                        registerScrollView.setPadding(
                                originalPaddingLeft,
                                originalPaddingTop,
                                originalPaddingRight,
                                originalPaddingBottom
                                        + keyboardHeight
                                        + dpToPx(16)
                        );

                        View focusedView = getCurrentFocus();

                        if (focusedView instanceof EditText) {

                            boolean alignNearKeyboard =
                                    focusedView == editTextConfirmPassword;

                            scheduleFieldPosition(
                                    focusedView,
                                    alignNearKeyboard
                            );
                        }

                    } else {

                        registerScrollView.setPadding(
                                originalPaddingLeft,
                                originalPaddingTop,
                                originalPaddingRight,
                                originalPaddingBottom
                        );

                        if (wasKeyboardVisible) {

                            cancelPendingScroll();

                            registerScrollView.postDelayed(
                                    () -> registerScrollView.smoothScrollTo(0, 0),
                                    150
                            );
                        }
                    }

                    wasKeyboardVisible = keyboardVisible;
                    return windowInsets;
                }
        );

        registerMain.post(
                () -> ViewCompat.requestApplyInsets(registerMain)
        );
    }

    private void setupAutoScrollForField(
            EditText field,
            boolean alignNearKeyboard
    ) {

        field.setOnFocusChangeListener((view, hasFocus) -> {

            if (hasFocus) {
                scheduleFieldPosition(view, alignNearKeyboard);
            }
        });

        field.setOnClickListener(
                view -> scheduleFieldPosition(view, alignNearKeyboard)
        );
    }

    private void scheduleFieldPosition(
            View field,
            boolean alignNearKeyboard
    ) {

        cancelPendingScroll();

        pendingScrollRunnable = () -> {

            positionFieldAboveKeyboard(field, alignNearKeyboard);
            pendingScrollRunnable = null;
        };

        registerScrollView.postDelayed(
                pendingScrollRunnable,
                220
        );
    }

    private void cancelPendingScroll() {

        if (pendingScrollRunnable != null) {

            registerScrollView.removeCallbacks(pendingScrollRunnable);
            pendingScrollRunnable = null;
        }
    }

    private void positionFieldAboveKeyboard(
            View field,
            boolean alignNearKeyboard
    ) {

        if (field == null || !field.isAttachedToWindow()) {
            return;
        }

        WindowInsetsCompat rootInsets =
                ViewCompat.getRootWindowInsets(registerMain);

        if (rootInsets == null
                || !rootInsets.isVisible(WindowInsetsCompat.Type.ime())) {
            return;
        }

        Rect visibleWindow = new Rect();
        registerMain.getWindowVisibleDisplayFrame(visibleWindow);

        int[] fieldLocation = new int[2];
        field.getLocationOnScreen(fieldLocation);

        int fieldBottom = fieldLocation[1] + field.getHeight();

        int desiredFieldBottom =
                visibleWindow.bottom - dpToPx(KEYBOARD_GAP_DP);

        int movement = fieldBottom - desiredFieldBottom;

        if (alignNearKeyboard) {

            if (Math.abs(movement) > dpToPx(6)) {

                int targetScrollY =
                        registerScrollView.getScrollY() + movement;

                registerScrollView.smoothScrollTo(
                        0,
                        Math.max(targetScrollY, 0)
                );
            }

        } else if (movement > 0) {

            int targetScrollY =
                    registerScrollView.getScrollY() + movement;

            registerScrollView.smoothScrollTo(
                    0,
                    Math.max(targetScrollY, 0)
            );
        }
    }

    private int dpToPx(int dp) {

        float density =
                getResources().getDisplayMetrics().density;

        return Math.round(dp * density);
    }

    private void registerUser() {

        String name =
                editTextName.getText().toString().trim();

        String email =
                editTextEmail.getText().toString().trim();

        // Passwords are not trimmed because spaces may be intentional.
        String password =
                editTextPassword.getText().toString();

        String confirmPassword =
                editTextConfirmPassword.getText().toString();

        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        createFirebaseAccount(name, email, password);
    }

    private boolean validateInput(
            String name,
            String email,
            String password,
            String confirmPassword
    ) {

        clearErrors();

        if (name.isEmpty()) {

            editTextName.setError("Enter your full name.");
            editTextName.requestFocus();
            return false;
        }

        if (name.length() < 2) {

            editTextName.setError("Enter a valid name.");
            editTextName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {

            editTextEmail.setError("Enter your email address.");
            editTextEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            editTextEmail.setError("Enter a valid email address.");
            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {

            editTextPassword.setError("Enter a password.");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {

            editTextPassword.setError(
                    "Password must contain at least 8 characters."
            );

            editTextPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {

            editTextConfirmPassword.setError(
                    "Enter the password again."
            );

            editTextConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {

            editTextConfirmPassword.setError(
                    "Passwords do not match."
            );

            editTextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void clearErrors() {

        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);
    }

    private void createFirebaseAccount(
            String name,
            String email,
            String password
    ) {

        setLoading(true);

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (!task.isSuccessful()) {

                        setLoading(false);
                        showRegistrationError(task.getException());
                        return;
                    }

                    FirebaseUser user =
                            task.getResult() != null
                                    ? task.getResult().getUser()
                                    : null;

                    if (user == null) {

                        setLoading(false);
                        firebaseAuth.signOut();

                        Toast.makeText(
                                RegisterActivity.this,
                                "The account was created, but user information could not be loaded.",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    saveDisplayName(user, name);
                });
    }

    private void saveDisplayName(
            FirebaseUser user,
            String name
    ) {

        UserProfileChangeRequest profileUpdate =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();

        user.updateProfile(profileUpdate)
                .addOnCompleteListener(profileTask -> {

                    if (!profileTask.isSuccessful()) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "The account was created, but the name could not be saved.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    sendVerificationEmail(user);
                });
    }

    private void sendVerificationEmail(FirebaseUser user) {

        user.sendEmailVerification()
                .addOnCompleteListener(emailTask -> {

                    setLoading(false);

                    // Never leave an unverified account signed in.
                    firebaseAuth.signOut();

                    if (emailTask.isSuccessful()) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "Account created. A verification link was sent to "
                                        + user.getEmail()
                                        + ". Check Inbox and Spam.",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        String message =
                                "Account created, but the verification email could not be sent.";

                        if (emailTask.getException() != null
                                && emailTask.getException().getMessage() != null) {

                            message += "\n" + emailTask.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    openLoginPage();
                });
    }

    private void openLoginPage() {

        Intent intent = new Intent(
                RegisterActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }

    private void showRegistrationError(Exception exception) {

        String message =
                "Registration failed. Please try again.";

        if (exception instanceof FirebaseAuthUserCollisionException) {

            message =
                    "An account already exists with this email. "
                            + "Go to Login, enter the same email and password, "
                            + "then choose Resend Verification Email.";

        } else if (exception instanceof FirebaseAuthWeakPasswordException) {

            message = "Please enter a stronger password.";

        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {

            message = "The email address is not valid.";

        } else if (exception != null
                && exception.getMessage() != null) {

            message = exception.getMessage();
        }

        Toast.makeText(
                RegisterActivity.this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    private void setLoading(boolean loading) {

        progressRegister.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        buttonRegister.setEnabled(!loading);

        buttonRegister.setText(
                loading ? "Creating Account..." : "Create Account"
        );
    }

    @Override
    protected void onDestroy() {

        cancelPendingScroll();
        super.onDestroy();
    }
}
