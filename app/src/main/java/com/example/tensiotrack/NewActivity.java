package com.example.tensiotrack;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;


import androidx.appcompat.app.AppCompatActivity;

public class NewActivity extends AppCompatActivity {

    private EditText inputSys, inputDia, inputPulse, inputNote;
    private Spinner spinnerTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        // Inicializálás
        inputSys = findViewById(R.id.inputSys);
        inputDia = findViewById(R.id.inputDia);
        inputPulse = findViewById(R.id.inputPulse);
        inputNote = findViewById(R.id.inputNote);
        spinnerTags = findViewById(R.id.spinnerTags);

        // Spinner beállítása
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.tag_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTags.setAdapter(adapter);

        // Gombok
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);

        buttonSave.setOnClickListener(v -> saveEntry());
        buttonCancel.setOnClickListener(v -> finish());

        EditText buttonDate = findViewById(R.id.inputDate);
        EditText buttonNow = findViewById(R.id.inputNow);

        // Dátumválasztó
        buttonDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(NewActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedYear + "." + String.format("%02d", selectedMonth + 1) + "." + String.format("%02d", selectedDay);
                        buttonDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Időválasztó
        buttonNow.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(NewActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                        buttonNow.setText(time);
                    }, hour, minute, true);
            timePickerDialog.show();
        });
    }

    private void saveEntry() {
        String sys = inputSys.getText().toString();
        String dia = inputDia.getText().toString();
        String pulse = inputPulse.getText().toString();
        String note = inputNote.getText().toString();
        String tag = spinnerTags.getSelectedItem().toString();

        // Firestore objektum
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, "Bejelentkezés szükséges a mentéshez!", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText inputDate = findViewById(R.id.inputDate);
        EditText inputNow = findViewById(R.id.inputNow);

        Map<String, Object> entry = new HashMap<>();
        entry.put("sys", sys);
        entry.put("dia", dia);
        entry.put("pulse", pulse);
        entry.put("note", note);
        entry.put("tag", tag);
        entry.put("date", inputDate.getText().toString());   // dátum mező mentése
        entry.put("time", inputNow.getText().toString());     // idő mező mentése
        entry.put("timestamp", System.currentTimeMillis());   // Firestore rendezéshez
        entry.put("uid", user.getUid());

        db.collection("measurements")
                .add(entry)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Sikeresen mentve!", Toast.LENGTH_SHORT).show();
                    finish(); // vissza a HomeActivity-re
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt mentés közben: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}
