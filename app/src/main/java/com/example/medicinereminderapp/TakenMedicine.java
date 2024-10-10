package com.example.medicinereminderapp;

import java.util.Date;

public class TakenMedicine {
    String medicineName, medicineTime, intakeDate;
    Boolean hasTaken;

    public TakenMedicine() {
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineTime() {
        return medicineTime;
    }

    public void setMedicineTime(String medicineTime) {
        this.medicineTime = medicineTime;
    }

    public String getIntakeDate() {
        return intakeDate;
    }

    public void setIntakeDate(String intakeDate) {
        this.intakeDate = intakeDate;
    }

    public Boolean getHasTaken() {
        return hasTaken;
    }

    public void setHasTaken(Boolean hasTaken) {
        this.hasTaken = hasTaken;
    }

    public TakenMedicine(String medicineName, String medicineTime, String intakeDate, Boolean hasTaken) {
        this.medicineName = medicineName;
        this.medicineTime = medicineTime;
        this.intakeDate = intakeDate;
        this.hasTaken = hasTaken;
    }
}
