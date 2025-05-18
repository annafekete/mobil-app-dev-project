package com.example.tensiotrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StatActivity extends AppCompatActivity {

    private TextView textAverage, textMaxSys, textMinPulse;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        textAverage = findViewById(R.id.textAverage);
        textMaxSys = findViewById(R.id.textMaxSys);
        textMinPulse = findViewById(R.id.textMinPulse);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        loadAverageValues();
        loadMaxSysValue();
        loadMinPulseValue();

        findViewById(R.id.buttonBackHome).setOnClickListener(v -> {
            Intent intent = new Intent(StatActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void loadAverageValues() {
        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    int sumSys = 0, sumDia = 0, sumPulse = 0, count = 0;
                    for (QueryDocumentSnapshot doc : query) {
                        try {
                            sumSys += Integer.parseInt(doc.getString("sys"));
                            sumDia += Integer.parseInt(doc.getString("dia"));
                            sumPulse += Integer.parseInt(doc.getString("pulse"));
                            count++;
                        } catch (NumberFormatException | NullPointerException e) {
                            Log.w("StatActivity", "Érvénytelen adat kihagyva", e);
                        }
                    }
                    if (count > 0) {
                        int avgSys = sumSys / count;
                        int avgDia = sumDia / count;
                        int avgPulse = sumPulse / count;
                        textAverage.setText("Átlag - SYS: " + avgSys + ", DIA: " + avgDia + ", PUL: " + avgPulse);
                    } else {
                        textAverage.setText("Nincs adat az átlaghoz.");
                    }
                });
    }

    //legnagyobb sys érték - komplex lekerdezes 2
    private void loadMaxSysValue() {
        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .orderBy("sys", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        textMaxSys.setText("Nincs SYS adat.");
                    } else {
                        for (QueryDocumentSnapshot doc : query) {
                            textMaxSys.setText("Legmagasabb SYS: " + doc.getString("sys"));
                        }
                    }
                });
    }

    //legkisebb pulzus - komplex lekerdezes 3
    private void loadMinPulseValue() {
        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .orderBy("pulse", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        textMaxSys.setText("Nincs PULSE adat.");
                    } else {
                        for (QueryDocumentSnapshot doc : query) {
                            textMaxSys.setText("Legmagasabb PULSE: " + doc.getString("pulse"));
                        }
                    }
                });
    }
}
