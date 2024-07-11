# Alarm Setter App

An Android application to set multiple alarms with labels. This app allows users to add, edit, and delete alarms, and stores alarm data using SharedPreferences. It utilizes RecyclerView for displaying alarms and ensures compatibility with Android API level 31 and above.

## Features
- Set multiple alarms with labels
- Edit existing alarms
- Delete alarms
- Alarms stored using SharedPreferences
- RecyclerView for displaying alarms

## Permissions
- `POST_NOTIFICATIONS`
- `WAKE_LOCK`
- `VIBRATE`
- `SCHEDULE_EXACT_ALARM`

## Installation
1. Clone the repository:
   git clone https://github.com/Dhruva1722/alarm-setter-app.git
2. Open the project in Android Studio.
3. Build and run the application on an Android device or emulator.

## Usage
1. Set an alarm using the TimePicker and add a label.
2. Click "Set Alarm" to save the alarm.
3. Edit or delete alarms from the list displayed.
   
## Code Overview

## MainActivity.java
The main activity handles the setting, editing, and deleting of alarms. It uses RecyclerView to display a list of alarms and SharedPreferences to store alarm data.

## AlarmAdapter.java
Adapter class for the RecyclerView that displays the list of alarms. It handles the binding of alarm data to the view and user interactions such as edit and delete.

## AlarmReceiver.java
BroadcastReceiver class that handles the alarm event and shows a notification.

## AlarmItem.java
Data class for representing an alarm item.
