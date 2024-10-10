package com.example.medicinereminderapp;

import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.squareup.picasso.Picasso;


import java.util.regex.Pattern;


public class MedicineAdapter extends FirebaseRecyclerAdapter<Medicine,MedicineAdapter.medicineViewHolder> {
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MedicineAdapter(@NonNull FirebaseRecyclerOptions<Medicine> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull medicineViewHolder holder, int position, @NonNull Medicine model) {
        holder.medName.setText(model.getMedicineName());
        holder.medDose.setText(model.getMedicineDosage());
        holder.medDesc.setText(model.getMedicineDescription());
        TimeConverter tc = new TimeConverter();
        holder.medTime.setText(tc.convert24HourTo12Hour(model.getMedicineTime()));
        try {
            Picasso.get()
                    .load(model.getMedicineImage())
                    .placeholder(R.drawable.baseline_block_flipped_24)
                    .into(holder.medImg);
        } catch (Exception e) {
            // If an exception occurs (e.g., if model.getMedicineImage() returns null),
            // set the placeholder image
            holder.medImg.setImageResource(R.drawable.baseline_block_flipped_24);
        }

    }

    @NonNull
    @Override
    public medicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.medicine_row,parent,false);
        return new medicineViewHolder(view);
    }

    class medicineViewHolder extends RecyclerView.ViewHolder {
        ImageView medImg;
        TextView medTime, medName, medDose, medDesc;

        public medicineViewHolder(View itemView) {
            super(itemView);

            medImg = itemView.findViewById(R.id.iv_medImage_cv);
            medTime = itemView.findViewById(R.id.tv_time_cv);
            medName = itemView.findViewById(R.id.tv_medName_cv);
            medDesc = itemView.findViewById(R.id.tv_medDesc_cv);
            medDose = itemView.findViewById(R.id.tv_medDose_cv);
        }

    }

}


