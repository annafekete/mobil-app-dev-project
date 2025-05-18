package com.example.tensiotrack;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ImageButton buttonReminder, buttonProfile;
    private FloatingActionButton buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Gombok összekapcsolása
        buttonReminder = findViewById(R.id.buttonReminder);
        buttonProfile = findViewById(R.id.buttonProfile);
        buttonAdd = findViewById(R.id.buttonAdd);

        // + gomb - Új érték hozzáadása
        buttonAdd.setOnClickListener(view -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.isAnonymous()) {
                Toast.makeText(HomeActivity.this, "Vendégként nem adhatsz hozzá új értéket. Kérlek jelentkezz be!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(HomeActivity.this, NewActivity.class);
                startActivity(intent);
            }
        });


        // Emlékeztetők
        buttonReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ReminderActivity.class);
                startActivity(intent);
            }
        });

        // Profil
        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        Button buttonStats = findViewById(R.id.buttonStats);
        buttonStats.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StatActivity.class);
            startActivity(intent);
        });

        loadLatestEntry();
    }

    //utolso ertek megjelenitese
    private void loadLatestEntry() {
        TextView textLatestEntry = findViewById(R.id.textLatestEntry);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.isAnonymous()) {
            textLatestEntry.setText("Csak bejelentkezett felhasználók láthatják az adatokat.");
            return;
        }

        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())  // 🛡️ Csak saját adatokat kérünk le
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String sys = doc.getString("sys");
                        String dia = doc.getString("dia");
                        String pulse = doc.getString("pulse");

                        String result = "Utolsó érték:\nSYS: " + sys + " | DIA: " + dia + " | PUL: " + pulse;
                        textLatestEntry.setText(result);
                    }
                })
                .addOnFailureListener(e -> {
                    textLatestEntry.setText("Nem sikerült lekérni az adatot.");
                    Log.e("Firestore", "Lekérdezés hiba:", e);
                });
    }

    //atlagszamitas - firebase komplex lekerdezes 1
    private void loadAverages() {
        TextView textAverage = findViewById(R.id.textAverage);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        db.collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int sumSys = 0, sumDia = 0, sumPulse = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            int sys = Integer.parseInt(doc.getString("sys"));
                            int dia = Integer.parseInt(doc.getString("dia"));
                            int pulse = Integer.parseInt(doc.getString("pulse"));

                            sumSys += sys;
                            sumDia += dia;
                            sumPulse += pulse;
                            count++;
                        } catch (Exception e) {
                            // Hibás vagy hiányzó érték
                        }
                    }

                    if (count > 0) {
                        int avgSys = sumSys / count;
                        int avgDia = sumDia / count;
                        int avgPulse = sumPulse / count;

                        String avgText = "Átlag (utolsó 30): SYS: " + avgSys + ", DIA: " + avgDia + ", PUL: " + avgPulse;
                        textAverage.setText(avgText);
                    } else {
                        textAverage.setText("Nincs elérhető mérés.");
                    }
                })
                .addOnFailureListener(e -> {
                    textAverage.setText("Hiba az adatok lekérésekor.");
                });
    }

    private void loadCharts() {
        LineChart chartSys = findViewById(R.id.chartSys);
        LineChart chartDia = findViewById(R.id.chartDia);
        TextView tipCombined = findViewById(R.id.tipCombined);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("measurements")
                .whereEqualTo("uid", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Entry> sysEntries = new ArrayList<>();
                    List<Entry> diaEntries = new ArrayList<>();

                    float index = 0;
                    String latestSys = "0";
                    String latestDia = "0";

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String sys = doc.getString("sys");
                        String dia = doc.getString("dia");
                        try {
                            sysEntries.add(new Entry(index, Float.parseFloat(sys)));
                            diaEntries.add(new Entry(index, Float.parseFloat(dia)));
                            if (index == 0) {
                                latestSys = sys;
                                latestDia = dia;
                            }
                            index++;
                        } catch (Exception ignored) {}
                    }

                    LineDataSet sysDataSet = new LineDataSet(sysEntries, "SYS");
                    LineDataSet diaDataSet = new LineDataSet(diaEntries, "DIA");

                    chartSys.setData(new LineData(sysDataSet));
                    chartDia.setData(new LineData(diaDataSet));
                    chartSys.invalidate();
                    chartDia.invalidate();

                    String tipText = getTip("SYS", latestSys) + "\n" + getTip("DIA", latestDia);
                    tipCombined.setText(tipText);
                });
    }

    private String getTip(String type, String valueStr) {
        try {
            int value = Integer.parseInt(valueStr);
            if (type.equals("SYS")) {
                if (value < 90) return "SYS túl alacsony – Igyál több vizet, kerüld az alkoholt.";
                else if (value > 140) return "SYS túl magas – Kerüld a kávét, pihenj, és mérj újra később.";
                else return "SYS normális – Csak így tovább!";
            } else {
                if (value < 60) return "DIA túl alacsony – Igyál egy kis vizet, és feküdj le pár percre.";
                else if (value > 90) return "DIA túl magas – Kerüld a sós ételeket, relaxálj.";
                else return "DIA normális – Jó úton jársz!";
            }
        } catch (Exception e) {
            return "";
        }
    }



    //Lifecycle hook 1
    @Override
    protected void onResume() {
        super.onResume();
        loadLatestEntry();
        loadAverages();
        loadCharts();
        Animation fadeInScale = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        MaterialCardView cardMain = findViewById(R.id.cardMain);
        MaterialCardView cardSys = findViewById(R.id.cardSys);
        MaterialCardView cardDia = findViewById(R.id.cardDia);
        MaterialCardView cardTip = findViewById(R.id.cardTip);
        MaterialCardView cardStat = findViewById(R.id.cardStat);
        cardMain.startAnimation(fadeInScale);
        cardSys.startAnimation(fadeInScale);
        cardDia.startAnimation(fadeInScale);
        cardTip.startAnimation(fadeInScale);
        cardStat.startAnimation(fadeInScale);
    }
}
