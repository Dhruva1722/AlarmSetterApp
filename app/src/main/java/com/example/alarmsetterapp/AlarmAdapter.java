package com.example.alarmsetterapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {

    private Context context;
    private ArrayList<MainActivity.AlarmItem> alarmList;
    private OnAlarmItemInteractionListener listener;

    public interface OnAlarmItemInteractionListener {
        void onDeleteClicked(MainActivity.AlarmItem alarmItem);
        void onEditClicked(MainActivity.AlarmItem alarmItem);
    }

    public AlarmAdapter(Context context, ArrayList<MainActivity.AlarmItem> alarmList, OnAlarmItemInteractionListener listener) {
        this.context = context;
        this.alarmList = alarmList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.alarm_list_ui, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        MainActivity.AlarmItem alarmItem = alarmList.get(position);
        holder.alarmText.setText(alarmItem.getAlarmText());
        holder.labelText.setText(alarmItem.getLabel());

        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(alarmItem));
        holder.editButton.setOnClickListener(v -> listener.onEditClicked(alarmItem));
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView alarmText;
        TextView labelText;
        ImageView deleteButton;
        ImageView editButton;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            alarmText = itemView.findViewById(R.id.alarmText);
            labelText = itemView.findViewById(R.id.labelText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}