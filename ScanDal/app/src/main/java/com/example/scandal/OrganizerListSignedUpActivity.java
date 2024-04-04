package com.example.scandal;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for displaying the list of user who signed up for an event
 */
public class OrganizerListSignedUpActivity extends AppCompatActivity {
    /**
     * FrameLayout for navigating back to the main page.
     */
    FrameLayout backMain;
    /**
     * ListView for displaying signed up users.
     */
    ListView userList;
    /**
     * Firebase Firestore instance for database operations.
     */
    FirebaseFirestore db;
    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState If the activity is being re-initialized after being previously shut down, this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events_page); // Ensure this is the correct layout
        TextView txtMyEvents = findViewById(R.id.txtMyEvents);
        txtMyEvents.setText("SignedUp Attendees");

        backMain = findViewById(R.id.buttonBack_MyEventsPage);
        userList = findViewById(R.id.listView_MyEventsPage);
        db = FirebaseFirestore.getInstance();

        backMain.setOnClickListener(v -> finish());

        // Retrieve the event name from the intent
        String eventName = getIntent().getStringExtra("eventName");

        loadUsers(eventName); // Pass the eventName to the method
    }

    /**
     * Retrieves and displays users signed up for the specified event.
     */
    private void loadUsers(String eventName) {
        List<String> userNames = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        userList.setAdapter(adapter);

        db.collection("events")
                .whereEqualTo("name", eventName) // Filter by the event name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Map<String, Object> eventData = documentSnapshot.getData();
                        if (eventData.containsKey("signedUp")) {
                            Map<String, Object> signedUpUsers = (Map<String, Object>) eventData.get("signedUp");
                            for (Map.Entry<String, Object> entry : signedUpUsers.entrySet()) {
                                // Assuming the value is the user's name. Adjust as necessary.
                                String userName = (String) entry.getValue();
                                if (userName != null) {
                                    userNames.add(userName);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                });
    }
}
