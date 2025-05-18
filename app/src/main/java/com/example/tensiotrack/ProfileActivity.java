package com.example.tensiotrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private EditText editName, editEmail, editBirthDate, editWeight, editHeight;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private LinearLayout measurementList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editBirthDate = findViewById(R.id.editBirthDate);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        measurementList = findViewById(R.id.measurementList);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadUserData();
        loadMeasurements();

        findViewById(R.id.buttonUpdateProfile).setOnClickListener(v -> updateProfile());

        findViewById(R.id.buttonBackHome).setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        if (user == null) return;
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    editName.setText(doc.getString("name"));
                    editEmail.setText(user.getEmail());
                    editBirthDate.setText(doc.getString("birthdate"));
                    editWeight.setText(String.valueOf(doc.getLong("weight")));
                    editHeight.setText(String.valueOf(doc.getLong("height")));
                });
    }

    private void updateProfile() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", editName.getText().toString());
        updates.put("birthdate", editBirthDate.getText().toString());
        updates.put("weight", Integer.parseInt(editWeight.getText().toString()));
        updates.put("height", Integer.parseInt(editHeight.getText().toString()));

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(v -> Toast.makeText(this, "Sikeres frissítés!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadMeasurements() {
        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    measurementList.removeAllViews();
                    for (QueryDocumentSnapshot doc : query) {
                        String id = doc.getId();
                        String sys = doc.getString("sys");
                        String dia = doc.getString("dia");
                        String pulse = doc.getString("pulse");

                        TextView entry = new TextView(this);
                        entry.setText("SYS: " + sys + ", DIA: " + dia + ", PUL: " + pulse);

                        Button deleteButton = new Button(this);
                        deleteButton.setText("Törlés");
                        deleteButton.setOnClickListener(v -> {
                            db.collection("measurements").document(id).delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Törölve!", Toast.LENGTH_SHORT).show();
                                        loadMeasurements(); // frissítés
                                    });
                        });

                        LinearLayout container = new LinearLayout(this);
                        container.setOrientation(LinearLayout.VERTICAL);
                        container.addView(entry);
                        container.addView(deleteButton);

                        measurementList.addView(container);
                    }
                });
    }
}

