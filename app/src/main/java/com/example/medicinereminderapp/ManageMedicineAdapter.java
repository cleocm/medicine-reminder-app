package com.example.medicinereminderapp;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ManageMedicineAdapter extends FirebaseRecyclerAdapter<Medicine,ManageMedicineAdapter.manageMedicineViewHolder> {

    private static final Pattern TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3])[0-5]\\d$");
    private final String selectedElderlyId;

    private boolean isValidTime(String time) {
        return !TextUtils.isEmpty(time) && TIME_PATTERN.matcher(time).matches();
    }

    private boolean isValidInput(EditText et){
        Boolean valid=true;
        if (et.getText().toString().trim().isEmpty()){
            valid = false;
        }
        return valid;
    }
    public ManageMedicineAdapter(@NonNull FirebaseRecyclerOptions<Medicine> options, String selectedElderlyId) {
        super(options);
        this.selectedElderlyId = selectedElderlyId;
    }

    @Override
    protected void onBindViewHolder(@NonNull manageMedicineViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull Medicine model) {
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

        holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogPlus dialogPlus = DialogPlus.newDialog(holder.medImg.getContext())
                        .setContentHolder(new ViewHolder(R.layout.update_popup))
                        .setExpanded(true,1580)
                        .setGravity(Gravity.BOTTOM)
                        .setCancelable(true)
                        .create();

                View view = dialogPlus.getHolderView();

                EditText medName = view.findViewById(R.id.et_name_update);
                EditText medDose = view.findViewById(R.id.et_dose_update);
                EditText medDesc = view.findViewById(R.id.et_desc_update);
                EditText medImage = view.findViewById(R.id.et_imagelink_update);
                EditText medTime = view.findViewById(R.id.et_time_update);
                Button confirm = view.findViewById(R.id.btn_confirm_update);

                medName.setText(model.getMedicineName());
                medDose.setText(model.getMedicineDosage());
                medDesc.setText(model.getMedicineDescription());
                medImage.setText(model.getMedicineImage());
                medTime.setText(model.getMedicineTime());

                dialogPlus.show();

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Boolean isValid = true;

                        if (!isValidInput(medName)){
                            medName.setError("Please fill in the medicine name.");
                            isValid = false;
                        }
                        if (!isValidInput(medDose)){
                            medDose.setError("Please fill in the medicine dose.");
                            isValid = false;
                        }
                        if (!isValidInput(medTime)){
                            medTime.setError("Please fill in the medicine intake time.");
                            isValid = false;
                        } else if (!isValidTime(medTime.getText().toString().trim())){
                            medTime.setError("Invalid time format (HHMM)");
                            isValid = false;
                        }

                        if(isValid){
                            Map<String,Object> map = new HashMap<>();
                            map.put("medicineName",medName.getText().toString().trim());
                            map.put("medicineDosage",medDose.getText().toString().trim());
                            map.put("medicineDescription",medDesc.getText().toString().trim());
                            map.put("medicineImage",medImage.getText().toString());
                            map.put("medicineTime",medTime.getText().toString().trim());

                            FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(selectedElderlyId).child("medicine")
                                    .child(getRef(position).getKey()).updateChildren(map)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(holder.medName.getContext(),"Medicine updated successfully.",Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(holder.medName.getContext(),"Error while updating.",Toast.LENGTH_SHORT).show();
                                            dialogPlus.dismiss();
                                        }
                                    });
                        }

                    }
                });
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.medName.getContext());
                builder.setTitle("Are you sure?");
                builder.setMessage("Deleted data cannot be undone.");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance("https://medicinereminderapp-fca68-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                .getReference().child("user").child(selectedElderlyId).child("medicine")
                                .child(getRef(position).getKey()).removeValue();
                        Toast.makeText(holder.medName.getContext(),
                                holder.medName.getText().toString()+ " succesfully removed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(holder.medName.getContext(),"Action cancelled.",Toast.LENGTH_SHORT).show();
                    }
                });

                builder.show();
            }
        });
    }



    @NonNull
    @Override
    public manageMedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_medicine_row,parent,false);
        return new manageMedicineViewHolder(view);
    }

    class manageMedicineViewHolder extends RecyclerView.ViewHolder {
        ImageView medImg;
        TextView medTime, medName, medDose, medDesc;

        Button btnUpdate, btnDelete;

        public manageMedicineViewHolder(View itemView) {
            super(itemView);

            medImg = itemView.findViewById(R.id.iv_medImage_cv);
            medTime = itemView.findViewById(R.id.tv_time_cv);
            medName = itemView.findViewById(R.id.tv_medName_cv);
            medDesc = itemView.findViewById(R.id.tv_medDesc_cv);
            medDose = itemView.findViewById(R.id.tv_medDose_cv);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnUpdate = itemView.findViewById(R.id.btn_edit);
        }

    }

}
