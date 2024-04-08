package com.example.scandal;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * Activity for managing images in the admin panel.
 */
public class AdminImageActivity extends AppCompatActivity {
    /** Button to navigate back to homepage. */
    FrameLayout backToHomepage;
    /** ListView to display images. */
    ListView listView;
    /** Firestore instance for database operations. */
    FirebaseFirestore db;
    /** Custom adapter to handle Bitmaps. */
    CustomImageAdapter adapter; // Custom adapter to handle Bitmaps
    /** List to hold loaded images. */
    List<Bitmap> imagesList = new ArrayList<>();
    /** Store image document IDs for deletion. */
    List<String> imageIds = new ArrayList<>(); // Store image document IDs for deletion

    /**
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_list_page);

        listView = findViewById(R.id.listView_ImageListPage);
        backToHomepage = findViewById(R.id.buttonBack_ImageListPage);

        db = FirebaseFirestore.getInstance();
        adapter = new CustomImageAdapter(this, imagesList); // Initialize the custom adapter
        listView.setAdapter(adapter); // Set the adapter to the ListView

        loadImages();

        backToHomepage.setOnClickListener(v -> {
            Intent intent = new Intent(AdminImageActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        // Set click listener for the ListView items
        listView.setOnItemClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminImageActivity.this);
            builder.setMessage("Are you sure you want to delete this image?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the image from Firebase
                        deleteImage(imageIds.get(position));
                        // Remove the image from the list and notify the adapter
                        imagesList.remove(position);
                        imageIds.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });
    }
    /**
     * Load images from Firestore.
     * Fetches images and their document IDs from Firestore, and updates the adapter.
     */
    private void loadImages() {
        // Load images from 'events' collection
        db.collection("events").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                imagesList.clear();
                imageIds.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String imageString = document.getString("posterImage");
                    if (imageString != null && !imageString.isEmpty()) {
                        Bitmap bitmap = convertImageStringToBitmap(imageString);
                        if (bitmap != null) {
                            imagesList.add(bitmap);
                            imageIds.add(document.getId());
                        }
                    }
                }
                // Now, load images from 'profiles' collection
                db.collection("profiles").get().addOnCompleteListener(profileTask -> {
                    if (profileTask.isSuccessful()) {
                        for (QueryDocumentSnapshot profileDocument : profileTask.getResult()) {
                            String profileImageString = profileDocument.getString("imageString");
                            if (profileImageString != null && !profileImageString.isEmpty()) {
                                Bitmap bitmap = convertImageStringToBitmap(profileImageString);
                                if (bitmap != null) {
                                    imagesList.add(bitmap);
                                    // Assuming profile document IDs are also needed
                                    imageIds.add(profileDocument.getId());
                                }
                            }
                        }
                        adapter.notifyDataSetChanged(); // Notify adapter after loading from both collections
                    }
                });
            } else {
                // Handle the error for 'events' collection loading failure
            }
        });
    }


    /**
     * Delete an image from Firestore.
     * Deletes the image with the given document ID from Firestore,
     * and removes it from the list and adapter upon success.
     *
     * @param imageId  The document ID of the image to delete.
     */
    private void deleteImage(String imageId) {
        // Attempt to clear the image in the 'events' collection
        db.collection("events").document(imageId)
                .update("posterImage", "")
                .addOnSuccessListener(aVoid -> Log.d("AdminImageActivity", "Image potentially cleared from events"))
                .addOnFailureListener(e -> Log.d("AdminImageActivity", "Image not found or error clearing from events: " + e.getMessage()));

        // Attempt to clear the image in the 'profiles' collection
        db.collection("profiles").document(imageId)
                .update("imageString", "")
                .addOnSuccessListener(aVoid -> Log.d("AdminImageActivity", "Image potentially cleared from profiles"))
                .addOnFailureListener(e -> Log.d("AdminImageActivity", "Image not found or error clearing from profiles: " + e.getMessage()));
    }

    /**
     * Convert a Base64-encoded image string to a Bitmap.
     *
     * @param imageString The Base64-encoded image string.
     * @return The decoded Bitmap, or null if decoding fails.
     */
    private Bitmap convertImageStringToBitmap(String imageString) {
        try {
            byte[] decodedString = Base64.decode(imageString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

