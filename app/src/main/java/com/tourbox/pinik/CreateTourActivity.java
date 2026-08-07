package com.tourbox.pinik;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateTourActivity extends AppCompatActivity {

    private View createTourMain;
    private View createTourScroll;

    private TextView textBack;
    private TextInputLayout layoutTourTitle;
    private TextInputLayout layoutTourLocation;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutEndDate;
    private TextInputEditText editTourTitle;
    private TextInputEditText editTourLocation;
    private TextInputEditText editStartDate;
    private TextInputEditText editEndDate;
    private TextInputEditText editTourDescription;
    private MaterialButton buttonSaveTour;
    private ProgressBar progressCreateTour;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private Long selectedStartDateMillis;
    private Long selectedEndDateMillis;

    private final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_create_tour);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        connectViews();
        configureSystemBars();
        applySystemInsets();
        setupListeners();
    }

    private void connectViews() {

        createTourMain = findViewById(R.id.createTourMain);
        createTourScroll = findViewById(R.id.createTourScroll);

        textBack = findViewById(R.id.textCreateTourBack);
        layoutTourTitle = findViewById(R.id.layoutTourTitle);
        layoutTourLocation = findViewById(R.id.layoutTourLocation);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutEndDate = findViewById(R.id.layoutEndDate);

        editTourTitle = findViewById(R.id.editTourTitle);
        editTourLocation = findViewById(R.id.editTourLocation);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        editTourDescription = findViewById(R.id.editTourDescription);

        buttonSaveTour = findViewById(R.id.buttonSaveTour);
        progressCreateTour = findViewById(R.id.progressCreateTour);
    }

    private void configureSystemBars() {
        WindowCompat.getInsetsController(
                getWindow(),
                getWindow().getDecorView()
        ).setAppearanceLightStatusBars(false);

        WindowCompat.getInsetsController(
                getWindow(),
                getWindow().getDecorView()
        ).setAppearanceLightNavigationBars(false);
    }

    private void applySystemInsets() {

        final int start = createTourScroll.getPaddingStart();
        final int top = createTourScroll.getPaddingTop();
        final int end = createTourScroll.getPaddingEnd();
        final int bottom = createTourScroll.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                createTourMain,
                (view, windowInsets) -> {

                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    createTourScroll.setPaddingRelative(
                            start + systemBars.left,
                            top + systemBars.top,
                            end + systemBars.right,
                            bottom + systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        createTourMain.post(
                () -> ViewCompat.requestApplyInsets(createTourMain)
        );
    }

    private void setupListeners() {

        textBack.setOnClickListener(view -> finish());

        editStartDate.setOnClickListener(view -> showStartDatePicker());
        layoutStartDate.setEndIconOnClickListener(view -> showStartDatePicker());

        editEndDate.setOnClickListener(view -> showEndDatePicker());
        layoutEndDate.setEndIconOnClickListener(view -> showEndDatePicker());

        buttonSaveTour.setOnClickListener(view -> saveTour());
    }

    private void showStartDatePicker() {

        Calendar initialDate = Calendar.getInstance();

        if (selectedStartDateMillis != null) {
            initialDate.setTimeInMillis(selectedStartDateMillis);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {

                    selectedStartDateMillis = createStartOfDayMillis(
                            year,
                            month,
                            dayOfMonth
                    );

                    editStartDate.setText(
                            dateFormatter.format(new Date(selectedStartDateMillis))
                    );

                    layoutStartDate.setError(null);

                    if (selectedEndDateMillis != null
                            && selectedEndDateMillis < selectedStartDateMillis) {

                        selectedEndDateMillis = null;
                        editEndDate.setText(null);
                    }
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(createTodayStartMillis());
        dialog.show();
    }

    private void showEndDatePicker() {

        if (selectedStartDateMillis == null) {
            layoutStartDate.setError(
                    getString(R.string.pinik_error_select_start_date_first)
            );
            return;
        }

        Calendar initialDate = Calendar.getInstance();
        initialDate.setTimeInMillis(
                selectedEndDateMillis != null
                        ? selectedEndDateMillis
                        : selectedStartDateMillis
        );

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, dayOfMonth) -> {

                    selectedEndDateMillis = createEndOfDayMillis(
                            year,
                            month,
                            dayOfMonth
                    );

                    editEndDate.setText(
                            dateFormatter.format(new Date(selectedEndDateMillis))
                    );

                    layoutEndDate.setError(null);
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(selectedStartDateMillis);
        dialog.show();
    }

    private long createTodayStartMillis() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long createStartOfDayMillis(int year, int month, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long createEndOfDayMillis(int year, int month, int dayOfMonth) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    private void saveTour() {

        String title = getText(editTourTitle);
        String location = getText(editTourLocation);
        String description = getText(editTourDescription);

        if (!validateInput(title, location)) {
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            openLoginPage();
            return;
        }

        setLoading(true);

        List<String> memberIds = Collections.singletonList(user.getUid());
        String creatorName = ActivityLogger.getActorName(user);

        DocumentReference tourReference =
                firestore.collection("tours").document();

        Map<String, Object> tourData = new HashMap<>();
        tourData.put("tourId", tourReference.getId());
        tourData.put("title", title);
        tourData.put("location", location);
        tourData.put("description", description);
        tourData.put("startDate", new Timestamp(new Date(selectedStartDateMillis)));
        tourData.put("endDate", new Timestamp(new Date(selectedEndDateMillis)));
        tourData.put("status", "upcoming");
        tourData.put("createdBy", user.getUid());
        tourData.put("createdByName", creatorName);
        tourData.put("memberIds", memberIds);
        tourData.put("createdAt", FieldValue.serverTimestamp());
        tourData.put("updatedAt", FieldValue.serverTimestamp());

        String activityMessage = getString(
                R.string.pinik_activity_tour_created,
                creatorName,
                title
        );

        Map<String, Object> activityData =
                ActivityLogger.createActivityData(
                        user,
                        tourReference.getId(),
                        null,
                        ActivityLogger.TYPE_TOUR_CREATED,
                        activityMessage,
                        memberIds
                );

        DocumentReference activityReference =
                firestore.collection("activities").document();

        WriteBatch writeBatch = firestore.batch();
        writeBatch.set(tourReference, tourData);
        writeBatch.set(activityReference, activityData);

        writeBatch.commit()
                .addOnSuccessListener(unused -> {

                    setLoading(false);

                    Toast.makeText(
                            CreateTourActivity.this,
                            R.string.pinik_tour_created_success,
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(exception -> {

                    setLoading(false);

                    Toast.makeText(
                            CreateTourActivity.this,
                            R.string.pinik_tour_create_failed,
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private boolean validateInput(String title, String location) {

        layoutTourTitle.setError(null);
        layoutTourLocation.setError(null);
        layoutStartDate.setError(null);
        layoutEndDate.setError(null);

        if (title.isEmpty()) {
            layoutTourTitle.setError(
                    getString(R.string.pinik_error_tour_title_required)
            );
            editTourTitle.requestFocus();
            return false;
        }

        if (location.isEmpty()) {
            layoutTourLocation.setError(
                    getString(R.string.pinik_error_tour_location_required)
            );
            editTourLocation.requestFocus();
            return false;
        }

        if (selectedStartDateMillis == null) {
            layoutStartDate.setError(
                    getString(R.string.pinik_error_start_date_required)
            );
            return false;
        }

        if (selectedEndDateMillis == null) {
            layoutEndDate.setError(
                    getString(R.string.pinik_error_end_date_required)
            );
            return false;
        }

        if (selectedEndDateMillis < selectedStartDateMillis) {
            layoutEndDate.setError(
                    getString(R.string.pinik_error_invalid_end_date)
            );
            return false;
        }

        return true;
    }

    private String getText(TextInputEditText editText) {

        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private void setLoading(boolean loading) {

        progressCreateTour.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        buttonSaveTour.setEnabled(!loading);
        textBack.setEnabled(!loading);

        buttonSaveTour.setText(
                loading
                        ? R.string.pinik_saving_tour
                        : R.string.pinik_save_tour
        );
    }

    private void openLoginPage() {

        Intent intent = new Intent(
                CreateTourActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}
