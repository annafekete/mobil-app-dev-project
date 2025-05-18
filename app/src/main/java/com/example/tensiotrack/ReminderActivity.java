package com.example.tensiotrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class ReminderActivity extends AppCompatActivity {

    private Switch switchEnableReminder;
    private LinearLayout timeLayout;
    private CheckBox checkMonday, checkTuesday, checkWednesday, checkThursday, checkFriday, checkSaturday, checkSunday;
    private Button buttonAddTime, buttonSaveReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        // Inicializálás
        switchEnableReminder = findViewById(R.id.switchEnableReminder);
        timeLayout = findViewById(R.id.timeLayout);
        checkMonday = findViewById(R.id.checkMonday);
        checkTuesday = findViewById(R.id.checkTuesday);
        checkWednesday = findViewById(R.id.checkWednesday);
        checkThursday = findViewById(R.id.checkThursday);
        checkFriday = findViewById(R.id.checkFriday);
        checkSaturday = findViewById(R.id.checkSaturday);
        checkSunday = findViewById(R.id.checkSunday);

        buttonAddTime = findViewById(R.id.buttonAddTime);
        buttonSaveReminder = findViewById(R.id.buttonSaveReminder);

        // Új időpont hozzáadása
        buttonAddTime.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog timePicker = new TimePickerDialog(ReminderActivity.this, (view, selectedHour, selectedMinute) -> {
                TextView newTime = new TextView(ReminderActivity.this);
                newTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
                newTime.setPadding(8, 8, 8, 8);
                timeLayout.addView(newTime);
            }, hour, minute, true);

            timePicker.show();
        });

        // Emlékeztetők mentése
        buttonSaveReminder.setOnClickListener(v -> {
            if (switchEnableReminder.isChecked()) {
                scheduleAllReminders();
                Toast.makeText(this, "Emlékeztetők beállítva!", Toast.LENGTH_SHORT).show();
            } else {
                cancelAllReminders();
                Toast.makeText(this, "Emlékeztetők kikapcsolva.", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.buttonBackHome).setOnClickListener(v -> {
            Intent intent = new Intent(ReminderActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void scheduleAllReminders() {
        List<Integer> selectedDays = new ArrayList<>();
        if (checkMonday.isChecked()) selectedDays.add(Calendar.MONDAY);
        if (checkTuesday.isChecked()) selectedDays.add(Calendar.TUESDAY);
        if (checkWednesday.isChecked()) selectedDays.add(Calendar.WEDNESDAY);
        if (checkThursday.isChecked()) selectedDays.add(Calendar.THURSDAY);
        if (checkFriday.isChecked()) selectedDays.add(Calendar.FRIDAY);
        if (checkSaturday.isChecked()) selectedDays.add(Calendar.SATURDAY);
        if (checkSunday.isChecked()) selectedDays.add(Calendar.SUNDAY);

        for (int i = 0; i < timeLayout.getChildCount(); i++) {
            String[] parts = ((TextView) timeLayout.getChildAt(i)).getText().toString().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            for (int day : selectedDays) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1); // ha a beállított idő már elmúlt
                }

                scheduleReminder(calendar);
            }
        }
    }

    private void scheduleReminder(Calendar cal) {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                cal.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    private void cancelAllReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        for (int i = 0; i < timeLayout.getChildCount(); i++) {
            String[] parts = ((TextView) timeLayout.getChildAt(i)).getText().toString().split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                Intent intent = new Intent(this, ReminderReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        cal.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                alarmManager.cancel(pendingIntent);
            }
        }
    }
}
