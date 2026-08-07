package com.tourbox.pinik;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores user actions that should appear in the Home page's Recent Activity section.
 */
public final class ActivityLogger {

    public static final String TYPE_TOUR_CREATED = "TOUR_CREATED";
    public static final String TYPE_VOTE_SUBMITTED = "VOTE_SUBMITTED";
    public static final String TYPE_EXPENSE_ADDED = "EXPENSE_ADDED";
    public static final String TYPE_TASK_COMPLETED = "TASK_COMPLETED";
    public static final String TYPE_PHOTO_UPLOADED = "PHOTO_UPLOADED";

    private static final String TAG = "ActivityLogger";

    private ActivityLogger() {
        // Utility class.
    }

    public static Map<String, Object> createActivityData(
            FirebaseUser user,
            String tourId,
            String groupId,
            String activityType,
            String message,
            List<String> visibleTo
    ) {

        Map<String, Object> activityData = new HashMap<>();

        activityData.put("actorId", user.getUid());
        activityData.put("actorName", getActorName(user));
        activityData.put("type", activityType);
        activityData.put("message", message);
        activityData.put("visibleTo", new ArrayList<>(visibleTo));
        activityData.put("createdAt", FieldValue.serverTimestamp());
        activityData.put("clientCreatedAt", Timestamp.now());

        if (tourId != null && !tourId.trim().isEmpty()) {
            activityData.put("tourId", tourId);
        }

        if (groupId != null && !groupId.trim().isEmpty()) {
            activityData.put("groupId", groupId);
        }

        return activityData;
    }

    /**
     * Call this only after the original operation succeeds.
     */
    public static void logActivity(
            String tourId,
            String groupId,
            String activityType,
            String message,
            List<String> visibleTo
    ) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || visibleTo == null || visibleTo.isEmpty()) {
            return;
        }

        Map<String, Object> data = createActivityData(
                user,
                tourId,
                groupId,
                activityType,
                message,
                visibleTo
        );

        FirebaseFirestore.getInstance()
                .collection("activities")
                .add(data)
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Could not save recent activity.", exception)
                );
    }

    public static String getActorName(FirebaseUser user) {

        String displayName = user.getDisplayName();

        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }

        String email = user.getEmail();

        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }

        return "Pinik member";
    }
}
