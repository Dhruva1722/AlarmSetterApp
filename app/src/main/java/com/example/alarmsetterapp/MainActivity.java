package com.example.alarmsetterapp;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AlarmAdapter.OnAlarmItemInteractionListener {

    private static final int REQUEST_CODE_NOTIFICATION = 1;
    private static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 2;

    private TimePicker timePicker;
    private TextInputLayout alarmLabel;
    private Button setAlarmButton;
    private RecyclerView alarmRecyclerView;
    private AlarmManager alarmManager;
    private SharedPreferences sharedPreferences;
    private AlarmAdapter adapter;
    private ArrayList<AlarmItem> alarmList;

    private static final String PREFS_NAME = "AlarmPrefs";
    private static final String ALARMS_KEY = "alarms";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        alarmLabel = findViewById(R.id.alarmText);
        setAlarmButton = findViewById(R.id.setAlarmButton);
        alarmRecyclerView = findViewById(R.id.alarmListView);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        alarmList = new ArrayList<>();
        loadAlarms();

        adapter = new AlarmAdapter(this, alarmList, this);
        alarmRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        alarmRecyclerView.setAdapter(adapter);

        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNotificationPermission();
            }
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
            } else {
                checkExactAlarmPermission();
            }
        } else {
            checkExactAlarmPermission();
        }
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_SCHEDULE_EXACT_ALARM);
            } else {
                setAlarm();
            }
        } else {
            setAlarm();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkExactAlarmPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setAlarm();
                }
            }
        }
    }

    private void setAlarm() {
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String label = alarmLabel.getEditText().getText().toString();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        int requestCode = (int) System.currentTimeMillis();
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("label", label);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        saveAlarm(requestCode, hour, minute, label);

        String alarmText = String.format("%02d:%02d", hour, minute);
        AlarmItem alarmItem = new AlarmItem(requestCode, hour, minute, label, alarmText);
        alarmList.add(alarmItem);
        adapter.notifyDataSetChanged();
    }

    private void saveAlarm(int requestCode, int hour, int minute, String label) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> alarmSet = sharedPreferences.getStringSet(ALARMS_KEY, new HashSet<>());
        alarmSet.add(requestCode + "," + hour + "," + minute + "," + label);
        editor.putStringSet(ALARMS_KEY, alarmSet);
        editor.apply();
    }

    private void loadAlarms() {
        Set<String> alarmSet = sharedPreferences.getStringSet(ALARMS_KEY, new HashSet<>());
        for (String alarmString : alarmSet) {
            String[] alarmParts = alarmString.split(",");
            int requestCode = Integer.parseInt(alarmParts[0]);
            int hour = Integer.parseInt(alarmParts[1]);
            int minute = Integer.parseInt(alarmParts[2]);
            String label = alarmParts[3];
            String alarmText = String.format("%02d:%02d", hour, minute);
            AlarmItem alarmItem = new AlarmItem(requestCode, hour, minute, label, alarmText);
            alarmList.add(alarmItem);
        }
    }

    private void deleteAlarm(int requestCode) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> alarmSet = sharedPreferences.getStringSet(ALARMS_KEY, new HashSet<>());
        Set<String> newAlarmSet = new HashSet<>(alarmSet);
        for (String alarmString : alarmSet) {
            String[] alarmParts = alarmString.split(",");
            int savedRequestCode = Integer.parseInt(alarmParts[0]);
            if (savedRequestCode == requestCode) {
                newAlarmSet.remove(alarmString);
                break;
            }
        }
        editor.putStringSet(ALARMS_KEY, newAlarmSet);
        editor.apply();

        // Remove the alarm from the list and update the adapter
        for (int i = 0; i < alarmList.size(); i++) {
            if (alarmList.get(i).getRequestCode() == requestCode) {
                alarmList.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void editAlarm(AlarmItem alarmItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_alarm, null);
        builder.setView(dialogView);

        TimePicker editTimePicker = dialogView.findViewById(R.id.editTimePicker);
        TextInputEditText editLabel = dialogView.findViewById(R.id.editLabel);

        editTimePicker.setHour(alarmItem.getHour());
        editTimePicker.setMinute(alarmItem.getMinute());
        editLabel.setText(alarmItem.getLabel());

        builder.setPositiveButton("Save", (dialog, which) -> {
            int newHour = editTimePicker.getHour();
            int newMinute = editTimePicker.getMinute();
            String newLabel = editLabel.getText().toString();

            deleteAlarm(alarmItem.getRequestCode());

            int requestCode = (int) System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, newHour);
            calendar.set(Calendar.MINUTE, newMinute);
            calendar.set(Calendar.SECOND, 0);

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("label", newLabel);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            saveAlarm(requestCode, newHour, newMinute, newLabel);

            String alarmText = String.format("%02d:%02d", newHour, newMinute);
            AlarmItem newAlarmItem = new AlarmItem(requestCode, newHour, newMinute, newLabel, alarmText);
            alarmList.add(newAlarmItem);
            adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onDeleteClicked(AlarmItem alarmItem) {
        deleteAlarm(alarmItem.getRequestCode());
    }

    @Override
    public void onEditClicked(AlarmItem alarmItem) {
        editAlarm(alarmItem);
    }

    // Data class for alarm items
    public static class AlarmItem {
        private int requestCode;
        private int hour;
        private int minute;
        private String label;
        private String alarmText;

        public AlarmItem(int requestCode, int hour, int minute, String label, String alarmText) {
            this.requestCode = requestCode;
            this.hour = hour;
            this.minute = minute;
            this.label = label;
            this.alarmText = alarmText;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public int getHour() {
            return hour;
        }

        public int getMinute() {
            return minute;
        }

        public String getLabel() {
            return label;
        }

        public String getAlarmText() {
            return alarmText;
        }
    }
}