package com.example.scandal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * Activity for browsing events.
 */
public class BrowseEventActivity extends AppCompatActivity {
    /**
     * FrameLayout for navigating back to the previous page.
     */
    FrameLayout buttonBack_BrowseEventsPage;
    /**
     * ListView for displaying events.
     */
    ListView eventsList;
    /**
     * Firebase Firestore instance for database operations.
     */
    FirebaseFirestore db;
    /**
     * Provides functionality for event list
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_events_page);
        // inits UI stuff
        buttonBack_BrowseEventsPage = findViewById(R.id.buttonBack_BrowseEventsPage);
        db = FirebaseFirestore.getInstance(); // gets DB
        eventsList = findViewById(R.id.listView_BrowseEventPage);
        //back button goes to home page
        buttonBack_BrowseEventsPage.setOnClickListener(v->{
            Intent intent = new Intent(BrowseEventActivity.this, HomeActivity.class);
            startActivity(intent);
        });
        // if item is clicked go to the events page
        eventsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eventName = (String) parent.getItemAtPosition(position); // gets the name
                Intent intent = new Intent(BrowseEventActivity.this, EventDetailsActivity.class);
                intent.putExtra("eventName", eventName); //passes the name through an intent
                startActivity(intent); // goes to event page
            }
        });
        loadEvents(); // loads the events
    }
    /**
     * Retrieves and displays event pulled from firebase
     */
    private void loadEvents() {
        List<String> eventNames = new ArrayList<>(); // makes list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventNames);
        eventsList.setAdapter(adapter); // sets the adapter

        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) { // loops through all the events
                    String eventName = document.getString("name");  // gets the name
                    if (eventName != null) { // checks if the name is null
                        eventNames.add(eventName); //  adds to list if not null
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh the list view with the new data
            } else {
                // Handle errors here
            }
        });
    }
}
