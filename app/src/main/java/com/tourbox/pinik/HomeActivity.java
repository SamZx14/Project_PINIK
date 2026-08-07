package com.tourbox.pinik;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private View homeMain;
    private NestedScrollView homeScrollView;
    private View bottomNavigationContainer;

    private TextView textGreeting;
    private ProgressBar progressHome;
    private View notificationDot;

    private final List<DocumentSnapshot> unreadNotificationDocuments =
            new ArrayList<>();

    private View layoutNoTour;
    private View cardUpcomingTour;
    private TextView textTourTitle;
    private TextView textTourDate;
    private TextView textTourLocation;
    private TextView textTourDuration;
    private MaterialButton buttonCreateFirstTour;

    private View layoutRecentEmpty;
    private View recentActivityGlassCard;
    private TextView textRecentEmptyMessage;
    private LinearLayout recentActivityContainer;

    private DocumentSnapshot selectedTourDocument;

    private boolean tourDataLoaded;
    private boolean activityDataLoaded;

    private final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        connectViews();
        configureSystemBars();
        applySystemInsets();
        setupFeatureTiles();
        setupClickListeners();
    }

    private void connectViews() {

        homeMain = findViewById(R.id.homeMain);
        homeScrollView = findViewById(R.id.homeScrollView);
        bottomNavigationContainer = findViewById(R.id.bottomNavigationContainer);

        textGreeting = findViewById(R.id.textGreeting);
        progressHome = findViewById(R.id.progressHome);
        notificationDot = findViewById(R.id.notificationDot);
        setNotificationDotVisible(false);

        layoutNoTour = findViewById(R.id.layoutNoTour);
        cardUpcomingTour = findViewById(R.id.cardUpcomingTour);
        textTourTitle = findViewById(R.id.textTourTitle);
        textTourDate = findViewById(R.id.textTourDate);
        textTourLocation = findViewById(R.id.textTourLocation);
        textTourDuration = findViewById(R.id.textTourDuration);
        buttonCreateFirstTour = findViewById(R.id.buttonCreateFirstTour);

        layoutRecentEmpty = findViewById(R.id.layoutRecentEmpty);
        recentActivityGlassCard = findViewById(R.id.recentActivityGlassCard);
        textRecentEmptyMessage = findViewById(R.id.textRecentEmptyMessage);
        recentActivityContainer = findViewById(R.id.recentActivityContainer);
    }

    private void configureSystemBars() {

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        getWindow(),
                        getWindow().getDecorView()
                );

        controller.setAppearanceLightStatusBars(false);
        controller.setAppearanceLightNavigationBars(false);
    }

    private void applySystemInsets() {

        final int scrollStart = homeScrollView.getPaddingStart();
        final int scrollTop = homeScrollView.getPaddingTop();
        final int scrollEnd = homeScrollView.getPaddingEnd();
        final int scrollBottom = homeScrollView.getPaddingBottom();

        final int navigationStart = bottomNavigationContainer.getPaddingStart();
        final int navigationTop = bottomNavigationContainer.getPaddingTop();
        final int navigationEnd = bottomNavigationContainer.getPaddingEnd();
        final int navigationBottom = bottomNavigationContainer.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(
                homeMain,
                (view, windowInsets) -> {

                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    homeScrollView.setPaddingRelative(
                            scrollStart + systemBars.left,
                            scrollTop + systemBars.top,
                            scrollEnd + systemBars.right,
                            scrollBottom
                    );

                    bottomNavigationContainer.setPaddingRelative(
                            navigationStart + systemBars.left,
                            navigationTop,
                            navigationEnd + systemBars.right,
                            navigationBottom + systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        homeMain.post(() -> ViewCompat.requestApplyInsets(homeMain));
    }


    private void setupFeatureTiles() {

        configureFeatureTile(
                R.id.cardItinerary,
                R.drawable.ic_home_calendar,
                R.string.pinik_itinerary,
                R.color.pinik_accent_blue
        );

        configureFeatureTile(
                R.id.cardVote,
                R.drawable.ic_home_vote,
                R.string.pinik_vote,
                R.color.pinik_accent_yellow
        );

        configureFeatureTile(
                R.id.cardExpenses,
                R.drawable.ic_home_wallet,
                R.string.pinik_expenses,
                R.color.pinik_accent_orange
        );

        configureFeatureTile(
                R.id.cardTasks,
                R.drawable.ic_home_tasks,
                R.string.pinik_tasks,
                R.color.pinik_accent_green
        );

        configureFeatureTile(
                R.id.cardMap,
                R.drawable.ic_home_map,
                R.string.pinik_map,
                R.color.pinik_accent_green
        );

        configureFeatureTile(
                R.id.cardMembers,
                R.drawable.ic_home_members,
                R.string.pinik_members,
                R.color.pinik_accent_red
        );

        configureFeatureTile(
                R.id.cardPhotos,
                R.drawable.ic_home_photos,
                R.string.pinik_photos,
                R.color.pinik_accent_blue
        );

        configureFeatureTile(
                R.id.cardMore,
                R.drawable.ic_home_more,
                R.string.pinik_more,
                R.color.pinik_accent_yellow
        );
    }

    private void configureFeatureTile(
            int tileId,
            int iconResource,
            int labelResource,
            int colorResource
    ) {

        View tile = findViewById(tileId);
        ImageView icon = tile.findViewById(R.id.imageFeatureIcon);
        TextView label = tile.findViewById(R.id.textFeatureLabel);

        icon.setImageResource(iconResource);
        icon.setImageTintList(
                ColorStateList.valueOf(
                        ContextCompat.getColor(this, colorResource)
                )
        );
        label.setText(labelResource);
        tile.setContentDescription(getString(labelResource));
    }

    private void setupClickListeners() {

        View.OnClickListener openCreateTourListener =
                view -> openCreateTourPage();

        buttonCreateFirstTour.setOnClickListener(openCreateTourListener);
        findViewById(R.id.buttonAddTour).setOnClickListener(openCreateTourListener);

        cardUpcomingTour.setOnClickListener(
                view -> showSelectedTourDetails()
        );

        findViewById(R.id.buttonMenu).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_menu)
        );

        findViewById(R.id.buttonNotification).setOnClickListener(
                view -> handleNotificationClick()
        );

        findViewById(R.id.cardItinerary).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_itinerary)
        );

        findViewById(R.id.cardVote).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_vote)
        );

        findViewById(R.id.cardExpenses).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_expenses)
        );

        findViewById(R.id.cardTasks).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_tasks)
        );

        findViewById(R.id.cardMap).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_map)
        );

        findViewById(R.id.cardMembers).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_members)
        );

        findViewById(R.id.cardPhotos).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_photos)
        );

        findViewById(R.id.cardMore).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_more)
        );

        findViewById(R.id.navHome).setOnClickListener(
                view -> Toast.makeText(
                        HomeActivity.this,
                        R.string.pinik_already_home,
                        Toast.LENGTH_SHORT
                ).show()
        );

        findViewById(R.id.navTours).setOnClickListener(
                view -> homeScrollView.smoothScrollTo(0, 0)
        );

        findViewById(R.id.navChat).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_chat)
        );

        findViewById(R.id.navProfile).setOnClickListener(
                view -> showFeatureMessage(R.string.pinik_profile)
        );
    }

    private void showFeatureMessage(int featureNameResource) {

        Toast.makeText(
                this,
                getString(
                        R.string.pinik_feature_next_message,
                        getString(featureNameResource)
                ),
                Toast.LENGTH_SHORT
        ).show();
    }

    private void openCreateTourPage() {
        startActivity(new Intent(this, CreateTourActivity.class));
    }

    private void updateGreeting(FirebaseUser user) {

        String displayName = user.getDisplayName();
        String name;

        if (displayName != null && !displayName.trim().isEmpty()) {
            name = displayName.trim().split("\\s+")[0];
        } else {
            String email = user.getEmail();

            if (email != null && email.contains("@")) {
                name = email.substring(0, email.indexOf('@'));
            } else {
                textGreeting.setText(R.string.pinik_home_greeting_default);
                return;
            }
        }

        textGreeting.setText(
                getString(R.string.pinik_home_greeting_user, name)
        );
    }

    private void reloadUserName(FirebaseUser user) {

        updateGreeting(user);

        user.reload().addOnSuccessListener(unused -> {
            FirebaseUser refreshedUser = firebaseAuth.getCurrentUser();

            if (refreshedUser != null) {
                updateGreeting(refreshedUser);
            }
        });
    }

    private void startHomeListeners(FirebaseUser user) {

        tourDataLoaded = false;
        activityDataLoaded = false;
        progressHome.setVisibility(View.VISIBLE);

        firestore.collection("tours")
                .whereArrayContains("memberIds", user.getUid())
                .addSnapshotListener(
                        this,
                        (querySnapshot, exception) -> {

                            tourDataLoaded = true;
                            updateLoadingState();

                            if (exception != null || querySnapshot == null) {
                                showNoUpcomingTour();
                                return;
                            }

                            DocumentSnapshot nearestTour = null;
                            long nearestStartTime = Long.MAX_VALUE;
                            long now = System.currentTimeMillis();

                            for (QueryDocumentSnapshot document
                                    : querySnapshot) {

                                Timestamp endTimestamp =
                                        document.getTimestamp("endDate");

                                Timestamp startTimestamp =
                                        document.getTimestamp("startDate");

                                if (endTimestamp == null || startTimestamp == null) {
                                    continue;
                                }

                                long endTime = endTimestamp.toDate().getTime();
                                long startTime = startTimestamp.toDate().getTime();

                                if (endTime >= now && startTime < nearestStartTime) {
                                    nearestStartTime = startTime;
                                    nearestTour = document;
                                }
                            }

                            if (nearestTour == null) {
                                showNoUpcomingTour();
                            } else {
                                showUpcomingTour(nearestTour);
                            }
                        }
                );

        firestore.collection("activities")
                .whereArrayContains("visibleTo", user.getUid())
                .addSnapshotListener(
                        this,
                        (querySnapshot, exception) -> {

                            activityDataLoaded = true;
                            updateLoadingState();

                            if (exception != null || querySnapshot == null) {
                                showRecentActivityError();
                                return;
                            }

                            List<DocumentSnapshot> activities =
                                    new ArrayList<>(querySnapshot.getDocuments());

                            Collections.sort(
                                    activities,
                                    (first, second) -> Long.compare(
                                            getActivityTime(second),
                                            getActivityTime(first)
                                    )
                            );

                            showRecentActivities(activities);
                        }
                );
    }

    private void updateLoadingState() {
        progressHome.setVisibility(
                tourDataLoaded && activityDataLoaded
                        ? View.GONE
                        : View.VISIBLE
        );
    }

    private void showNoUpcomingTour() {

        selectedTourDocument = null;
        cardUpcomingTour.setVisibility(View.GONE);
        layoutNoTour.setVisibility(View.VISIBLE);
    }

    private void showUpcomingTour(DocumentSnapshot document) {

        selectedTourDocument = document;

        String title = document.getString("title");
        String location = document.getString("location");
        Timestamp startTimestamp = document.getTimestamp("startDate");
        Timestamp endTimestamp = document.getTimestamp("endDate");

        if (title == null || title.trim().isEmpty()) {
            title = getString(R.string.pinik_untitled_tour);
        }

        if (location == null || location.trim().isEmpty()) {
            location = getString(R.string.pinik_location_not_added);
        }

        textTourTitle.setText(title);
        textTourLocation.setText(location);

        if (startTimestamp != null && endTimestamp != null) {

            Date startDate = startTimestamp.toDate();
            Date endDate = endTimestamp.toDate();

            textTourDate.setText(
                    getString(
                            R.string.pinik_tour_date_range,
                            dateFormatter.format(startDate),
                            dateFormatter.format(endDate)
                    )
            );

            long difference = Math.max(
                    0L,
                    endDate.getTime() - startDate.getTime()
            );

            int days = (int) TimeUnit.MILLISECONDS.toDays(difference) + 1;
            int nights = Math.max(0, days - 1);

            String dayLabel = getResources().getQuantityString(
                    R.plurals.pinik_day_label,
                    days
            );

            String nightLabel = getResources().getQuantityString(
                    R.plurals.pinik_night_label,
                    nights
            );

            textTourDuration.setText(
                    getString(
                            R.string.pinik_tour_duration,
                            days,
                            dayLabel,
                            nights,
                            nightLabel
                    )
            );
        }

        layoutNoTour.setVisibility(View.GONE);
        cardUpcomingTour.setVisibility(View.VISIBLE);
    }

    private void showSelectedTourDetails() {

        if (selectedTourDocument == null) {
            return;
        }

        String title = selectedTourDocument.getString("title");
        String location = selectedTourDocument.getString("location");
        String description = selectedTourDocument.getString("description");
        Timestamp startTimestamp = selectedTourDocument.getTimestamp("startDate");
        Timestamp endTimestamp = selectedTourDocument.getTimestamp("endDate");

        if (title == null || title.trim().isEmpty()) {
            title = getString(R.string.pinik_untitled_tour);
        }

        if (location == null || location.trim().isEmpty()) {
            location = getString(R.string.pinik_location_not_added);
        }

        if (description == null || description.trim().isEmpty()) {
            description = getString(R.string.pinik_no_description);
        }

        String dateText = getString(R.string.pinik_date_not_added);

        if (startTimestamp != null && endTimestamp != null) {
            dateText = getString(
                    R.string.pinik_tour_date_range,
                    dateFormatter.format(startTimestamp.toDate()),
                    dateFormatter.format(endTimestamp.toDate())
            );
        }

        String details = getString(
                R.string.pinik_tour_details_message,
                location,
                dateText,
                description
        );

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(details)
                .setPositiveButton(R.string.pinik_close, null)
                .show();
    }

    private long getActivityTime(DocumentSnapshot document) {

        Timestamp serverTimestamp = document.getTimestamp("createdAt");

        if (serverTimestamp != null) {
            return serverTimestamp.toDate().getTime();
        }

        Timestamp clientTimestamp = document.getTimestamp("clientCreatedAt");

        if (clientTimestamp != null) {
            return clientTimestamp.toDate().getTime();
        }

        return 0L;
    }

    private void showRecentActivities(List<DocumentSnapshot> activities) {

        recentActivityContainer.removeAllViews();

        if (activities.isEmpty()) {
            showRecentActivityEmpty();
            return;
        }

        int itemCount = Math.min(
                RECENT_ACTIVITY_LIMIT,
                activities.size()
        );

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int index = 0; index < itemCount; index++) {

            DocumentSnapshot document = activities.get(index);

            View itemView = inflater.inflate(
                    R.layout.item_recent_activity,
                    recentActivityContainer,
                    false
            );

            TextView textAvatar =
                    itemView.findViewById(R.id.textActivityAvatar);

            TextView textMessage =
                    itemView.findViewById(R.id.textActivityMessage);

            TextView textTime =
                    itemView.findViewById(R.id.textActivityTime);

            View divider = itemView.findViewById(R.id.activityDivider);

            String actorName = document.getString("actorName");
            String message = document.getString("message");
            String activityType = document.getString("type");

            textAvatar.setText(getInitial(actorName));
            textMessage.setText(
                    message == null || message.trim().isEmpty()
                            ? getString(R.string.pinik_activity_updated)
                            : message
            );

            textTime.setText(
                    formatRelativeTime(getActivityTime(document))
            );

            int avatarColor = getActivityColor(activityType);

            ViewCompat.setBackgroundTintList(
                    textAvatar,
                    ColorStateList.valueOf(avatarColor)
            );

            divider.setVisibility(
                    index == itemCount - 1
                            ? View.GONE
                            : View.VISIBLE
            );

            recentActivityContainer.addView(itemView);
        }

        layoutRecentEmpty.setVisibility(View.GONE);
        recentActivityGlassCard.setVisibility(View.VISIBLE);
    }

    private void showRecentActivityEmpty() {

        textRecentEmptyMessage.setText(
                R.string.pinik_no_recent_activity_description
        );

        recentActivityGlassCard.setVisibility(View.GONE);
        layoutRecentEmpty.setVisibility(View.VISIBLE);
    }

    private void showRecentActivityError() {

        textRecentEmptyMessage.setText(
                R.string.pinik_recent_activity_load_error
        );

        recentActivityGlassCard.setVisibility(View.GONE);
        layoutRecentEmpty.setVisibility(View.VISIBLE);
    }

    private String getInitial(String actorName) {

        if (actorName == null || actorName.trim().isEmpty()) {
            return getString(R.string.pinik_default_avatar_initial);
        }

        return actorName.trim().substring(0, 1).toUpperCase(Locale.getDefault());
    }

    private int getActivityColor(String activityType) {

        int colorResource;

        if (ActivityLogger.TYPE_VOTE_SUBMITTED.equals(activityType)) {
            colorResource = R.color.pinik_activity_vote;

        } else if (ActivityLogger.TYPE_EXPENSE_ADDED.equals(activityType)) {
            colorResource = R.color.pinik_activity_expense;

        } else if (ActivityLogger.TYPE_TASK_COMPLETED.equals(activityType)) {
            colorResource = R.color.pinik_activity_task;

        } else if (ActivityLogger.TYPE_PHOTO_UPLOADED.equals(activityType)) {
            colorResource = R.color.pinik_activity_photo;

        } else {
            colorResource = R.color.pinik_activity_tour;
        }

        return ContextCompat.getColor(this, colorResource);
    }

    private String formatRelativeTime(long activityTime) {

        if (activityTime <= 0L) {
            return getString(R.string.pinik_just_now);
        }

        long difference = Math.max(
                0L,
                System.currentTimeMillis() - activityTime
        );

        long minutes = TimeUnit.MILLISECONDS.toMinutes(difference);

        if (minutes < 1L) {
            return getString(R.string.pinik_just_now);
        }

        if (minutes < 60L) {
            return getResources().getQuantityString(
                    R.plurals.pinik_minutes_ago,
                    (int) minutes,
                    (int) minutes
            );
        }

        long hours = TimeUnit.MILLISECONDS.toHours(difference);

        if (hours < 24L) {
            return getResources().getQuantityString(
                    R.plurals.pinik_hours_ago,
                    (int) hours,
                    (int) hours
            );
        }

        long days = TimeUnit.MILLISECONDS.toDays(difference);

        if (days < 7L) {
            return getResources().getQuantityString(
                    R.plurals.pinik_days_ago,
                    (int) days,
                    (int) days
            );
        }

        return dateFormatter.format(new Date(activityTime));
    }

    /**
     * Listens for notification documents that are visible to the current user.
     * The red dot is shown only when at least one notification is unread.
     *
     * Notification document format:
     * visibleTo: [uid1, uid2, ...]
     * readBy: [uid1, uid2, ...]
     * createdBy: creator uid
     */
    private void startNotificationListener(FirebaseUser user) {

        unreadNotificationDocuments.clear();
        setNotificationDotVisible(false);

        firestore.collection("notifications")
                .whereArrayContains("visibleTo", user.getUid())
                .addSnapshotListener(
                        this,
                        (querySnapshot, exception) -> {

                            unreadNotificationDocuments.clear();

                            if (exception != null || querySnapshot == null) {
                                setNotificationDotVisible(false);
                                return;
                            }

                            for (DocumentSnapshot document
                                    : querySnapshot.getDocuments()) {

                                if (isNotificationUnread(
                                        document,
                                        user.getUid()
                                )) {

                                    unreadNotificationDocuments.add(document);
                                }
                            }

                            setNotificationDotVisible(
                                    !unreadNotificationDocuments.isEmpty()
                            );
                        }
                );
    }

    private boolean isNotificationUnread(
            DocumentSnapshot document,
            String userId
    ) {

        Object readByValue = document.get("readBy");

        if (!(readByValue instanceof List<?>)) {
            return true;
        }

        List<?> readByUsers = (List<?>) readByValue;
        return !readByUsers.contains(userId);
    }

    private void setNotificationDotVisible(boolean visible) {

        if (notificationDot == null) {
            return;
        }

        notificationDot.setVisibility(
                visible ? View.VISIBLE : View.GONE
        );
    }

    /**
     * For the current prototype, tapping the bell marks every unread
     * notification as read. Later this can open NotificationsActivity.
     */
    private void handleNotificationClick() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            openLoginPage();
            return;
        }

        if (unreadNotificationDocuments.isEmpty()) {

            Toast.makeText(
                    this,
                    R.string.pinik_no_new_notifications,
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        WriteBatch batch = firestore.batch();

        for (DocumentSnapshot document
                : unreadNotificationDocuments) {

            batch.update(
                    document.getReference(),
                    "readBy",
                    FieldValue.arrayUnion(currentUser.getUid())
            );
        }

        batch.commit()
                .addOnSuccessListener(unused -> {

                    unreadNotificationDocuments.clear();
                    setNotificationDotVisible(false);

                    Toast.makeText(
                            HomeActivity.this,
                            R.string.pinik_notifications_marked_read,
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(exception ->
                        Toast.makeText(
                                HomeActivity.this,
                                R.string.pinik_notifications_mark_read_failed,
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void openLoginPage() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            openLoginPage();
            return;
        }

        reloadUserName(currentUser);
        startHomeListeners(currentUser);
        startNotificationListener(currentUser);
    }
}
