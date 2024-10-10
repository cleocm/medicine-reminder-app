package com.example.medicinereminderapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

public class MedicineIntakeAdapter extends FirebaseRecyclerAdapter<TakenMedicine,MedicineIntakeAdapter.medicationHistoryViewHolder> {
    private final List<TakenMedicine> reversedList = new ArrayList<>();
    public MedicineIntakeAdapter(@NonNull FirebaseRecyclerOptions<TakenMedicine> options){
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull medicationHistoryViewHolder medicationHistoryViewHolder, int i, @NonNull TakenMedicine takenMedicine) {
        medicationHistoryViewHolder.medicationIntakeDate.setText(takenMedicine.getIntakeDate());
        medicationHistoryViewHolder.medicationName.setText(takenMedicine.getMedicineName());
        TimeConverter tc = new TimeConverter();
        medicationHistoryViewHolder.medicationTime.setText(tc.convert24HourTo12Hour(takenMedicine.getMedicineTime()));
        if(takenMedicine.getHasTaken()) {
            medicationHistoryViewHolder.hasTaken.setText("Taken");
            medicationHistoryViewHolder.cardView.setCardBackgroundColor(Color.rgb(221,234,206));
        }
        else {
            medicationHistoryViewHolder.hasTaken.setText("Skipped");
            medicationHistoryViewHolder.cardView.setCardBackgroundColor(Color.rgb(244,210,210));
        }
    }

    @NonNull
    @Override
    public medicationHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row,parent,false);
        return new medicationHistoryViewHolder(view);
    }

    class medicationHistoryViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView medicationIntakeDate, medicationName, medicationTime, hasTaken;

        public medicationHistoryViewHolder(View itemView){
            super(itemView);
            cardView=itemView.findViewById(R.id.cardview);
            medicationIntakeDate=itemView.findViewById(R.id.tv_medicine_intake_date);
            medicationName=itemView.findViewById(R.id.tv_medicine_name_history);
            medicationTime=itemView.findViewById(R.id.tv_med_time_history);
            hasTaken=itemView.findViewById(R.id.tv_has_taken);
        }
    }
}
